package com.nuance.plugin.backend.v1;

import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.cobra.utils.audio.OpusFileWriter;
import com.nuance.plugin.backend.v1.json.RequestJson;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gagravarr.opus.OpusAudioData;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.websocket.CloseReason;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TtsResponseSender implements IResponseSender {
    private static final Logger logger = LogManager.getLogger(TtsResponseSender.class);
    public static final String SESSION_INACTIVITY_TIMEOUT_SECONDS = "backendIdleTimeoutSeconds";
    public static final String SPEECH_SYNTHESIS_CODEC = "speechSynthesisCodec";
    public static final int TIMEOUT_COUNT_STEP = 100;


    private long idleTimeoutTimer;
    private long inactivityTimeoutSeconds;
    private final List<byte[]> responsesBinary = new LinkedList<>();
    private long responsesBinarySize = 0L;
    private final HttpServletResponse servletResponse;
    private boolean done = false;
    private final List<JSONObject> aggregator = new ArrayList<>();
    private final Object eventLock = new Object();

    TtsResponseSender(HttpServletResponse responseConn) {
        servletResponse = responseConn;
    }

    @Override
    public void sendResponse(String response) {
        JSONObject responseJson = new JSONObject(response);
        aggregator.add(responseJson);
        if (responseJson.optString("type", "").equalsIgnoreCase("commandChainResult")
                && response.matches("^.*final['\"]\\s*:\\s*true.*$")) {
            done = true;
        }
        idleTimeoutTimer = inactivityTimeoutSeconds;
    }

    @Override
    public void sendBinary(byte[] data) {
        responsesBinary.add(data);
        responsesBinarySize += data.length;
        idleTimeoutTimer = inactivityTimeoutSeconds;
    }

    @Override
    public void closeConnection(CloseReason closeReason) {
        done = true;
    }

    void waitForResponse(CombinedConfiguration currentConfig, RequestJson queryParam) throws IOException {
        inactivityTimeoutSeconds = TimeUnit.SECONDS.toMillis((long) currentConfig.getInt(SESSION_INACTIVITY_TIMEOUT_SECONDS, 30));
        idleTimeoutTimer = inactivityTimeoutSeconds;
        while (!done) {
            try {
                Thread.sleep(TIMEOUT_COUNT_STEP);
                idleTimeoutTimer -= TIMEOUT_COUNT_STEP;
                if (idleTimeoutTimer < 0) {
                    done = true;
                    servletResponse.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, "The backend was idle for too long. Terminating the connection.");
                    return;
                }
            } catch (InterruptedException e) {
                done = true;
                Thread.currentThread().interrupt();
                logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
                return;
            }
        }

        sendResponse(currentConfig, queryParam);
    }

    void sendResponse(CombinedConfiguration currentConfig, RequestJson queryParam) throws IOException {

        logger.debug("Sending back: {}", queryParam);

        servletResponse.reset();
        servletResponse.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        servletResponse.setStatus(HttpServletResponse.SC_OK);

        if (aggregator.isEmpty()) {
            servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No data from backend");
            logger.warn("No data from backend was received while waiting for webapi responses.");
            return;
        }

        PrintWriter out = servletResponse.getWriter();

        JSONObject responseBody = new JSONObject();
        responseBody.put("requestId", aggregator.get(0).optString("sessionId", "NULL"));
        responseBody.put("clientSessionId", queryParam.clientMetaDataConfig.get("clientSessionId"));

        for (JSONObject res: aggregator) {
            if (res.getString("type").equalsIgnoreCase("result")) {
                JSONObject result = res.getJSONObject("result");
                if (!result.getString("status").equalsIgnoreCase("success")) {
                    processNonSuccess(result, responseBody);
                }

                if(res.has("command") && res.getJSONObject("command").getString("name").equalsIgnoreCase("getResources")){
                    responseBody.put("voices", result.getJSONObject("resources").getJSONObject("speechSynthesis").get("voices"));
                    if(responseBody.has("requestId")) responseBody.remove("requestId");
                }

                if (res.has("command") && res.getJSONObject("command").getString("name").equalsIgnoreCase("speechSynthesis")) {
                    if (result.has("statistics")) {
                        responseBody.put("statistics", result.getJSONObject("statistics"));
                    }
                }
            }
        }

        if (!responsesBinary.isEmpty()) {
            AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
            String codecString = (String)queryParam.audioConfig.get("audioEncoding");
            if (codecString == null || codecString.isEmpty()) {
                codecString = currentConfig.getString(SPEECH_SYNTHESIS_CODEC);
            }
            AudioCodec codec = AudioCodec.fromString(codecString);

            String b64Data;

            if (codec == AudioCodec.PCM_16_8K || codec == AudioCodec.PCM_16_16K || codec == AudioCodec.PCM_16_22K) {
                AudioFormat format = new AudioFormat(codec.getSampleRate(), codec.getSampleSizeBits(), 1, true, false);

                //merge all the binary blobs into a massive stream
                SequenceInputStream blob = new SequenceInputStream(
                        Collections.enumeration(
                                responsesBinary.stream().map(ByteArrayInputStream::new).collect(Collectors.toList())
                        )
                );

                AudioInputStream audioIn = new AudioInputStream(blob, format, responsesBinarySize);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                AudioSystem.write(audioIn, fileType, buffer);

                InputStream stream = new ByteArrayInputStream(buffer.toByteArray());
                b64Data = Base64.getEncoder().encodeToString(IOUtils.toByteArray(stream));

            } else if (codec == AudioCodec.OPUS_16K || codec == AudioCodec.OPUS_8K) {
                List<OpusAudioData> data = new ArrayList<>();
                for (byte[] aResponsesBinary : responsesBinary) {
                    data.add(new OpusAudioData(aResponsesBinary));
                }
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                OpusFileWriter opusFile = new OpusFileWriter(buffer);
                opusFile.writeWithLibrary(data);
                InputStream stream = new ByteArrayInputStream(buffer.toByteArray());
                b64Data = Base64.getEncoder().encodeToString(IOUtils.toByteArray(stream));
            } else {
                servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid codec for audio data.");
                logger.warn("Invalid codec for audio data: {}", codec.getWebapiString());
                throw new IOException("Codec not supported. Codec: " + codec.getWebapiString());
            }

            JSONObject audioContent = new JSONObject();
            audioContent.put("audioEncoding", codec.getWebapiString());
            audioContent.put("audioContent", b64Data);

            responseBody.put("result", audioContent);
        }

        out.println(responseBody);
        out.flush();
    }

    protected void processNonSuccess(JSONObject result, JSONObject responseJson)
    {
        JSONArray errors = result.getJSONArray("errors");
        responseJson.put("errors", errors);

        //Check for rate limiting to change status code
        for (int i = 0; i < errors.length(); i++) {
            JSONObject error = errors.getJSONObject(i);
            int code = error.getInt("code");
            if (code == 1104 || code == 1105)
            {
                servletResponse.setStatus(429);
                return;
            }
        }
    }
}

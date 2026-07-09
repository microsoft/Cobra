package com.nuance.ncs.nmsp.servlet;

import com.nuance.ncs.nmsp.engine.RequestData;
import com.nuance.ncs.nmsp.engine.TransactionEngine;
import com.nuance.ncs.nmsp.engine.TransactionFactory;
import com.nuance.ncs.nmsp.engine.streamers.ToGwAudioStreamer;
import com.nuance.ncs.nmsp.servlet.NcsServletDefines.BodyPart;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import com.nuance.ncs.nmsp.servlet.utils.JsonPdxConverter;
import com.nuance.ncs.nmsp.util.SanitizationUtil;
import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.naming.InsufficientResourcesException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NcsServletHandler {

    private static final Logger log = LogManager.getLogger(NcsServletHandler.class);

    private static final int STREAMING_BUFFER_SIZE = 420;
    private static final String BOUNDARY_INDICATOR = "boundary=";

    private final TransactionEngine nmspTransaction = TransactionFactory.newTransactionEngine();
    private final HashMap<String, ToGwAudioStreamer> audioStreamers = new HashMap<>();
    private RequestData requestData;
    private ResponseManager responseManager;

    /**
     * The Post handler method of the servlet. <br>
     *
     * @param request  the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException               if an error occurred
     * @throws IOException                    if an error occurred
     * @throws InsufficientResourcesException
     */
    @SuppressWarnings("java:S3776")
    public void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        log.trace("----------------------------------------- doPost() start");

        // Read context
        String context = SanitizationUtil.getSanitizedHeader(request, NcsServletDefines.NUANCE_CONTEXT_HEADER);

        responseManager = TransactionFactory.newResponseManager(response, context);

        //parse response
        String contentType = request.getContentType();

        // Read AppId and AppKey from header if possible
        String headerAppId = SanitizationUtil.getSanitizedHeader(request, NcsServletDefines.NUANCE_APP_ID_HEADER);
        String headerAppKey = SanitizationUtil.getSanitizedHeader(request, NcsServletDefines.NUANCE_APP_KEY_HEADER);
        String headerUId = SanitizationUtil.getSanitizedHeader(request, NcsServletDefines.NUANCE_UID_HEADER);

        // Read the origin if any (for CORS support)
        String headerOrigin = SanitizationUtil.getSanitizedHeader(request, NcsServletDefines.HTTP_REQUEST_ORIGIN_HEADER);

        int boundaryIndex = contentType.indexOf(BOUNDARY_INDICATOR) + BOUNDARY_INDICATOR.length();
        byte[] boundary = (contentType.substring(boundaryIndex)).getBytes();
        MultipartStream multipartStream = new MultipartStream(request.getInputStream(), boundary, STREAMING_BUFFER_SIZE, null);

        boolean nexPartAvailable = multipartStream.skipPreamble();

        boolean abortRequest = false;
        while (nexPartAvailable) {
            log.debug("Reading part headers");
            String headersString = multipartStream.readHeaders();
            log.debug(headersString);
            Map<String, String> headers = parseHeaders(headersString);
            String contentDisposition = headers.get(NcsServletDefines.HTTP_CONTENT_DISPOSITION);
            String partContentType = StringUtils.defaultIfEmpty(headers.get(NcsServletDefines.HTTP_CONTENT_TYPE), "");
            String[] dispoContent = extractFormNameAndParamName(contentDisposition);
            String partName = dispoContent[0].toLowerCase();
            String paramName = dispoContent[1];
            log.debug("Part name: [{}] Param name: [{}]", partName, paramName);

            // Convert from string to enum. This optimizes the switch below.
            BodyPart part = NcsServletDefines.getBodyPartMap().get(partName);

            if (part == null) {
                responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Unidentified or invalid part name received.");
                part = BodyPart.PART_INVALID;
            }

            // NPM: Although some of the dict-style parameters seems to be handled in a similar
            // way, it is suboptimal to place them on the same path.
            switch (part) {
                case PART_NCS_REQUEST_DATA:
                    log.trace("RequestData");
                    if (!handleRequestData(multipartStream, headerAppId, headerAppKey, headerUId, headerOrigin))
                        abortRequest = true;
                    break;
                case PART_NCS_CALL_LOG_DATA:
                    log.trace("CallLogData");
                    if (NcsServletDefaults.getClientCallLogEnabled())
                        handleCallLog(multipartStream);
                    else {
                        responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Logging is not authorized on this server. Please contact customer service.");
                        abortRequest = true;
                    }
                    break;
                case PART_NCS_CONCLUDING_AUDIO_PARAMETER:
                    log.trace("ConcludingAudioParam");
                    abortRequest = !handleAudioParam(multipartStream, paramName, partContentType, true);
                    break;
                case PART_NCS_AUDIO_PARAMETER:
                    log.trace("AudioParam");
                    abortRequest = !handleAudioParam(multipartStream, paramName, partContentType, false);
                    break;
                case PART_NCS_CHOICE_PARAMETER:
                    log.trace("ChoiceParam");
                    abortRequest = !handleChoiceParam(multipartStream, paramName);
                    break;
                case PART_NCS_DATA_PARAMETER:
                    log.trace("DataParam");
                    abortRequest = !handleDataParam(multipartStream, paramName);
                    break;
                case PART_NCS_TEXT_PARAMETER:
                    log.trace("TextParam");
                    abortRequest = !handleTextParam(multipartStream, paramName);
                    break;
                case PART_NCS_DICT_PARAMETER:
                    log.trace("DictParam");
                    abortRequest = !handleDictParam(multipartStream, paramName);
                    break;
                case PART_NCS_SEQ_START_PARAMETER:
                    log.trace("SeqStartParam");
                    abortRequest = !handleSeqStartParam(multipartStream, paramName);
                    break;
                case PART_NCS_SEQ_CHUNK_PARAMETER:
                    log.trace("SeqChunkParam");
                    abortRequest = !handleSeqChunkParam(multipartStream, paramName);
                    break;
                case PART_NCS_SEQ_END_PARAMETER:
                    log.trace("SeqEndParam");
                    abortRequest = !handleSeqEndParam(multipartStream, paramName);
                    break;
                case PART_NCS_TTS_PARAMETER:
                    log.trace("TtsParam");
                    abortRequest = !handleTtsParam(multipartStream, paramName);
                    break;
                case PART_NCS_CONCLUDING_PARAMETER:
                    log.trace("Concluding Parameter");
                    // Make sure to discard data even it is empty, to avoid a MultipartStreamer
                    // intermittent bug.
                    multipartStream.discardBodyData();

                    // just end the command transaction
                    nmspTransaction.endCommand();
                    break;
                default:
                    log.warn("Unidentified or empty body part. This should not happen.");
                    abortRequest = true;
                    break;
            }

            if (abortRequest)
                break;
            log.debug(">>>>> Reading boundary");
            nexPartAvailable = multipartStream.readBoundary();
        }

        finalizeAudioStreamers();

        // At this point end command Anyway.
        nmspTransaction.endCommand();

        if (!abortRequest) {
            log.trace("Waiting for final response.");
            responseManager.waitForResponse();
        } else {
            // Make sure to send an error
            responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Request is malformed. Some necessary parts are missing or improperly specified.");
        }


        nmspTransaction.finalizeTransaction();
        log.trace("----------------------------------------- doPost() end");
    }

    private boolean handleRequestData(MultipartStream multipartStream, String headerAppId, String headerAppKey, String headerUId, String headerOrigin) {
        // Get Content
        log.trace("Handling Request Data");
        ByteArrayOutputStream output = readData(multipartStream);

        String requestJSON = "";
        requestJSON = output.toString(StandardCharsets.UTF_8);

        requestData = new RequestData(requestJSON, headerAppId, headerAppKey, headerUId, headerOrigin);

        if (requestData.succeeded()) {
			return nmspTransaction.setUpRequest(requestData, responseManager);
        } else {
            responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Request is malformed. Mandatory fields are missing from the request or improperly specified.");
            return false;
        }
	}

    private boolean handleCallLog(MultipartStream multipartStream) {
        // Get Content
        log.trace("Handling Call-Log Data");
        ByteArrayOutputStream output = readData(multipartStream);

        String requestJSON = "";
        requestJSON = output.toString(StandardCharsets.UTF_8);

        return nmspTransaction.addCallLog(requestJSON);
    }

    private boolean handleAudioParam(MultipartStream multipartStream, String paramName, String partContentType, boolean concluding) {
        log.trace("handleAudioParam");

        if (!checkParamName(paramName)) {
            try {
                log.error("handleAudioParam: paramName is not specified");
                multipartStream.discardBodyData();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return false;
        }
        String key = paramName.toLowerCase();

        // If this is the first time we see this AudioParam create the parameter, and
        // store its streamer.
        if (!audioStreamers.containsKey(key)) {
            log.debug("Adding new audio streamer key {}", key);
            PdxAudioSink audioSink = nmspTransaction.sendAudioParam(paramName);
            try {
                ToGwAudioStreamer audioStreamer = new ToGwAudioStreamer(audioSink, requestData.getInCodec());
                audioStreamers.put(key, audioStreamer);
            } catch (Exception e) {
                log.error("Couldn't create the streamer.");
                log.error(e);
                return false;
            }
            // note that if concluding audio param is sent we should hit this only once. If for any reason
            // we hit it twice, the code above would trigger a transaction ended error
            if (concluding) {
                log.debug("Concluding");
                nmspTransaction.endCommand();
            }
        }

        // Send the audio to the GW
        ToGwAudioStreamer audioStreamer = audioStreamers.get(key);
        audioStreamer.initialize(multipartStream, partContentType);
        audioStreamer.stream();
        return true;
    }

    private boolean handleChoiceParam(MultipartStream multipartStream, String paramName) {
        log.trace("handleChoiceParam");

        if (!checkParamName(paramName)) {
            try {
                log.error("handleChoiceParam: paramName is not specified");
                multipartStream.discardBodyData();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return false;
        }

        // Get Content
        ByteArrayOutputStream output = readData(multipartStream);

        String choice = "";

        choice = output.toString(StandardCharsets.UTF_8);

        return nmspTransaction.sendChoiceParam(paramName, choice);
    }

    private boolean handleDataParam(MultipartStream multipartStream, String paramName) {
        log.trace("handleDataParam");

        if (!checkParamName(paramName)) {
            try {
                log.error("handleDataParam: paramName is not specified");
                multipartStream.discardBodyData();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return false;
        }

        // Get Content
        ByteArrayOutputStream output = readData(multipartStream);

        byte[] data = output.toByteArray();
        return nmspTransaction.sendDataParam(paramName, data);
    }

    private boolean handleTextParam(MultipartStream multipartStream, String paramName)
    {
        log.trace("handleTextParam");

        if (!checkParamName(paramName)) {
            try {
                log.error("handleTextParam: paramName is not specified");
                multipartStream.discardBodyData();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return false;
        }

        // Get Content
        ByteArrayOutputStream output = readData(multipartStream);

        String text = "";
        text = output.toString(StandardCharsets.UTF_8);

        return nmspTransaction.sendTextParam(paramName, text);
    }

    private NmspDictionary handleParam(MultipartStream multipartStream, String paramName, String fromMethodName)
    {
        log.trace(fromMethodName);

        if (!checkParamName(paramName)) {
            try {
                log.error("handleParam: paramName is not specified");
                multipartStream.discardBodyData();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }

        // Get Content
        ByteArrayOutputStream output = readData(multipartStream);

        String requestJSON = "";
        requestJSON = output.toString(StandardCharsets.UTF_8);

        JSONObject jsonObject = (JSONObject) new JSONTokener(requestJSON).nextValue();
        return JsonPdxConverter.jsonObjectToPdxDictionary(jsonObject);
    }

    private boolean handleDictParam(MultipartStream multipartStream, String paramName)
    {
        NmspDictionary dict = handleParam(multipartStream, paramName, "handleDictParam");
        if(dict == null) return false;
        return nmspTransaction.sendDictParam(paramName, dict);
    }

    private boolean handleSeqStartParam(MultipartStream multipartStream, String paramName)
    {
        NmspDictionary dict = handleParam(multipartStream, paramName, "handleSeqStartParam");
        if(dict == null) return false;
        return nmspTransaction.sendSeqStartParam(paramName, dict);
    }

    private boolean handleSeqChunkParam(MultipartStream multipartStream, String paramName)
    {
        NmspDictionary dict = handleParam(multipartStream, paramName, "handleSeqChunkParam");
        if(dict == null) return false;
        return nmspTransaction.sendSeqChunkParam(paramName, dict);
    }

    private boolean handleSeqEndParam(MultipartStream multipartStream, String paramName)
    {
        NmspDictionary dict = handleParam(multipartStream, paramName, "handleSeqEndParam");
        if(dict == null) return false;
        return nmspTransaction.sendSeqEndParam(paramName, dict);
    }

    private boolean handleTtsParam(MultipartStream multipartStream, String paramName)
    {
        NmspDictionary dict = handleParam(multipartStream, paramName, "handleTtsParam");
        if(dict == null) return false;
        return nmspTransaction.sendTtsParam(paramName, dict);
    }

    private void finalizeAudioStreamers()
    {
        log.trace("Finalize Audio Streamers.");
        for (ToGwAudioStreamer toGwAudioStreamer : audioStreamers.values()) {
            toGwAudioStreamer.terminate();
        }
        audioStreamers.clear();
    }

    // Extracts both form name and param name in one run to optimize parsing
    private String[] extractFormNameAndParamName(String contentDisposition) {
        String[] ret = new String[2];
        ret[0] = null;
        ret[1] = null;
        String[] parts = contentDisposition.split(";");

        for (String s : parts) {
            // do this once and use for all 3 checks
            String cDLower = s.toLowerCase();
            if (cDLower.replaceAll("\\s", "").startsWith("paramname=")) {
                ret[1] = extractValue(s);
            } else if (cDLower.replaceAll("\\s", "").startsWith("filename="))
            {
                //Do nothing
            } else if (cDLower.replaceAll("\\s", "").startsWith("name=")) {
                ret[0] = extractValue(s);
            }
        }
        return ret;
    }

    // extracts value from strings of this form
    // key = "value"
    private String extractValue(String in) {
        String[] sSplit = in.split("=");
        if (sSplit.length > 1)
            return sSplit[1].replace("\"", "");

        return null;
    }

    private Map<String, String> parseHeaders(String headerString) throws IOException {
        String line;
        int idx;
        Map<String, String> headers = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new StringReader(headerString))) {
			line = reader.readLine();
			while (!line.equals("")) {
				idx = line.indexOf(':');
				if (idx < 0) {
					throw new IOException("Failed to read headers");
				} else {
					headers.put(line.substring(0, idx).toLowerCase(), line.substring(idx + 1).trim());
				}
				line = reader.readLine();
			}
		}

        return headers;
    }

    private ByteArrayOutputStream readData(MultipartStream multipartStream) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Read all
        try {
            while (multipartStream.readBodyData(output) > 0)
            {
                //Do nothing
            }
        } catch (MalformedStreamException e) {
            log.error("readData: MalformedStreamException.", e);
            responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Malformed request.");
        } catch (IOException e) {
            log.error("readData: IOException.", e);
            responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Could not read request. It was possibly corrupted.");
        }

        return output;
    }

    private boolean checkParamName(String paramName) {
        if (paramName == null) {
            responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "paramName not sent. Please check your request.");
            return false;
        }
        return true;
    }
}

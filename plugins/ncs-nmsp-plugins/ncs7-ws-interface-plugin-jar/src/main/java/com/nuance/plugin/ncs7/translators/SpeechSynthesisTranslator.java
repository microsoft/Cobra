package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.config.ConfigHelpers;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static com.nuance.plugin.ncs7.Ncs7InterfaceConfig.Property.INSERT_AUDIO_MESSAGE;

public class SpeechSynthesisTranslator extends AbstractCommandTranslator {
    private static final Logger logger = LogManager.getLogger();

    public static final String WEBAPI_NAME = "speechSynthesis";
    public static final String NCS_NAME = "NMDP_TTS_CMD";
    private boolean hasSentAudioStartMessage = false;
    private boolean insertAudioMessages = false;

    public SpeechSynthesisTranslator (Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
        try {
            insertAudioMessages = orchestrator.getWebApiFeatures().getConfigs().getBoolean(INSERT_AUDIO_MESSAGE.getKey(), (boolean)INSERT_AUDIO_MESSAGE.getDefaultValue());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @Override
    public void sendBinary(byte[] buffer) {
        logger.debug("<====== Stream to client: {}", buffer.length);
        try {
            if(!hasSentAudioStartMessage) {
                doAudioBegins(orchestrator); // Will also send an initial audio message
            }
            else if(insertAudioMessages) {
                NcsResponse ncsResponse = new NcsResponse();
                ncsResponse.message = "audio";
                ncsResponse.audio_id = orchestrator.getAudioId();
                respondToClient(orchestrator, ncsResponse.toJson());
            }
        } catch (IOException e) {
            logger.error(e);
        }
        orchestrator.getWebSocketSession().getAsyncRemote().sendBinary(ByteBuffer.wrap(buffer));
    }

    @Override
    public void execute() throws IOException {
        doRequest(ncsRequest);
        waitForResponse();
    }

    protected void doRequest(NcsRequest ncsRequest) throws IOException {
        JSONObject parameters = new JSONObject();
        parameters.put("data", ncsRequest.dictionary.get("tts_input"));
        parameters.put("type", ncsRequest.dictionary.get("tts_type"));
        if (orchestrator.getCodec()== null) {
            parameters.put("speechSynthesisCodec", ncsRequest.getWebAPICodecName(ncsRequest.getCodecName()));
        } else {
            parameters.put("speechSynthesisCodec", ncsRequest.getWebAPICodecName(orchestrator.getCodec()));
        }

        if (ncsRequest.tts_voice != null) {
            parameters.put("voice", ncsRequest.tts_voice);
        } else if (ncsRequest.getTTSLanguage() != null) {
            String ttsLanguage = ncsRequest.getTTSLanguage().toLowerCase();
            HierarchicalConfiguration vocs = orchestrator.getWebApi().getActiveSession().getSessionConfig().configurationAt("vocsMapping");
            for (String voice: ConfigHelpers.extractRootKeys(vocs)) {

                HierarchicalConfiguration voiceConf = vocs.configurationAt(voice);
                if (voiceConf.containsKey("language")
                        && voiceConf.getString("language").equalsIgnoreCase(ttsLanguage)) {
                    parameters.put("voice", voice);
                    break;
                }
            }
        }

        String ttsGenre = getTtsGenre(ncsRequest);

        if(ttsGenre != null) {
            parameters.put("speechSynthesisTuningBundle", ttsGenre);
        }

        parameters.put("speechSynthesisRate", ncsRequest.tts_rate);

        final var modelImprovementsDefined = orchestrator.areModelImprovementsDefined("model-improvements");
        if (modelImprovementsDefined && orchestrator.areModelImprovementsEnabled("model-improvements")) {
            parameters.put("sensitivity", "open");
        }

        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", WEBAPI_NAME);
        command.put("id", getCommandID(ncsRequest));

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "command");
        webApiRequest.put("sessionId", orchestrator.getWebapiSessionID());
        webApiRequest.put("command", command);

        if (ncsRequest.dictionary.get("audio_id") == null) {
            throw new IllegalArgumentException("Trying to run NMDP_TTS_CMD without audio_id");
        }

        orchestrator.setAudioId((int) ncsRequest.dictionary.get("audio_id"));
        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    protected String getTtsGenre(NcsRequest ncsRequest) {
        if(ncsRequest.tts_text_genre != null && !ncsRequest.tts_text_genre.isEmpty()) {
            return ncsRequest.tts_text_genre;
        }
        else if(ncsRequest.tts_genre != null && !ncsRequest.tts_genre.isEmpty()) {
            return ncsRequest.tts_genre;
        }
        return null;
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        doAudioDone(orchestrator, webApiResponse);

        NcsResponse ncsResponse = new NcsResponse();
        if (webApiResponse.isSuccess())
        {
            ncsResponse.message = "query_response";
        } else if (webApiResponse.isCanceled())
        {
            ncsResponse.message = "query_error";
            ncsResponse.reason = "cancelled";
        } else {
            AbstractCommandTranslator.ExtractedErrors err = AbstractCommandTranslator.extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
            ncsResponse.message = "query_error";
            ncsResponse.reason = err.message;
            ncsResponse.code = err.code;
            ncsResponse.parameter = "TEXT_TO_READ";
        }
        ncsResponse.TTSStatus = capitalise((String) webApiResponse.result.get("status"));
        ncsResponse.transaction_id = Integer.parseInt((String)webApiResponse.command.get("id"));
        ncsResponse.result_type = NCS_NAME;
        ncsResponse.NMAS_PRFX_SESSION_ID = orchestrator.getSessionId();
        ncsResponse.NMAS_PRFX_TRANSACTION_ID = (String)webApiResponse.command.get("id");

        respondToClient(orchestrator, ncsResponse.toJson());

        if(webApiResponse.isSuccess() || orchestrator.getProtocolVersion() == Orchestrator.ProtocolVersion.v1) {
            sendQueryEnd(orchestrator, webApiResponse);
        }

        return doFinal(orchestrator, webApiResponse);
    }

    public void doAudioBegins(Orchestrator orchestrator) {
        try {
            hasSentAudioStartMessage = true;
            NcsResponse ncsResponse = new NcsResponse();
            ncsResponse.message = "audio_begin";
            ncsResponse.audio_id = orchestrator.getAudioId();
            ncsResponse.codec = orchestrator.getCodec();
            ncsResponse.codec_id = orchestrator.getCodecId();
            respondToClient(orchestrator, ncsResponse.toJson());

            ncsResponse = new NcsResponse();
            ncsResponse.message = "audio";
            ncsResponse.audio_id = orchestrator.getAudioId();
            respondToClient(orchestrator, ncsResponse.toJson());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void doAudioDone(Orchestrator orchestrator, WebApiResponse webApiResponse) {
        if (webApiResponse.isFinal() && hasSentAudioStartMessage) {
            NcsResponse ncsResponse = new NcsResponse();
            ncsResponse.message = "audio_end";
            ncsResponse.audio_id = orchestrator.getAudioId();

            try {
                respondToClient(orchestrator, ncsResponse.toJson());
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void commandCleanup() {
        if (streamer != null) {
            streamer.terminate(true);
        }
    }
}

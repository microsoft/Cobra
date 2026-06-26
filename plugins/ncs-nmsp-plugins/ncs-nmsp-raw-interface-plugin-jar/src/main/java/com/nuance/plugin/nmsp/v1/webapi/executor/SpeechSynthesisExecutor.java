package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.config.ConfigHelpers;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.audio.AudioEncoder;
import com.nuance.plugin.audio.PassThroughAudioEncoder;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.messages.PDXQueryResult;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayEndMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpeechSynthesisExecutor extends Executor {
    private static final Logger logger = LogManager.getLogger();

    AudioEncoder audioEncoder = new PassThroughAudioEncoder(); //default value

    public SpeechSynthesisExecutor(Session nmspSession, HashMap<String, Object> queryInfo, String webAPISessionID) {
        super(nmspSession, queryInfo, webAPISessionID);
    }

    public static final String WEBAPI_SPEECH_SYNTHESIS_CMD = "speechSynthesis";
    public static JSONObject buildSpeechSynthesisCommand(Map<String, Object> queryInfo, Session nmspSession) throws IOException {
        JSONObject command = new JSONObject();

        JSONObject parameters = new JSONObject();
        if (null != queryInfo.get("tts_voice")) {
            String ttsVoice = (String) queryInfo.get("tts_voice");
            parameters.put("voice", ttsVoice);
        } else if (queryInfo.get("tts_language") != null) {
            CombinedConfiguration sessionConfig = nmspSession.getWebAPIOrchestrator().getSessionConfigs();
            HierarchicalConfiguration<ImmutableNode> vocs = sessionConfig.configurationAt("vocsMapping");
            for (String voice: ConfigHelpers.extractRootKeys(vocs)) {

                HierarchicalConfiguration<ImmutableNode> voiceConf = vocs.configurationAt(voice);
                if (voiceConf.containsKey("language")
                        && voiceConf.getString("language").equalsIgnoreCase(((String)queryInfo.get("tts_language")).toLowerCase())) {
                    parameters.put("voice", voice);
                    break;
                }
            }
        }
        PdxDictionary dict = (PdxDictionary) queryInfo.get("dict");
        parameters.put("type", dict.get("tts_type"));
        parameters.put("data", dict.get("tts_input"));
        parameters.put("speechSynthesisCodec", nmspSession.getToCodec().getWebAPIString());

        String ttsGenre = getTtsGenre(queryInfo);
        if(ttsGenre != null) {
            parameters.put("speechSynthesisTuningBundle", ttsGenre);
        }

        Integer audioId = (Integer) dict.get("audio_id");
        nmspSession.getWebAPIOrchestrator().setAudioId(audioId);
        command.put("parameters", parameters);
        command.put("name", WEBAPI_SPEECH_SYNTHESIS_CMD);

        return command;
    }

    public void execute() throws IOException {
        //setup transcoders if we need to.
        var configs = PdxInterfaceConfig.getPluginConfMap(nmspSession.getWebAPIOrchestrator().getSessionConfigs());
        audioEncoder = AudioEncoder.buildEncoder(nmspSession.getToCodec(), configs);

        JSONArray commands = new JSONArray();
        JSONObject command = buildSpeechSynthesisCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        commands.put(command);

        command = EndSessionExecutor.buildEndSessionCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        commands.put(command);

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "commandChain");
        webApiRequest.put("sessionId", getWebAPISessionID());
        webApiRequest.put("commands", commands);

        nmspSession.getWebAPIOrchestrator().sendToWebapi(this, webApiRequest.toString(), false);
    }

    public void onResponse(WebApiResponse responseJson) throws IOException {
        if (responseJson.isResult()  && responseJson.isForCommand(WEBAPI_SPEECH_SYNTHESIS_CMD)) {
            nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, true);

            if (responseJson.isSuccess()) {
                if (responseJson.isFinal()) {
                    doFinal(responseJson);
                }
            }

            if (responseJson.isError()) {
                ExtractedErrors errors = extractWebApiErrors((ArrayList<HashMap<String, Object>>) responseJson.result.get("errors"));
                nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, errors.message, errors.code));
            }

            if (responseJson.isCanceled()) {
                if (!isTerminated) {
                    nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, "Operation canceled", ResponseCodes.CompletionCause.CANCELLED));
                } else {
                    endWebapiSessionWithFinal(null);
                }
            }
        }
    }

    protected void doFinal(WebApiResponse responseJson) {
        VAPPlayEndMessage vapPlayEnd = new VAPPlayEndMessage(VAPMessage.VAP_MINIMUM_VERSION);
        vapPlayEnd.setAudioId(nmspSession.getWebAPIOrchestrator().getAudioId());
        nmspSession.getClientChannel().write(vapPlayEnd);

        endWebapiSessionWithFinal(buildPdxResult().toByteArray());
    }

    private PDXQueryResult buildPdxResult() {
        PDXDictionary response = new PDXDictionary();
        response.addUTF8String("NMAS_PRFX_SESSION_ID", nmspSession.getSessionIdString());
        response.addUTF8String("NMAS_PRFX_TRANSACTION_ID", String.valueOf(nmspSession.getWebAPIOrchestrator().getTransactionID()));
        response.addUTF8String("result_type", getCommandName());
        response.addUTF8String("TTSStatus", "Success");
        return new PDXQueryResult(response.toByteArray());
    }

    @Override
    public void onBinary(byte[] data) {
        VAPPlayMessage vapPlayMessage = new VAPPlayMessage(VAPMessage.VAP_MINIMUM_VERSION);
        vapPlayMessage.setAudioId(nmspSession.getWebAPIOrchestrator().getAudioId());
        vapPlayMessage.setAudio(audioEncoder.onAudio(data));

        if (nmspSession.getClientChannel().isActive()) {
            logger.debug("<====== Sending to client {}", vapPlayMessage);

            // see VAPPlaySerializer
            nmspSession.getClientChannel().write(vapPlayMessage);
        } else {
            logger.error("Channel not connected, dropping message");
        }
    }

    @Override
    public void onClose(CloseReason closeReason) {
        audioEncoder.onDone();
    }

    @Override
    public void cancelCommand() {
        sendDirective("cancel", WEBAPI_SPEECH_SYNTHESIS_CMD);
        isTerminated = true;
        terminateStreaming();
    }

    protected static String getTtsGenre(Map<String, Object> dict) {
        var ttsTextGenreObj = dict.get("tts_text_genre");
        var ttsGenreObj = dict.get("tts_genre");
        if(ttsTextGenreObj instanceof String && !((String) ttsTextGenreObj).isEmpty()) {
            return (String)ttsTextGenreObj;
        }
        else if(ttsGenreObj instanceof String && !((String) ttsGenreObj).isEmpty()) {
            return (String)ttsGenreObj;
        }
        return null;
    }
}

package com.nuance.plugin.ncs7;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.plugin.ncs7.translators.SpeechSynthesisTranslator;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NCS7InterfacePlugin implements IWebApiRawPlugin {
    private static final Logger logger = LogManager.getLogger(NCS7InterfacePlugin.class);
    private static final String PLUGIN_URL_FILTER = "ncs7($|/|/.*)";
    private IWebApiFeatures webApiFeatures;
    IWebApiEndpoint webApi;
    WebApiResponseHandler responseSender;
    Map<String, Object> ncsJsonRequest = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    Orchestrator orchestrator;

    @Override
    public void onInitPlugin(IWebApiFeatures featuresList) {
        this.webApiFeatures = featuresList;
    }

    @Override
    public String getUrlPattern() {
        return PLUGIN_URL_FILTER;
    }

    @Override
    public boolean onSocketOpen(Session session) throws IOException {
        orchestrator = new Orchestrator(session);

        responseSender = new WebApiResponseHandler(orchestrator);
        webApi = webApiFeatures.getWebApiEndpoint(responseSender);
        orchestrator.setWebApi(webApi);
        orchestrator.setWebApiFeatures(webApiFeatures);
        return true;
    }

    @Override
    public boolean onSocketMessage(Session session, String message) throws IOException {
        logger.debug(">====== Message from client: {}", message);
        doCommandConversion(message);
        return true;
    }

    @Override
    public boolean onSocketBinary(Session session, byte[] buffer) throws IOException {
        logger.debug(">====== Binary from client: {} bytes", buffer.length);
        orchestrator.clientAudioBytesBufferQueue.add(Map.entry(Orchestrator.MessageType.DATA, buffer));
        return true;
    }

    @Override
    public boolean onSocketError(Session session, Throwable error) throws IOException {
        logger.info("On socket error", error);
        webApi.onError(session, error);
        session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, error.getMessage()));
        orchestrator.terminate();
        return true;
    }

    @Override
    public boolean onSocketClosed(Session session, CloseReason reason) throws IOException {
        logger.debug("On socket close: {}", reason.getReasonPhrase());
        webApi.onClose(session, reason);
        orchestrator.terminate();
        webApiFeatures.close();
        return true;
    }

    @Override
    public boolean onSocketPong(Session session, PongMessage pongMessage) throws IOException {
        orchestrator.notify(pongMessage.toString());
        return true;
    }

    private void doCommandConversion(String json) throws IOException {
        Map<String, Object> crMessage = mapper.readValue(json, new TypeReference<>(){});

        switch ((String)crMessage.get("message")) {
            case "query_begin":
                // SubscriberId and deviceModel come in the query_begin message, but are required for cobra's startSession
                // This message will trigger the execution of the StartSessionTranslator
                orchestrator.clientMessageBufferQueue.add( Map.entry(Orchestrator.MessageType.SUBSCRIBER, crMessage));

                //reset the aggregated data
                ncsJsonRequest = mapper.readValue(json, HashMap.class);
                break;
            case "query_parameter":
                //aggregate all properties into one map
                mergeMap(ncsJsonRequest, crMessage);

                // If is TTS, don't wait for 'query_end'. Launch TTS now.
                if (SpeechSynthesisTranslator.NCS_NAME.equals(ncsJsonRequest.get("command"))) {
                    orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, ncsJsonRequest));
                }
                break;
            case "query_end":
                //finish aggregating and try to run webapi.
                mergeMap(ncsJsonRequest, crMessage);

                // If is TTS, ignore 'query_end' message. TTS was already launched.
                if (SpeechSynthesisTranslator.NCS_NAME.equals(ncsJsonRequest.get("command"))) {
                    break;
                }
                orchestrator.clientMessageBufferQueue.add( Map.entry(Orchestrator.MessageType.MESSAGE, ncsJsonRequest));
                break;

            case "connect":
                orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, crMessage));
                break;
            //audio
            case "audio":
                orchestrator.setAudioId((Integer) crMessage.get("audio_id"));
                orchestrator.setStartTime(System.currentTimeMillis());
            case "audio_begin":
            case "audio_end":
                orchestrator.setEndTime(System.currentTimeMillis());
                orchestrator.clientAudioBytesBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, crMessage));
                break;
            case "disconnect":
                // In V1, the ``disconnect`` message closes the connection between the client and the WebSocket server.
                // In V2, the ``disconnect`` message does not close the connection between the client and the WebSocket server. The connection between the client and the WebSocket server will be closed automatically after a certain period of inactivity.
                orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, crMessage));
                break;
            default:
                logger.info("NCS message not implemented: {}", crMessage.get("message"));
                orchestrator.sendDisconnect(-1, "Unknown message string: " + (String)crMessage.get("message"));
        }
    }

    private void mergeMap(Map<String, Object> result, Map<String, Object> toMerge) {
        for (String key: toMerge.keySet()) {
            result.merge(key, toMerge.get(key), (oldValue, newValue) -> {
                if (newValue instanceof Map) {
                    mergeMap((Map<String, Object>)oldValue, (Map<String, Object>)newValue);
                }
                if (newValue instanceof List) {
                    ((List<Object>)newValue).addAll((List<Object>)oldValue);
                }
                return newValue;
            });
        }
    }
}

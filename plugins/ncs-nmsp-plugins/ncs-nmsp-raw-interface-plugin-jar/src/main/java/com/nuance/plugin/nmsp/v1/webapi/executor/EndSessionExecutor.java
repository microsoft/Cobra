package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EndSessionExecutor extends Executor {
    private static final Logger logger = LogManager.getLogger();

    private final byte[] response;

    public EndSessionExecutor(Session nmpsSession, HashMap<String, Object> queryInfo, String webAPISessionID, byte[] responseResult) {
        super(nmpsSession, queryInfo, webAPISessionID);
        this.response = responseResult;
    }

    public static final String WEBAPI_END_SESSION_CMD = "endSession";
    public static JSONObject buildEndSessionCommand(Map<String, Object> queryInfo, Session nmspSession) {
        JSONObject parameters = new JSONObject();
        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", WEBAPI_END_SESSION_CMD);

        return command;
    }

    public void execute() {
        try {
            if (nmspSession.getWebAPIOrchestrator().isWebapiSessionAlive()) {
                executeEndSession();
            } else {
                logger.debug("====== Skipping end session since there is no active session.");
                sendFinal();
            }
        } catch (IOException e) {
            logger.error("Failed to end session", e);
            sendFinal();
        }
    }

    private void executeEndSession() throws IOException {
        JSONObject parameters = new JSONObject();
        JSONObject command = buildEndSessionCommand(queryInfo, nmspSession);
        command.put("id", commandID);

        JSONObject request = new JSONObject();
        request.put("type", "command");
        request.put("sessionId", getWebAPISessionID());
        request.put("command", command);

        nmspSession.getWebAPIOrchestrator().sendToWebapi(this, request.toString(), false);
    }

    public void onResponse(WebApiResponse responseJson) throws IOException {
        setWebAPISessionID(responseJson.sessionId);

        sendFinal();
        cleanup();
    }

    private void sendFinal() {
        sendFinalResponse(response);
    }

    @Override
    public void onBinary(byte[] data) {
        //nothing here.
    }

    @Override
    public void onClose(CloseReason closeReason) {
        String reason = "Failed to initialize end session";
        if (closeReason != null) {
            reason += " " + closeReason.getReasonPhrase();
        }
        logger.warn(reason);
    }

    @Override
    public void cancelCommand() {
        isTerminated = true;
    }
}

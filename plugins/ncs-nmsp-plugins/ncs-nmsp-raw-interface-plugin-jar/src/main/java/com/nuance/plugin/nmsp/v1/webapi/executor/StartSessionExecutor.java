package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.hash.CobraHash;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import joptsimple.internal.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StartSessionExecutor extends Executor {
    private static final Logger logger = LogManager.getLogger();

    public static final String WEBAPI_START_SESSION_CMD = "startSession";

    public StartSessionExecutor(Session nmpsSession, HashMap<String, Object> queryInfo, String webAPISessionID) {
        super(nmpsSession, queryInfo, webAPISessionID);

        if (nmspClientHandler == null) {
            logger.error("Client channel is null");
            throw new IllegalStateException("Client no longer connected");
        }
    }

    public void execute() {
        try {
            if (!nmspSession.getWebAPIOrchestrator().isWebapiSessionAlive()) {
                executeStartSession();
            } else {
                logger.debug("====== Skipping start session since there is already an active session.");
                nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, false);
            }
        } catch (IOException e) {
            logger.error("Failed to initialize start session", e);

            nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, e.getMessage(), ResponseCodes.CompletionCause.INVALID_REQUEST));

            nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, true);
        }
    }

    private void executeStartSession() throws IOException {
        nmspSession.getWebAPIOrchestrator().initWebsocketSession();
        JSONObject auth = new JSONObject();
        if (nmspSession.getClientNmaidKey() != null) {
            auth.put("type", "hashcode");
            auth.put("data", CobraHash.sha256(nmspSession.getClientNmaidKey()));
        } else {
            auth.put("type", "no_auth");
        }

        JSONObject parameters = new JSONObject();
        parameters.put("authentication", auth);

        parameters.put("applicationKey", nmspSession.getNmaid());
        parameters.put("clientUserId", nmspSession.getObfuscatedUserId());
        parameters.put("clientDeviceId", nmspSession.getDeviceId());
        parameters.put("clientSessionId", nmspSession.getSessionIdString());
        parameters.put("clientSubscriberId", nmspSession.getSubscriberId());

        Map<String, Object> clientMetadata = getMetadata(nmspSession);
        if(clientMetadata.size() > 0) {
            parameters.put("clientDetails", clientMetadata);
        }

        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", WEBAPI_START_SESSION_CMD);
        command.put("id", commandID);

        JSONObject request = new JSONObject();
        request.put("type", "command");
        request.put("command", command);

        nmspSession.getWebAPIOrchestrator().sendToWebapi(this, request.toString(), true); //Send this asynchronosly, to have webapi run in a separate thread.
    }

    @NotNull
    private static Map<String, Object> getMetadata(Session nmspSession) {
        Map<String, Object> clientMetadata = new HashMap<>();

        String deviceModel = nmspSession.getDeviceModel(); // Device model is the new phone model
        if (Strings.isNullOrEmpty(deviceModel)) {
            deviceModel = nmspSession.getPhoneModel();
        }
        if(!Strings.isNullOrEmpty(deviceModel)) {
            clientMetadata.put("deviceModel", deviceModel);
        }

        try {
            clientMetadata.put("deviceCodec", String.valueOf(nmspSession.getFromCodec().getNmaidCode()));
        }
        catch(Exception e) {
            logger.warn("Failed to convert codec to deviceCodec");
        }

        return clientMetadata;
    }

    public void onResponse(WebApiResponse responseJson) throws IOException {
        setWebAPISessionID(responseJson.sessionId);
        if (responseJson.isSuccess()) {
            if (WEBAPI_START_SESSION_CMD.equals(responseJson.command.get("name"))) {
                nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, false);
            }
        } else if (!responseJson.isSuccess()) {
            nmspSession.getWebAPIOrchestrator().sendError(nmspSession,  ResponseUtils.buildError(nmspSession, "", ResponseCodes.CompletionCause.INVALID_REQUEST));
            nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, true);
        }
    }

    @Override
    public void onBinary(byte[] data) {
        //nothing here.
    }

    @Override
    public void onClose(CloseReason closeReason) {
        String reason = "Failed to initialize start session";
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

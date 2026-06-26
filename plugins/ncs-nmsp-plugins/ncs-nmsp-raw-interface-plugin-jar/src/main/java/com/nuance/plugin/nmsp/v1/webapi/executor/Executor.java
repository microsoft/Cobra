package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.IWebapiOrchestratorEvents;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import com.nuance.plugin.nmsp.v1.webapi.streaming.StreamingService;
import joptsimple.internal.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Executor implements IWebapiOrchestratorEvents, IEndSessionListener {
    private static final Logger logger = LogManager.getLogger();

    protected final Session nmspSession;
    protected final IClientHandler nmspClientHandler;
    protected StreamingService streamer;
    protected HashMap<String, Object> queryInfo;
    protected boolean isTerminated = false;
    protected final String commandID;
    private byte[] finalResponseResult;

    public Executor(Session nmspSession, HashMap<String, Object> queryInfo, String webAPISessionID) {
        this.nmspSession = nmspSession;
        this.queryInfo = queryInfo;
        if (queryInfo.containsKey("device_codec")) {
            nmspSession.setFromCodec(ICodecResource.Codec.fromNMAINCodecId((int) queryInfo.get("device_codec")));
            nmspSession.setToCodec(ICodecResource.Codec.fromNMAINCodecId((int) queryInfo.get("device_codec")));
        }
        setWebAPISessionID(webAPISessionID);

        this.nmspClientHandler = nmspSession.getClient();
        this.commandID = "NMAS_PRFX_TRANSACTION_ID:" + nmspSession.getWebAPIOrchestrator().getTransactionID();
    }

    protected static ExtractedErrors extractWebApiErrors(ArrayList<HashMap<String, Object>> errors) {
        StringBuilder response = new StringBuilder();
        ExtractedErrors result = new ExtractedErrors();
        result.code = ResponseCodes.CompletionCause.OPERATION_FAILED;

        for (HashMap<String, Object> error : errors) {
            int code = (int) error.get("code");
            if (code == 1205) {
                result.code = ResponseCodes.CompletionCause.AUTHORIZE_ERROR_NMAID_INVALID;
            }

            if (code >= 1500 && code < 1600) {
                result.code = ResponseCodes.CompletionCause.TTS_ERROR;
            }

            if (code >= 1600 && code < 1700) {
                result.code = ResponseCodes.CompletionCause.REC_ERROR;
            }

            response.append(code).append(": ");
            response.append((String) error.get("message")).append(" ");
        }

        result.message = response.toString().strip();
        return result;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    protected String getCommandName() {
        return (String) queryInfo.get("command");
    }

    public String getWebAPISessionID() {
        return nmspSession.getWebAPIOrchestrator().getWebAPISessionID();
    }

    public void setWebAPISessionID(String webAPISessionID) {
        nmspSession.getWebAPIOrchestrator().setWebAPISessionID(webAPISessionID);
    }

    protected void endWebapiSessionWithFinal(byte[] responseResult) {
        this.finalResponseResult = responseResult;
    }

    protected void sendFinalResponse(byte[] responseResult)
    {
        if (responseResult != null && nmspSession.getWebAPIOrchestrator() != null)
        {
            nmspClientHandler.sendData(nmspSession, nmspSession.getWebAPIOrchestrator().getTransactionID(), nmspSession.getLastPingRequest(),
                    responseResult);
        }
    }

    public void onClientDisconnected() {

    }

    @Override
    public void onResponse(String response) {
        try {
            WebApiResponse responseJson = WebApiResponse.fromJson(response);
            if (responseJson.isChainResult() && responseJson.isFinal()) {
                sendFinalResponse(finalResponseResult);
            } else {
                onResponse(responseJson);
            }
        } catch (Throwable t) {
            logger.error("Failed to parse session result", t);
            nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, t.getMessage(), ResponseCodes.CompletionCause.OPERATION_FAILED));
            sessionEnded();
        }
    }

    protected void cleanup() {
        isTerminated = true;
        terminateStreaming();
        setWebAPISessionID("");
    }

    protected void terminateStreaming() {
        if (streamer != null) {
            streamer.terminate();
        }
    }

    protected void sendDirective(String directiveName, String commandName) {
        if (Strings.isNullOrEmpty(getWebAPISessionID())
                || nmspSession.getWebAPIOrchestrator().getActiveSession() == null
                || nmspSession.getWebAPIOrchestrator().getActiveSession().getCommand() == null) {
            logger.debug("Trying to send directive on a terminated. command: {} directive: {}", commandName, directiveName);
            return;
        }
        JSONObject directive = new JSONObject();
        directive.put("name", directiveName);

        JSONObject command = new JSONObject();
        command.put("name", commandName);
        command.put("id", commandID);

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "directive");
        webApiRequest.put("sessionId", getWebAPISessionID());
        webApiRequest.put("command", command);
        webApiRequest.put("directive", directive);

        try {
            nmspSession.getWebAPIOrchestrator().sendToWebapi(this, webApiRequest.toString(), false);
        } catch (IOException e) {
            logger.error("Failed to send {} directive", directiveName, e);
            nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, "", ResponseCodes.CompletionCause.OPERATION_FAILED));
            sessionEnded();
        }
    }

    public abstract void onResponse(WebApiResponse responseJson) throws IOException;

    public abstract void execute() throws IOException;

    public abstract void cancelCommand();

    protected static class ExtractedErrors {
        String message;
        ResponseCodes.CompletionCause code;
    }

    @Override
    public void sessionEnded() {
        cleanup();
        nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, true);
    }
}

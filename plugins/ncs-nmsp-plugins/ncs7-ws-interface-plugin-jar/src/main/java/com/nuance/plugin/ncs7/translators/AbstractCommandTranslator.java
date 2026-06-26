package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import com.nuance.plugin.ncs7.streaming.StreamingService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public abstract class AbstractCommandTranslator {
    protected NcsRequest ncsRequest;

    enum ExecutionStatus {
        SUCCESS(true),
        FAILED(false);

        boolean booleFlag;
        ExecutionStatus(boolean booleFlag) {
            this.booleFlag = booleFlag;
        }
    }

    protected static class ExecutionResult {
        private ExecutionStatus status;
        private boolean done;

        public void setStatus(ExecutionStatus status) {
            this.status = status;
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        public ExecutionResult(ExecutionStatus status, boolean done) {
            this.status = status;
            this.done = done;
        }
    }

    private static final Logger logger = LogManager.getLogger();

    public static final long THREAD_MAX_TIMEOUT_SECONDS = 10;
    public static final ExecutorService executor = Executors.newCachedThreadPool(); //this thread pool will resize according to load.
    protected final Orchestrator orchestrator;
    StreamingService streamer;
    String commandID;

    String getCommandID(NcsResponse ncsResponse) {
        if (ncsResponse == null) {
            return getCommandIDFromString(null);
        } else {
            return getCommandIDFromString(String.valueOf(ncsResponse.transaction_id));
        }
    }

    String getCommandID(NcsRequest ncsRequest) {
        if (ncsRequest == null) {
            return getCommandIDFromString(null);
        } else {
            return getCommandIDFromString(ncsRequest.transaction_id);
        }
    }

    String getCommandIDFromString(String transactionID)
    {
        if(commandID != null)
        {
            return commandID;
        }

        SecureRandom secureRandom = new SecureRandom();
        commandID = "NCS7Plugin_ID_" + secureRandom.nextInt();

        if(transactionID != null && !transactionID.equalsIgnoreCase("null"))
        {
            commandID = transactionID;
        }

        return commandID;
    }

    protected AbstractCommandTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        this.orchestrator = orchestrator;
        this.ncsRequest = ncsRequest;
    }

    public abstract void execute() throws IOException;

    /**
     * Clean up after the command is done
     */
    public void commandCleanup() {

    }

    protected void waitForResponse() throws IOException {
        logger.debug("====== {} Start waiting for response", this.getClass().getSimpleName());

        //this loop may need a timeout condition.
        ExecutionResult result = new ExecutionResult(ExecutionStatus.SUCCESS, false);

        while (!result.isDone()) {
            try {
                Map.Entry<Orchestrator.MessageType, Object> entry = orchestrator.webapiMessageBufferQueue.poll(THREAD_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (entry == null) {
                    //timeout, cleanup and terminate
                    entry = Map.entry(Orchestrator.MessageType.NOTIFY, "");
                }

                switch (entry.getKey()) {
                    case DATA:
                        byte[] data = (byte[])entry.getValue();
                        sendBinary(data);
                        break;
                    case MESSAGE:
                        String responseJson = (String) entry.getValue();
                        WebApiResponse webApiResponse = WebApiResponse.fromJson(responseJson);
                        if (!webApiResponse.isChainResult()) {
                            result = doResponse(webApiResponse);
                        }
                        break;
                    case TERMINATE:
                        result.done = true;
                        break;
                    default:
                        //do Nothing
                }

            } catch (InterruptedException e) {
                result.done = true;
                Thread.currentThread().interrupt();
            }
        }

        logger.debug("====== {} Done waiting for response", this.getClass().getSimpleName());
        orchestrator.notify(this.getClass().getSimpleName() + " " + Thread.currentThread().getName());
    }

    public void sendBinary(byte[] buffer) {
        // Do nothing
    }

    abstract ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException; //should return false if there are no other responses expected.

    static void respondToClient(Orchestrator orchestrator, String message) {
        logger.debug("<====== Sending back to client: {}", message);
        orchestrator.getWebSocketSession().getAsyncRemote().sendText(message);
    }

    static void sendQueryEnd(Orchestrator orchestrator, WebApiResponse webApiResponse) throws IOException {
        sendQueryEnd(orchestrator, webApiResponse, false);
    }

    static void sendQueryEnd(Orchestrator orchestrator, WebApiResponse webApiResponse, boolean afterError) throws IOException {
        /**
         * Difference in protocols
         * - v2 never sends a queryEnd after a queryError
         */
        if(orchestrator.getProtocolVersion() == Orchestrator.ProtocolVersion.v1 || !afterError) {
            NcsResponse ncsResponse = new NcsResponse();
            ncsResponse.message = "query_end";
            ncsResponse.transaction_id = Integer.parseInt((String) webApiResponse.command.get("id"));

            respondToClient(orchestrator, ncsResponse.toJson());
        }
    }

    public static void sendDisconnect(Orchestrator orchestrator, WebApiResponse webApiResponse) throws IOException {
        ExtractedErrors err = extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
        sendDisconnect(orchestrator, err.code, err.message);
    }

    public static void sendDisconnect(Orchestrator orchestrator, int code, String message) throws IOException {
        HashMap<String, Object> ncsResponse = new HashMap<>();
        ncsResponse.put("message", "disconnect");
        if (message == null || message.isBlank()) {
            message = "transaction error.";
        }
        ncsResponse.put("reason", message);
        ncsResponse.put("code",  code);

        if (orchestrator.getWebapiSessionID() != null) {
            orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, ncsResponse));
        }  else {
            respondToClient(orchestrator, NcsResponse.fromMap(ncsResponse).toJson());
        }
    }

    AbstractCommandTranslator.ExecutionResult doFinal(Orchestrator orchestrator, WebApiResponse webApiResponse) throws IOException {
        AbstractCommandTranslator.ExecutionResult result = new AbstractCommandTranslator.ExecutionResult(AbstractCommandTranslator.ExecutionStatus.SUCCESS, true);
        if (webApiResponse.isFinal()) {
            if (orchestrator.getProtocolVersion() == Orchestrator.ProtocolVersion.v1) {
                if (!webApiResponse.isSuccess() && !webApiResponse.isNoInput() && !webApiResponse.isNoMatch() && !webApiResponse.isCanceled()) {
                    result.setStatus(AbstractCommandTranslator.ExecutionStatus.FAILED);
                }
            } else if (orchestrator.getProtocolVersion() == Orchestrator.ProtocolVersion.v2) {
                if (webApiResponse.isSuccess() || webApiResponse.isNoInput() || webApiResponse.isNoMatch()) {
                    sendDisconnect(orchestrator, 0, "Transaction completed.");
                } else if (webApiResponse.isCanceled()) {
                    sendDisconnect(orchestrator, -1, "Transaction cancelled.");
                } else {
                    sendDisconnect(orchestrator, webApiResponse);
                    result.setStatus(AbstractCommandTranslator.ExecutionStatus.FAILED);
                }
            }
            result.setDone(true);
            return result;
        }
        result.setDone(false);
        return result;
    }

    public static void sendToWebapi(Orchestrator orchestrator, String message, boolean async) throws IOException {
        if (async) {
            executor.submit(
                    () -> {
                        try {
                            orchestrator.getWebApi().onMessage(orchestrator.getWebSocketSession(), message);
                        } catch (IOException e) {
                            logger.warn(e);
                        }
                    }
            );
        } else {
            orchestrator.getWebApi().onMessage(orchestrator.getWebSocketSession(), message);
        }
    }

    static class ExtractedErrors {
        String message;
        int code;
    }

    public static  ExtractedErrors extractWebApiErrors(List<HashMap<String, Object>> errors) {
        StringBuilder response = new StringBuilder();
        ExtractedErrors result = new ExtractedErrors();
        result.code = -1;

        if (errors != null) {
            for (HashMap<String, Object> error : errors) {
                int code = (int) error.get("code");
                if (code == 1205) {
                    result.code = 401;
                }

                response.append(code).append(": ");
                response.append((String) error.get("message")).append(" ");
            }
        } else {
            logger.error("WebAPI response has no error codes!");
            response.append("Internal server error");
        }

        result.message = response.toString().strip();
        return result;
    }

    public static String capitalise(String str) {
        if (str == null) {
            return null;
        } else {
            return str.length() == 0 ? "" : Character.toTitleCase(str.charAt(0)) + str.substring(1, str.length());
        }
    }

    protected WebApiResponse createDummyWebapiResponse(NcsRequest ncsRequest) {
        WebApiResponse response = new WebApiResponse();
        response.command = new HashedMap();
        response.command.put("id", ncsRequest.transaction_id);
        response.type = "result";
        response.result = new HashedMap();
        response.result.put("final", true);
        response.result.put("status", "success");

        response.sessionId = orchestrator.getWebapiSessionID();

        return response;
    }

    protected String getRequestOrQueryParam(String requestVal, String paramName) {
        if (requestVal != null && !requestVal.isEmpty()) {
            return(requestVal);

        }

        Map<String, List<String>> requestParameters = orchestrator.getWebSocketSession().getRequestParameterMap();
        if (!requestParameters.isEmpty() &&
                requestParameters.containsKey(paramName) &&
                requestParameters.get(paramName).size() == 1) {
            return(requestParameters.get(paramName).get(0));
        }

        return null;
    }

    public void setSubscriberId(NcsRequest queryBegin) {
        ncsRequest.subscriber_id = queryBegin.subscriber_id;
    }

    public void setDeviceModel(NcsRequest queryBegin) {
        ncsRequest.phone_model = queryBegin.phone_model;
        ncsRequest.phoneModel = queryBegin.phoneModel;
        ncsRequest.deviceModel = queryBegin.deviceModel;
    }
}

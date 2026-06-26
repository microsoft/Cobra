package com.nuance.plugin.kryptonws.methods;

import com.nuance.cobra.utils.encryption.Utils;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.kryptonws.KryptonWsPlugin;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsResponse;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;
import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_LOG_AUDIO;

public abstract class KryptonWsMethod {
    private static final Logger logger = LogManager.getLogger();

    public enum ProtocolVersion {
        V1_0("1.0"),
        UNSUPPORTED("unsupported");
        public static final List<ProtocolVersion> supported = List.of(V1_0);

        String versionValue;
        ProtocolVersion(String version) {
            this.versionValue = version;
        }

        public static ProtocolVersion fromString(String version) {
            for (ProtocolVersion protVersion: ProtocolVersion.values()) {
                if (protVersion.versionValue.equalsIgnoreCase(version)) {
                    return protVersion;
                }
            }
            return UNSUPPORTED;
        }

        @Override
        public String toString() {
            return versionValue;
        }
    }

    protected JSONObject methodJson;
    protected String kryptonWsRequestId;
    protected String methodName = null;
    protected ProtocolVersion version = ProtocolVersion.V1_0;
    protected boolean sentFinalResponse = false;
    protected List<Map<String, String>> errors = new ArrayList<>();
    protected KryptonWsSession kryptonWsSession;
    protected KryptonWsResponseBuilder kryptonWsResponseBuilder;
    protected Calendar startTime;
    protected boolean areMetricsEnabled;

    private static final String SESSION_OR_REQUEST_ID_PATTERN = "^[0-9a-zA-Z-._@]+$";
    private static final TimeZone timeZone = TimeZone.getTimeZone("UTC");

    protected KryptonWsMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession) {
        if (jsonObject == null) jsonObject = new JSONObject();
        methodJson = jsonObject;
        this.kryptonWsSession = kryptonWsSession;
        methodName = methodJson.optString("method", "");
        setVersion(methodJson.optString("version"));
        parseRequestId();
        this.kryptonWsResponseBuilder = new KryptonWsResponseBuilder(this);
        startTime = getCurrentUTCTime();
        try
        {
            areMetricsEnabled = kryptonWsSession.getPluginConfig()
                                                .getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey());
        }catch (NullPointerException e)
        {
            areMetricsEnabled = (boolean)KRYPTON_WS_ENABLE_METRICS.getDefaultValue();
        }
    }

    private void parseRequestId() {
        var requestId = "requestId";
        try {
            if (methodJson.has(requestId)) {
                kryptonWsRequestId = methodJson.getString(requestId);
            } else {
                kryptonWsRequestId = UUID.randomUUID().toString();
            }
        } catch (JSONException e) {
            this.kryptonWsRequestId = methodJson.get(requestId).toString();
            this.kryptonWsResponseBuilder = new KryptonWsResponseBuilder(this);
            this.kryptonWsResponseBuilder.addError(String.format("%s_%s.requestId should be string", methodName, version), null);
            sendNoMetricsFailedResponse();
            throw e;
        }
    }

    public void addWordset(JSONArray filteredUseWordSets, String wordSetId, String wordsetPkgType) {
        JSONObject sessionObject = kryptonWsSession.getSessionObjects().get(wordSetId);
        JSONObject wordsetObject = kryptonWsSession.getWordsetObjects().get(wordSetId);

        if (wordsetObject != null) {
            String type = wordsetObject.optString("type");
            if(!type.equals(wordsetPkgType)){
                filteredUseWordSets.put(wordSetId);
            }
        }
        else if(sessionObject != null) {
            filteredUseWordSets.put(wordSetId);
        }
    }

    protected void onAudioImplementation(byte[] audio) {
        KryptonWsPlugin.handleStrayAudio(kryptonWsSession, audio);
    }

    public abstract void processWebapiResponse(String webapiResponse);

    protected abstract Logger getLogger();

    public void process()
    {
        this.kryptonWsResponseBuilder.setSessionId(getKryptonWsSessionId());
    }

    public String getKryptonWsSessionId() {
        return kryptonWsSession.getKryptonWsSessionId();
    }

    public String getKryptonWsRequestId() {
        return kryptonWsRequestId;
    }

    public void setKryptonWsRequestId(String kryptonWsRequestId) {
        this.kryptonWsRequestId = kryptonWsRequestId;
    }

    public String getMethodName() {
        return methodName;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public void setVersion(String newVersion) {
        setVersion(ProtocolVersion.fromString(newVersion));
    }

    public void setVersion(ProtocolVersion newVersion) {
        version = newVersion;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public KryptonWsSession getKryptonWsSession() {
        return kryptonWsSession;
    }

    protected void sendInitialResponse(boolean success) {
        if (sentFinalResponse) {
            return;
        }

        kryptonWsResponseBuilder.setStatus(success ? KryptonWsMessage.STATUS.IN_PROGRESS : KryptonWsMessage.STATUS.FAILED);
        if (!kryptonWsResponseBuilder.hasMetrics() && areMetricsEnabled) {
            kryptonWsResponseBuilder.setMetrics(buildCommonMetrics());
        }
        kryptonWsSession.getKryptonWsResponseSender().sendResponse(kryptonWsResponseBuilder.build().toJson());
    }

    public void onAudio(byte[] audio) {
        onAudioImplementation(audio);
    }

    public void sendToWebapi(String message) {
        sendToWebapi(message, this);
    }

    public void sendToWebapi(String message, KryptonWsMethod method) {
        kryptonWsSession.getWebapiResponseListener().setCurrentMethod(method);

        Runnable task = () ->
        {
            try {
                kryptonWsSession.initWebapiWsSession();
                kryptonWsSession.getWebApiEndpoint()
                        .onMessage(kryptonWsSession.getWebapiWsSession(), message);
            } catch (IOException e) {
                getLogger().warn("Failed to open and send message to webapi: ", e);
            }
        };
        getKryptonWsSession().getExecutorService().submit(task);
    }

    public void sendAudioToWebapi(byte[] audio) {
        boolean logAudio = kryptonWsSession.getPluginConfig().getBoolean(KRYPTON_WS_LOG_AUDIO.getKey());

        if (logAudio) getLogger().info("Audio size: {} bytes", audio.length);

        Runnable task = () ->
        {
            try {
                kryptonWsSession.getWebApiEndpoint()
                        .onBinary(kryptonWsSession.getWebapiWsSession(), audio);
            } catch (IOException e) {
                getLogger().warn("Failed to send audio to webapi: ", e);
            }
        };
        getKryptonWsSession().getExecutorService().submit(task);
    }

    //Ensures that a KryptonWs session and KryptonWs Websocket session are bound
    //So that a different KryptonWs Websocket session can't send methods to a different KryptonWs Session.
    public boolean isKryptonWsSessionIdAssociatedToThisKryptonWsWsSession() {
        String kryptonWsSessionIdInMethod = getMethodKryptonWsSessionId();
        return kryptonWsSession.getKryptonWsSessionId().equals(kryptonWsSessionIdInMethod);
    }

    public String getMethodKryptonWsSessionId() {
        return methodJson.optString("sessionId");
    }

    public String getMethodKryptonWsSessionIdOrGenerate() {
        return methodJson.optString("sessionId", Utils.getRandomString(16));
    }

    public void sendErrorResponseFromWebapiResponseObject(WebApiResponse response) {
        var errorsJson = new JSONObject(response.result);
        if (errorsJson.has("errors")) {
            var errorsArray = errorsJson.getJSONArray("errors");

            for (Object obj : errorsArray) {

                JSONObject objectAsJson = (JSONObject) obj;

                if (!objectAsJson.has("message"))
                    continue;

                kryptonWsResponseBuilder.addError(objectAsJson.getString("message"), objectAsJson.optString("code", ""));

            }

        }
        sendResponse(KryptonWsMessage.STATUS.FAILED);
    }

    public void sendErrorResponse(String errorMessage, String extra) {
        kryptonWsResponseBuilder.addError(errorMessage, extra);
        sendResponse(KryptonWsMessage.STATUS.FAILED);
    }

    public void sendCompleteResponse() {
        sendResponse(KryptonWsMessage.STATUS.COMPLETE);
    }

    public void sendNoActionResponse() {
        sendResponse(KryptonWsMessage.STATUS.NO_ACTION);
    }

    public void sendNoMetricsFailedResponse(){
        kryptonWsResponseBuilder.setStatus(KryptonWsMessage.STATUS.FAILED);
        kryptonWsSession.getKryptonWsResponseSender().sendResponse(kryptonWsResponseBuilder.build().toJson());
    }

    public void sendResponse(KryptonWsMessage.STATUS status) {
        sentFinalResponse = true;
        if (!kryptonWsResponseBuilder.hasMetrics() &&
                areMetricsEnabled) {
            kryptonWsResponseBuilder.setMetrics(buildCommonMetrics());
        }
        kryptonWsResponseBuilder.setStatus(status);
        KryptonWsResponse kryptonWsResponse = kryptonWsResponseBuilder.build();
        kryptonWsSession.getKryptonWsResponseSender().sendResponse(kryptonWsResponse.toJson());
    }

    protected Map<String, Object> buildCommonMetrics() {
        return buildCommonMetrics(startTime, getCurrentUTCTime());
    }

    protected static Map<String, Object> buildCommonMetrics(Calendar startTime, Calendar now) {
        DateFormat utcDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamps", List.of(utcDateFormat.format(startTime.getTime()), utcDateFormat.format(now.getTime())));
        metrics.put("elapsed", now.getTimeInMillis() - startTime.getTimeInMillis());
        metrics.put("cpu", Map.of("user", 0, "sys", 0));
        return metrics;
    }

    protected void handleTypeMismatchError(JSONException jsonE) {
        this.getLogger().warn("There was an exception while processing Recognition Parameters: {}", jsonE.getMessage());

        String[] errorStringArr = jsonE.getMessage().split("\""); // for error message formatting only

        sendErrorResponse(errorStringArr[1] + errorStringArr[2].replace("]", ""), null);
    }

    public void cleanup() {
        if (kryptonWsSession.getWebapiResponseListener() != null) {
            kryptonWsSession.getWebapiResponseListener().setCurrentMethod(null);
        }
    }

    public boolean validateKryptonSessionEstablished() {
        //For most methods, we validate when the Krypton Session has been established already
        return kryptonWsSession.isKryptonSessionEstablished();
    }

    public void resetKryptonSession()
    {
        kryptonWsSession.resetKryptonSession();
        kryptonWsSession = null;
    }

    public boolean isValidSessionAndRequestId() {
        return kryptonWsRequestId.matches(SESSION_OR_REQUEST_ID_PATTERN) &&
               getMethodKryptonWsSessionIdOrGenerate().matches(SESSION_OR_REQUEST_ID_PATTERN);
    }

    public boolean canRunWhile(KryptonWsMethod commandThatIsAlreadyRunning) {
        return commandThatIsAlreadyRunning == null;
    }

    protected static Calendar getCurrentUTCTime() {
        return Calendar.getInstance(timeZone);
    }
}

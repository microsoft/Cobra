package com.nuance.plugin.kryptonws.response;

import com.nuance.cobra.utils.krypton.KryptonConverter;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.kryptonws.methods.KryptonWsMethod;
import org.json.JSONObject;

import java.util.*;

public class KryptonWsEventBuilder
{

    private KryptonWsEvent.EVENT event;
    private String method;
    private String version;
    private String sessionId;
    private String requestId;
    private String reason;
    private Map<String, Object> interpretationData;
    private Map<String, Object> interpretationMetrics;
    private String interpretationStatus;
    private List<Map<String, Object>> interpretationErrors;

    private static final String VERBOSE_RESULT = "verboseResult";

    private Map<String, Object> data = new HashMap<>();
    private List nBest = new ArrayList<>();
    private Map<String, Object> utteranceInfo = new HashMap<>();
    private Map<String, Object> metrics;

    public KryptonWsEvent build() {
        KryptonWsEvent myEvent = new KryptonWsEvent();
        myEvent.event = (this.event != null) ? this.event.eventString : null; // else fails from null pointer since we're trying to get null.event
        myEvent.method = this.method;
        myEvent.version = this.version;
        myEvent.sessionId = this.sessionId;
        myEvent.requestId = this.requestId;
        myEvent.reason = this.reason;
        myEvent.metrics = this.metrics;

        myEvent.data = this.data;

        myEvent.interpretationData = this.interpretationData;
        myEvent.interpretationMetrics = this.interpretationMetrics;
        myEvent.interpretationStatus = this.interpretationStatus;
        myEvent.interpretationErrors = this.interpretationErrors;

        return myEvent;
    }

    public KryptonWsEventBuilder setEvent(KryptonWsEvent.EVENT event) {
        this.event = event;
        return this;
    }

    public KryptonWsEventBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public KryptonWsEventBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public KryptonWsEventBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public KryptonWsEventBuilder setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public KryptonWsEventBuilder setnBest(List nBest) {
        this.nBest = nBest;
        return this;
    }

    public KryptonWsEventBuilder setUtteranceInfo(Map<String, Object> utteranceInfo) {
        this.utteranceInfo = utteranceInfo;
        return this;
    }

    public KryptonWsEventBuilder setCause(KryptonWsMessage.COMPLETION_CAUSE cause) {
        this.data.put("completion_cause", cause.cause);
        return this;
    }

    public KryptonWsEventBuilder setStatus(KryptonWsMessage.STATUS status) {
        this.data.put("status", status.statusString.toUpperCase());
        return this;
    }

    public KryptonWsEventBuilder setUtteranceComplete(boolean utteranceComplete) {
        this.data.put("utterance_complete", utteranceComplete);
        return this;
    }

    public KryptonWsEventBuilder setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
        return this;
    }

    public KryptonWsEventBuilder setAbsStart(long absStart) {
        this.data.put("absStart", absStart);
        return this;
    }

    public KryptonWsEventBuilder setAbsEnd(long absEnd) {
        this.data.put("absEnd", absEnd);
        return this;
    }

    public KryptonWsEventBuilder setReason(String reason){
        this.reason = reason;
        return this;
    }

    public KryptonWsEventBuilder setInterpretationData(Map<String, Object> interpretationData)
    {
        this.interpretationData = interpretationData;
        return this;
    }

    public KryptonWsEventBuilder setInterpretationMetrics(Map<String, Object> interpretationMetrics)
    {
        this.interpretationMetrics = interpretationMetrics;
        return this;
    }

    public KryptonWsEventBuilder setInterpretationStatus(String interpretationStatus)
    {
        this.interpretationStatus = interpretationStatus;
        return this;
    }

    public KryptonWsEventBuilder setInterpretationErrors(List<Map<String, Object>> interpretationErrors)
    {
        this.interpretationErrors = interpretationErrors;
        return this;
    }

    /**
     * Method which parses a WebapiResponse object and does a 1-to-1 mapping to a KryptonWsEvent object.
     * @param response Given WebapiResponse object.
     * @return A KryptonWsEventBuilder object with set nBest.
     */
    public KryptonWsEventBuilder setObjectsFromWebapiResponse(WebApiResponse response, boolean putUtteranceInfo) {
        JSONObject verboseResultJson = new JSONObject((LinkedHashMap) response.result.get(VERBOSE_RESULT));
        this.data = KryptonConverter.convertVerboseResultToKryptonWs(verboseResultJson).toMap();

        if (!putUtteranceInfo)
        {
            this.data.remove("utterance_info");
        }
        return this;
    }

    /**
     * Returns a more or less complete KryptonEventBuilder from a WebapiResponse object.
     * The KryptonWsEventBuilder which is returned can be further modified.
     * @param response Webapi response object to parse
     * @return A KryptonWsEventBuilder.
     */
    public static KryptonWsEventBuilder buildSuccessFinalResultFromResponse(WebApiResponse response) {
        KryptonWsEventBuilder builder = new KryptonWsEventBuilder()
                .setObjectsFromWebapiResponse(response, true)
                .setStatus(KryptonWsMessage.STATUS.COMPLETE)
                .setMethod("Event")
                .setVersion("1.0")
                .setSessionId(response.sessionId)
                .setCause(KryptonWsMessage.COMPLETION_CAUSE.fromResponse(response));

        if ((response.isFinal())) {
            builder.setEvent(KryptonWsEvent.EVENT.FINAL_RESULT);
        } else {
            builder.setEvent(KryptonWsEvent.EVENT.PARTIAL_RESULT);
        }

        return builder;
    }

    public static KryptonWsEventBuilder buildBaseEvent(KryptonWsEvent.EVENT event, String sessionId) {
        return new KryptonWsEventBuilder()
                .setMethod("Event")
                .setEvent(event)
                .setVersion("1.0")
                .setSessionId(sessionId);
    }

    public static KryptonWsEventBuilder buildEventForMethod(KryptonWsEvent.EVENT event, KryptonWsMethod method) {
        return buildBaseEvent(event, method.getKryptonWsSessionId()).setRequestId(method.getKryptonWsRequestId());
    }

    public static KryptonWsEventBuilder buildEndOfSessionEvent(String sessionId, String reason) {
        return buildBaseEvent(KryptonWsEvent.EVENT.END_OF_SESSION, sessionId).setReason(reason);
    }

    public Map<String, Object> getData()
    {
        return data;
    }
}

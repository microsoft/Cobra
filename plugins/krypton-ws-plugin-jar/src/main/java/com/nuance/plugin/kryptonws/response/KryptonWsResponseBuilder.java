package com.nuance.plugin.kryptonws.response;

import com.nuance.plugin.kryptonws.methods.KryptonWsMethod;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KryptonWsResponseBuilder
{

    private static final String RESPONSE = "Response";

    private final String method;
    private String responseTo;
    private String version = "1.0";
    private String sessionId;
    private final String requestId;

    private String cause;
    private KryptonWsMessage.STATUS status;

    private List<Map<String, String>> errors;
    private List<Object> sessionObjects;
    private Map<String, Object> metrics;
    private List<Object> objects;
    private Map<String, Object> clientData;
    private Map<String, Object> data;


    public KryptonWsResponseBuilder(KryptonWsMethod method) {
        this.method = RESPONSE;
        this.responseTo = method.getMethodName();
        this.sessionId = method.getKryptonWsSessionId() != null ? method.getKryptonWsSessionId() : method.getMethodKryptonWsSessionIdOrGenerate();
        this.requestId = method.getKryptonWsRequestId();
    }

    public KryptonWsResponseBuilder(String responseTo, String sessionId, String requestId)
    {
        this.method = RESPONSE;
        this.responseTo = responseTo;
        this.sessionId = sessionId;
        this.requestId = requestId;
    }

    public static Map<String, String> buildError(String message, String extra)
    {
        Map<String, String> rc = new HashMap<>();
        rc.put("message", message);
        if (extra != null) rc.put("extra", extra);
        return rc;
    }

    public KryptonWsResponse build() {
        KryptonWsResponse response = new KryptonWsResponse();
        response.method = this.method;
        response.responseTo = this.responseTo;
        response.version = this.version;
        response.sessionId = this.sessionId;
        response.requestId = this.requestId;

        response.status = this.status.statusString;
        response.cause = this.cause;

        response.errors = this.errors;
        response.clientData = this.clientData;
        response.data = this.data;
        response.metrics = this.metrics;
        response.objects = this.objects;
        response.sessionObjects = this.sessionObjects;

        return response;
    }

    public KryptonWsResponseBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public KryptonWsResponseBuilder setCompletionCause(KryptonWsMessage.COMPLETION_CAUSE cause) {
        this.cause = cause.cause;
        return this;
    }

    public KryptonWsResponseBuilder addError(String message, String extra) {
        addError(buildError(message, extra));
        return this;
    }

    public KryptonWsResponseBuilder addError(Map<String, String> error)
    {
        if(this.errors == null) this.errors = new ArrayList<>();
        this.errors.add(error);
        return this;
    }

    public KryptonWsResponseBuilder addErrors(List<Map<String, String>> errors)
    {
        for (Map<String, String> error : errors)
        {
            addError(error);
        }
        return this;
    }

    public KryptonWsResponseBuilder setErrors(List<Map<String, String>> newErrors){
        this.errors = newErrors;
        return this;
    }

    public KryptonWsResponseBuilder setSessionObjects(List<Object> sessionObjects) {
        this.sessionObjects = sessionObjects;
        return this;
    }

    public KryptonWsResponseBuilder setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
        return this;
    }

    public KryptonWsResponseBuilder setObjects(List<Object> objects) {
        this.objects = objects;
        return this;
    }

    public KryptonWsResponseBuilder setClientData(Map<String, Object> clientData) {
        this.clientData = clientData;
        return this;
    }

    public KryptonWsResponseBuilder setStatus(KryptonWsMessage.STATUS status) {
        this.status = status;
        return this;
    }

    public KryptonWsResponseBuilder setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public KryptonWsResponseBuilder setResponseTo(String responseTo)
    {
        this.responseTo = responseTo;
        return this;
    }

    public boolean hasMetrics()
    {
        return metrics != null && metrics.size() > 0;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebapiUploadDynamicVocabularyCommandBuilder extends WebapiCommandBuilder
{
    private static final Logger logger = LogManager.getLogger(WebapiUploadDynamicVocabularyCommandBuilder.class);

    private final String id;

    private JSONArray dynamicVocabularies;
    private String sessionId;
    private String sensitivity;
    private Boolean statistics;

    public WebapiUploadDynamicVocabularyCommandBuilder(String sessionId, String requestId)
    {
        super("command", "uploadDynamicVocabulary");
        this.sessionId = sessionId;
        this.id = requestId;
    }

    public JSONObject buildRequest()
    {
        JSONObject rc = new JSONObject();
        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();

        rc.put("type", type);
        rc.put("command", command);
        rc.put("sessionId", sessionId);

        command.put("name", name);
        command.put("id", id);
        command.put("parameters", parameters);

        parameters.put("dynamicVocabularies", dynamicVocabularies);
        parameters.put("sensitivity", sensitivity);
        parameters.put("statistics", statistics);

        logger.info("Built {}", rc);
        return rc;
    }

    public WebapiUploadDynamicVocabularyCommandBuilder dynamicVocabularies(JSONArray dynamicVocabularies)
    {
        this.dynamicVocabularies = dynamicVocabularies;
        return this;
    }

    public WebapiUploadDynamicVocabularyCommandBuilder sessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public WebapiUploadDynamicVocabularyCommandBuilder sensitivity(String sensitivity)
    {
        this.sensitivity = sensitivity;
        return this;
    }

    public WebapiUploadDynamicVocabularyCommandBuilder statistics(Boolean statistics)
    {
        this.statistics = statistics;
        return this;
    }
}

package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebapiInterpretationCommandBuilder extends WebapiCommandBuilder
{
    private static Logger logger = LogManager.getLogger(WebapiInterpretationCommandBuilder.class);

    private String sessionId;
    private String id;

    private Object data;
    private String dataType;
    private String dialogModelApplicationVersion;
    private String sensitivity;
    private String[] activeDynamicVocabularySets;
    private boolean statistics;
    private String language;
    private boolean interpretationVerboseResult;

    public WebapiInterpretationCommandBuilder(String sessionId, String requestId)
    {
        super("command", "interpretation");
        this.sessionId = sessionId;
        id = requestId;
    }

    public JSONObject buildRequest()
    {
        JSONObject rc = new JSONObject();
        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();

        rc.put("type", type);
        rc.put("sessionId", sessionId);
        rc.put("command", command);

        command.put("name", name);
        command.put("id", id);
        command.put("parameters", parameters);

        parameters.put("data", data);
        parameters.put("type", dataType);
        parameters.put("dialogModelApplicationVersion", dialogModelApplicationVersion);
        parameters.put("sensitivity", sensitivity);
        parameters.put("interpretationVerboseResult", interpretationVerboseResult);

        if (activeDynamicVocabularySets != null && activeDynamicVocabularySets.length > 0)
        {
            parameters.put("activeDynamicVocabularySets", new JSONArray(activeDynamicVocabularySets));
        }
        parameters.put("statistics", statistics);
        parameters.put("language", language);

        return rc;
    }

    public WebapiInterpretationCommandBuilder data(String data)
    {
        this.data = data;
        return this;
    }

    public WebapiInterpretationCommandBuilder data(JSONObject data)
    {
        this.data = data;
        return this;
    }

    public WebapiInterpretationCommandBuilder type(String dataType)
    {
        this.dataType = dataType;
        return this;
    }

    public WebapiInterpretationCommandBuilder dialogModelApplicationVersion(String dialogModelApplicationVersion)
    {
        this.dialogModelApplicationVersion = dialogModelApplicationVersion;
        return this;
    }

    public WebapiInterpretationCommandBuilder sensitivity(String sensitivity)
    {
        this.sensitivity = sensitivity;
        return this;
    }

    public WebapiInterpretationCommandBuilder activeDynamicVocabularySet(String[] activeDynamicVocabularySet)
    {
        this.activeDynamicVocabularySets = activeDynamicVocabularySet;
        return this;
    }

    public WebapiInterpretationCommandBuilder statistics(boolean statistics)
    {
        this.statistics = statistics;
        return this;
    }

    public WebapiInterpretationCommandBuilder language(String language)
    {
        this.language = language;
        return this;
    }

    public WebapiInterpretationCommandBuilder interpretationVerboseResult(boolean interpretationVerboseResult)
    {
        this.interpretationVerboseResult = interpretationVerboseResult;
        return this;
    }
}

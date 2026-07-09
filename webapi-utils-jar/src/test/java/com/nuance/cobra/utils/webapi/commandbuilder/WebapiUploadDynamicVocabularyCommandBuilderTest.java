package com.nuance.cobra.utils.webapi.commandbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebapiUploadDynamicVocabularyCommandBuilderTest
{
    WebapiUploadDynamicVocabularyCommandBuilder webapiUploadDynamicVocabularyCommandBuilder;

    @Before
    public void setUp()
            throws Exception
    {
        webapiUploadDynamicVocabularyCommandBuilder = new WebapiUploadDynamicVocabularyCommandBuilder("sessionId", "id");
    }

    @Test
    public void build1()
    {
        webapiUploadDynamicVocabularyCommandBuilder.dynamicVocabularies(new JSONArray("[{\"number\": \"18\"}]"));
        webapiUploadDynamicVocabularyCommandBuilder.sensitivity("19");
        webapiUploadDynamicVocabularyCommandBuilder.statistics(true);

        JSONObject dynamicVocabCommand = webapiUploadDynamicVocabularyCommandBuilder.buildRequest();
        assertEquals("uploadDynamicVocabulary", dynamicVocabCommand.getJSONObject("command").getString("name"));
        assertEquals("[{\"number\":\"18\"}]", dynamicVocabCommand.getJSONObject("command").getJSONObject("parameters").getJSONArray("dynamicVocabularies").toString());
    }

    @Test(expected = JSONException.class)
    public void build2()
    {
        JSONObject startSessionCommand = webapiUploadDynamicVocabularyCommandBuilder.buildRequest();
        startSessionCommand.getJSONObject("command").getJSONObject("parameters").getString("speechRecognitionCodec");
    }
}

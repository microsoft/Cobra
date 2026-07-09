package com.nuance.cobra.utils.webapi.commandbuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebapiStopDirectiveBuilderTest {
    WebapiStopDirectiveBuilder webapiStopDirectiveBuilder;

    @Before
    public void setUp()
            throws Exception
    {
        webapiStopDirectiveBuilder = new WebapiStopDirectiveBuilder("sessionId", "commandId", "speechRecognition");
    }

    @Test
    public void build1()
    {
        JSONObject dynamicVocabCommand = webapiStopDirectiveBuilder.buildRequest();
        assertEquals("speechRecognition", dynamicVocabCommand.getJSONObject("command").getString("name"));
        assertEquals("stop", dynamicVocabCommand.getJSONObject("directive").getString("name"));
    }

    @Test(expected = JSONException.class)
    public void build2()
    {
        JSONObject startSessionCommand = webapiStopDirectiveBuilder.buildRequest();
        startSessionCommand.getJSONObject("command").getJSONObject("parameters").getString("speechRecognitionCodec");
    }
}

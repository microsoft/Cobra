package com.nuance.cobra.utils.webapi.commandbuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebapiStartSessionCommandBuilderTest
{
    WebapiStartSessionCommandBuilder webapiStartSessionCommandBuilder;

    @Before
    public void setUp()
            throws Exception
    {
        webapiStartSessionCommandBuilder = new WebapiStartSessionCommandBuilder("id", "app123", "hashcode", "12345");
    }

    @Test
    public void build1()
    {
        webapiStartSessionCommandBuilder.speechDetector("01");
        webapiStartSessionCommandBuilder.beginNoiseSampleFrames(2);
        webapiStartSessionCommandBuilder.voiceThreshold(3.0);
        webapiStartSessionCommandBuilder.startOfSpeechVoicedFrames(4);
        webapiStartSessionCommandBuilder.startOfSpeechHistoryFrames(5);
        webapiStartSessionCommandBuilder.endOfSpeechVoicedFrames(6);
        webapiStartSessionCommandBuilder.endOfSpeechHistoryFrames(7);
        webapiStartSessionCommandBuilder.considerNegativeRatios(true);
        webapiStartSessionCommandBuilder.speechSynthesisCodec("09");
        webapiStartSessionCommandBuilder.speechRecognitionCodec("10");
        webapiStartSessionCommandBuilder.clientUserId("11");
        webapiStartSessionCommandBuilder.clientDeviceId("12");
        webapiStartSessionCommandBuilder.clientSessionId("13");
        webapiStartSessionCommandBuilder.clientOsName("14");
        webapiStartSessionCommandBuilder.clientOsVersion("15");
        webapiStartSessionCommandBuilder.clientCarrier("16");
        webapiStartSessionCommandBuilder.clientPhoneModel("17");
        webapiStartSessionCommandBuilder.clientDetails(new JSONObject("{\"number\": \"18\"}"));
        webapiStartSessionCommandBuilder.sensitivity("19");
        webapiStartSessionCommandBuilder.statistics(true);

        JSONObject startSessionCommand = webapiStartSessionCommandBuilder.buildRequest();
        assertEquals("startSession", startSessionCommand.getJSONObject("command").getString("name"));
        assertEquals("10", startSessionCommand.getJSONObject("command").getJSONObject("parameters").getString("speechRecognitionCodec"));
    }

    @Test(expected = JSONException.class)
    public void build2()
    {
        JSONObject startSessionCommand = webapiStartSessionCommandBuilder.buildRequest();
        startSessionCommand.getJSONObject("command").getJSONObject("parameters").getString("speechRecognitionCodec");
    }
}
package com.nuance.cobra.utils.webapi.commandbuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebapiStartRecognitionTimersDirectiveBuilderTest
{
    WebapiStartRecognitionTimersDirectiveBuilder webapiStartRecognitionTimersDirectiveBuilder;

    @Before
    public void setUp()
            throws Exception
    {
        webapiStartRecognitionTimersDirectiveBuilder = new WebapiStartRecognitionTimersDirectiveBuilder("abc123", "2", "speechRecognition");
    }

    @Test
    public void sessionId()
    {
        assertNotNull(webapiStartRecognitionTimersDirectiveBuilder.sessionId("cba321"));
    }


}
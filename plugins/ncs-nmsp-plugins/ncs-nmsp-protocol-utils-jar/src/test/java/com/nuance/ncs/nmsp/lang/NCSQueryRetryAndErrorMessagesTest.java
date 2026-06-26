package com.nuance.ncs.nmsp.lang;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NCSQueryRetryAndErrorMessagesTest
{
    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new NCSQueryRetryAndErrorMessages();
    }

    @Test
    public void getRecNoInputTimeoutPrompt()
    {
        String rc = NCSQueryRetryAndErrorMessages.getRecNoInputTimeoutPrompt(Map.of("dictation_language", "por-BRA"));
        assertEquals("Nenhuma fala detectada. Por favor tente novamente.", rc);
    }
}
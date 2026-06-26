package com.nuance.cobra.client;

import org.junit.Test;

import static org.junit.Assert.*;

public class InterpretationClientsTest
{
    @Test
    public void isInterpretationClient()
    {
        assertTrue(InterpretationClients.isInterpretationClient(InterpretationClients.NLU_GRPC.getValue()));

        assertTrue(InterpretationClients.isInterpretationClient(InterpretationClients.NLU_CACHE_DB.getValue()));

        assertFalse(SpeechRecognitionClients.isSpeechRecognitionClient("blah"));

        assertFalse(SpeechRecognitionClients.isSpeechRecognitionClient(null));
    }
}
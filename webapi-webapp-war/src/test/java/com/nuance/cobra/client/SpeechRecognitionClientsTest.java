package com.nuance.cobra.client;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpeechRecognitionClientsTest
{
    @Test
    public void isSpeechRecognitionClient()
    {
        assertTrue(SpeechRecognitionClients.isSpeechRecognitionClient(SpeechRecognitionClients.KRYPTON_GRPC.getValue()));

        assertFalse(SpeechRecognitionClients.isSpeechRecognitionClient("blah"));

        assertFalse(SpeechRecognitionClients.isSpeechRecognitionClient(null));
    }
}
package com.nuance.cobra.client;

import java.util.Arrays;

public enum SpeechRecognitionClients
{
    MREC_NIM("mrecnim"),
    KRYPTON_GRPC("krypton-grpc"),
    SPEECH_SERVICE_ASR("speechServiceASR");

    private final String value;

    SpeechRecognitionClients(String client)
    {
        value = client;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return getValue();
    }

    public static String[] getValues()
    {
        return Arrays.stream(SpeechRecognitionClients.class.getEnumConstants()).map(SpeechRecognitionClients::getValue).toArray(String[]::new);
    }

    public static SpeechRecognitionClients fromString(String client)
    {
        if (client == null)
        {
            throw new NullPointerException();
        }

        for (SpeechRecognitionClients toMatch: SpeechRecognitionClients.values()) {
            if (client.equalsIgnoreCase(toMatch.getValue())) {
                return toMatch;
            }
        }

        throw new IllegalArgumentException("Not a known Speech Recognition Engine: " + client);
    }

    public static boolean isSpeechRecognitionClient(String speechRecognitionClient)
    {
        try
        {
            fromString(speechRecognitionClient);
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }
}

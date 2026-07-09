package com.nuance.cobra.utils.audio;

import java.util.Arrays;

public enum SpeechDetectorTypes {
    ADAPTIVE("adaptive"),
    LEGACY("legacy"),
    ENGINE("engine");

    private final String value;

    SpeechDetectorTypes(String endpointing)
    {
        value = endpointing;
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
        return Arrays.stream(SpeechDetectorTypes.class.getEnumConstants()).map(SpeechDetectorTypes::getValue).toArray(String[]::new);
    }

    public static SpeechDetectorTypes fromString(String client)
    {
        if (client == null)
        {
            throw new NullPointerException("Provided string for SpeechDetectorTypes conversion is null");
        }

        for (SpeechDetectorTypes toMatch: SpeechDetectorTypes.values()) {
            if (client.equalsIgnoreCase(toMatch.getValue())) {
                return toMatch;
            }
        }

        throw new IllegalArgumentException("Not a known Speech DetectorTypes type: " + client);
    }

    public static boolean isSpeechDetectorTypes(String speechDetectorType)
    {
        try
        {
            fromString(speechDetectorType);
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }
}

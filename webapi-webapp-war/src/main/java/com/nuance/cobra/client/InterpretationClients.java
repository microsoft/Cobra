package com.nuance.cobra.client;

import java.util.Arrays;

public enum InterpretationClients
{
    NLU_GRPC("nlu-grpc"),
    NLU_CACHE_DB("nlu-cache-db");

    private final String value;

    InterpretationClients(String client)
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
        return Arrays.stream(InterpretationClients.class.getEnumConstants()).map(InterpretationClients::getValue).toArray(String[]::new);
    }

    public static InterpretationClients fromString(String client)
    {
        if (client == null)
        {
            throw new NullPointerException();
        }

        for (InterpretationClients toMatch: InterpretationClients.values()) {
            if (client.equalsIgnoreCase(toMatch.getValue())) {
                return toMatch;
            }
        }

        throw new IllegalArgumentException("Not a known Interpretation Engine: " + client);
    }

    public static boolean isInterpretationClient(String interpretationClient)
    {
        try
        {
            fromString(interpretationClient);
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }
}

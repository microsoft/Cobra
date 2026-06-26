package com.nuance.cobra.client;

import java.util.Arrays;

public enum UserResetClients
{
    MIX("mix");

    private final String value;

    UserResetClients(String client)
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
        return Arrays.stream(UserResetClients.class.getEnumConstants()).map(UserResetClients::getValue).toArray(String[]::new);
    }

    public static UserResetClients fromString(String client)
    {
        if (client == null)
        {
            throw new NullPointerException();
        }

        for (UserResetClients toMatch: UserResetClients.values()) {
            if (client.equalsIgnoreCase(toMatch.getValue())) {
                return toMatch;
            }
        }

        throw new IllegalArgumentException("Not a known UserReset Engine: " + client);
    }
}

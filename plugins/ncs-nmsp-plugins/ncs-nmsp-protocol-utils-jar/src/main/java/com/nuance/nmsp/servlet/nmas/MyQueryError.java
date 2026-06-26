package com.nuance.nmsp.servlet.nmas;

public class MyQueryError
{
    private final int error;
    private final String description;

    public MyQueryError(int error, String description)
    {
        this.error = error;
        this.description = description;
    }

    public int getError()
    {
        return error;
    }

    public String getDescription()
    {
        return description;
    }
}

package com.nuance.cobra.utils;

public class HealthCheckException
        extends Exception
{
    private final String displayMessage;

    public HealthCheckException(String displayMessage)
    {
        super();

        this.displayMessage = displayMessage;
    }

    public HealthCheckException(String displayMessage, Exception cause)
    {
        super(cause);

        this.displayMessage = displayMessage;
    }

    public String getDisplayMessage()
    {
        return displayMessage;
    }

}

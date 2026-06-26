package com.nuance.cobra.session;

public class InvalidConfigurationException extends Exception
{
    public InvalidConfigurationException(String msg)
    {
        super(msg);
    }

    InvalidConfigurationException(String msg, Exception e)
    {
        super(msg, e);
    }

}

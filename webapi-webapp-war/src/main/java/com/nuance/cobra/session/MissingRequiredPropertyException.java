package com.nuance.cobra.session;

class MissingRequiredPropertyException extends InvalidConfigurationException
{
    public MissingRequiredPropertyException(String message)
    {
        super(message);
    }
}

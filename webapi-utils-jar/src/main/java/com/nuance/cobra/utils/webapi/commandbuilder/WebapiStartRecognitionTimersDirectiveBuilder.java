package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebapiStartRecognitionTimersDirectiveBuilder
        extends WebapiDirectiveBuilder
{
    private static final Logger logger = LogManager.getLogger(WebapiStartRecognitionTimersDirectiveBuilder.class);

    public WebapiStartRecognitionTimersDirectiveBuilder(String sessionId, String commandId, String commandName)
    {
        super(sessionId, commandId, commandName, "startRecognitionTimers", logger);
    }
}

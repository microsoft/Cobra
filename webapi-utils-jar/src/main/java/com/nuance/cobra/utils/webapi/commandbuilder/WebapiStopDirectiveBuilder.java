package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebapiStopDirectiveBuilder extends WebapiDirectiveBuilder
{
    private static final Logger logger = LogManager.getLogger(WebapiStopDirectiveBuilder.class);

    public WebapiStopDirectiveBuilder(String sessionId, String commandId, String commandName)
    {
        super(sessionId, commandId, commandName, "stop", logger);
    }
}

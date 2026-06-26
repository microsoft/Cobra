package com.nuance.cobra.utils.logging;

import org.owasp.esapi.Logger;

public class EsapiLogger
{
    EsapiLogger()
    {
        throw new IllegalStateException("Utility class!");
    }

    public static void always(Logger logger, String log)
    {
        logger.always(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void always(Logger logger, String logFormat, Object... args)
    {
        String logString = String.format(logFormat, args);
        always(logger, logString);
    }

    public static void fatal(Logger logger, String log)
    {
        logger.fatal(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void fatal(Logger logger, String logFormat, Object... args)
    {
        if(!logger.isFatalEnabled())
        {
            return;
        }
        String logString = String.format(logFormat, args);
        fatal(logger, logString);
    }

    public static void error(Logger logger, String log)
    {
        logger.error(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void error(Logger logger, String logFormat, Object... args)
    {
        if(!logger.isErrorEnabled())
        {
            return;
        }
        String logString = String.format(logFormat, args);
        error(logger, logString);
    }

    public static void warning(Logger logger, String log)
    {
        logger.warning(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void warning(Logger logger, String logFormat, Object... args)
    {
        if(!logger.isWarningEnabled())
        {
            return;
        }
        String logString = String.format(logFormat, args);
        warning(logger, logString);
    }
    public static void warning(Logger logger, String log, Throwable e)
    {
        logger.warning(Logger.EVENT_UNSPECIFIED, log, e);
    }

    public static void info(Logger logger, String log)
    {
        logger.info(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void info(Logger logger, String logFormat, Object... args)
    {
        if(!logger.isInfoEnabled())
        {
            return;
        }
        String logString = String.format(logFormat, args);
        info(logger, logString);
    }

    public static void debug(Logger logger, String log)
    {
        logger.debug(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void debug(Logger logger, String logFormat, Object... args)
    {
        if(!logger.isDebugEnabled())
        {
            return;
        }
        String logString = String.format(logFormat, args);
        debug(logger, logString);
    }

    public static void trace(Logger logger, String log)
    {
        logger.trace(Logger.EVENT_UNSPECIFIED, log);
    }

    public static void trace(Logger logger, String logFormat, Object... args)
    {
        if(!logger.isTraceEnabled())
        {
            return;
        }
        String logString = String.format(logFormat, args);
        trace(logger, logString);
    }
}

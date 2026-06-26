package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.owasp.esapi.ESAPI;

public class LoggerErrorListener
        implements ErrorListener
{
    private org.apache.logging.log4j.Logger localLogger;
    private org.owasp.esapi.Logger esapiLocalLogger;

    public LoggerErrorListener(org.apache.logging.log4j.Logger logger)
    {
        localLogger = logger;
        esapiLocalLogger = ESAPI.getLogger(logger.getName());
    }

    @Override
    public void notify(IResponse errors)
    {

        if (errors.getErrors() == null)
        {
            return;
        }

        for (ResponseCode error : errors.getErrors())
        {
            if(error.getCode() != ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR.getValue() &&
               error.getCode() != ExternalResponseErrorCode.NO_ACTIVE_COMMAND_ERROR.getValue() )
            {
                EsapiLogger.error(esapiLocalLogger, error.formattedLogMessage());
            }
        }

    }
}

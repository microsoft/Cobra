package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseSenderErrorListener
        implements ErrorListener
{
    private static final Logger logger = LogManager.getLogger(ResponseSenderErrorListener.class);

    private IResponseSender responseSender;

    public ResponseSenderErrorListener(IResponseSender session)
    {
        this.responseSender = session;
    }

    @Override
    public void notify(IResponse error)
    {
        try
        {
            responseSender.sendResponse(error.toJson());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}

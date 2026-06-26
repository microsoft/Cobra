package com.nuance.cobra.utils.response;

import com.nuance.cobra.utils.webapi.ResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;

/**
 * A response sender that does not send the response to the user.
 * This will simply log the result
 */
public class LogResponseSender implements ResponseSender
{
    private static final Logger logger = LogManager.getLogger(LogResponseSender.class);

    /**
     * Sends a string to the user
     *
     * @param response
     *         The string to be sent
     */
    @Override
    public void sendResponse(String response) { logger.trace("LogResponseSender received: {}", response); }

    /**
     * Sends raw data to the user. Typically audio
     *
     * @param data
     *         The audio to be sent
     */
    @Override
    public void sendBinary(byte[] data) { logger.trace("LogResponseSender received data"); }

    @Override
    public void closeConnection(CloseReason closeReason)
    {
        logger.trace("LogResponseSender close connection");
    }
}

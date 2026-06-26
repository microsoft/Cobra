package com.nuance.cobra.utils.webapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Session;

/**
 *
 */
public class WebSocketResponseSender extends AbstractWebsocketResponseSender
{
    private static final Logger logger = LogManager.getLogger(WebSocketResponseSender.class);

    /**
     * Creates the response sender with the appropriate web socket session
     *
     * @param session
     *         The websocket session
     */
    public WebSocketResponseSender(Session session, Boolean closeSession)
    {
        webSocketSession = session;
        this.closeSession = closeSession;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }
}

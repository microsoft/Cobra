package com.nuance.plugin.kryptonws.response;

import com.nuance.cobra.utils.webapi.AbstractWebsocketResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Session;

public class KryptonWsResponseSender
        extends AbstractWebsocketResponseSender
{
    private static final Logger logger = LogManager.getLogger(KryptonWsResponseSender.class);

    public KryptonWsResponseSender(Session kryptonWsWsSession)
    {
        webSocketSession = kryptonWsWsSession;
    }

    @Override
    protected Logger getLogger() { return logger; }
}

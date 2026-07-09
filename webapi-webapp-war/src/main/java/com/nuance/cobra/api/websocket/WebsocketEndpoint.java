package com.nuance.cobra.api.websocket;

import com.nuance.cobra.api.endpoint.AbstractWebApiSessionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.server.ServerEndpoint;

/**
 * This endpoint allows the client application to connect using websockets
 * TODO This name will likely need approval from PO
 */
@ServerEndpoint(value = "/websocket")
public class WebsocketEndpoint extends AbstractWebApiSessionHelper
{
    private static Logger logger = LogManager.getLogger(WebsocketEndpoint.class);

    protected Logger getLogger() {
        return logger;
    }
}

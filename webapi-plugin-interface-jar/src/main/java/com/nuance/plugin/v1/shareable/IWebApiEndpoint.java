package com.nuance.plugin.v1.shareable;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

public interface IWebApiEndpoint {
    /**
     * The on open event for the web api endpoint.
     * @param session the socket session to be used for responses.
     */
    void onOpen(javax.websocket.Session session) throws IOException;

    /**
     * The on message event for the web api endpoint. This event is used for string messages.
     * @param session the socket session to be used for responses.
     * @param message -- key mapped to this configuration object
     */
    void onMessage(javax.websocket.Session session, String message) throws IOException;

    /**
     * The on binary event for the web api endpoint. This event is used for binary blobs.
     * @param session
     * @param buffer
     * @throws Exception
     */
    void onBinary(javax.websocket.Session session, byte[] buffer) throws IOException;

    /**
     * The on pong event for the web api endpoint.
     * @param session
     * @param pongMessage
     * @throws Exception
     */
    void onPong(javax.websocket.Session session, PongMessage pongMessage) throws IOException;

    /**
     * The on error event for the web api endpoint.
     * @param session
     * @param throwable
     * @throws Exception
     */
    void onError(javax.websocket.Session session, Throwable throwable) throws IOException;

    /**
     * The on close event for the web api endpoint.
     * @param session
     * @param reason
     * @throws Exception
     */
    void onClose(javax.websocket.Session session, CloseReason reason) throws IOException;

    /**
     * Get the active webAPI session.
     * @return
     *      the active session
     */
    ISession getActiveSession();

    /**
     * Get the webAPI session object that corresponds to the indicated session ID.
     * @param sessionID
     * @return
     *      the active session
     */
    ISession getWebapiSession(String sessionID);
}

package com.nuance.cobra.api.endpoint;

import com.nuance.cobra.utils.webapi.ResponseSender;
import com.nuance.cobra.utils.webapi.WebSocketResponseSender;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;

public abstract class AbstractWebApiSessionHelper {
    protected WebApiEndpoint wabApi;

    protected abstract Logger getLogger();

    protected void initializeWebApi(ResponseSender responseSender, Logger logger) {
        if (wabApi == null) {
            wabApi = new WebApiEndpoint(responseSender);
        }
    }

    /**
     * When a client application opens a websocket connection
     *
     * @param session
     *         The session associated with the websocket connection
     */
    @OnOpen
    public void onOpen(javax.websocket.Session session) {
        WebSocketResponseSender responseSender = new WebSocketResponseSender(session, false);
        initializeWebApi(responseSender, getLogger());
        wabApi.onOpen(session);
    }

    /**
     * When a client application sends a message to the web socket
     *
     * @param session
     *         The session associated with the websocket connection
     * @param message
     *         The received message on the websocket
     *
     * @throws Exception
     *         An exception
     */
    @OnMessage
    public void onMessage(javax.websocket.Session session, String message)
            throws Exception
    {
        WebSocketResponseSender responseSender = new WebSocketResponseSender(session, false);
        initializeWebApi(responseSender, getLogger());
        wabApi.onMessage(session, message);
    }

    /**
     * When the client application sends audio to the websocket
     *
     * @param buffer
     *         The data received from the client application
     *
     * @throws Exception
     *         An exception
     */
    @OnMessage
    public void onBinary(javax.websocket.Session session, byte[] buffer) {
        WebSocketResponseSender responseSender = new WebSocketResponseSender(session, false);
        initializeWebApi(responseSender, getLogger());
        wabApi.onBinary(session, buffer);
    }

    @OnMessage
    public void onPong(javax.websocket.Session session, PongMessage pongMessage)
            throws Exception
    {
        WebSocketResponseSender responseSender = new WebSocketResponseSender(session, false);
        initializeWebApi(responseSender, getLogger());
        wabApi.onPong(session, pongMessage);
    }

    /**
     * When the websocket connection throws an error
     * If this method is not implemented, we may have this warning:
     * WARN:oejwja.JsrEvents:qtp418304857-19: Unable to report throwable to websocket (no @OnError handler declared)
     *
     * @param session
     *         The session associated with the websocket connection
     * @param error
     *         Error thrown by the websocket
     */
    @OnError
    public void onError(javax.websocket.Session session, Throwable error) {
        WebSocketResponseSender responseSender = new WebSocketResponseSender(session, false);
        initializeWebApi(responseSender, getLogger());
        wabApi.onError(session, error);
    }

    /**
     * When the websocket connection is closed
     *
     * @param session
     *         The session associated with the websocket connection
     * @param reason
     *         The reason why the connection was closed
     */
    @OnClose
    public void onClose(javax.websocket.Session session, CloseReason reason) {
        WebSocketResponseSender responseSender = new WebSocketResponseSender(session, false);
        initializeWebApi(responseSender, getLogger());
        wabApi.onClose(session, reason);
    }
}

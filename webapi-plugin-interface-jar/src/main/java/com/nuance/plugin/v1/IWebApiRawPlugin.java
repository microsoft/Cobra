package com.nuance.plugin.v1;

import javax.servlet.ServletException;
import javax.websocket.PongMessage;
import java.io.IOException;

public interface IWebApiRawPlugin extends IWebApiTenantPlugin {
    /**
     * WebAPI will call this method after initializing the plugin to figure out what URL patterns apply to this plugin.
     * @return  A regex URL filters that this plugin will respond to.
     */
    String getUrlPattern();

    /**
     * Called when the websocket connection is established.
     * @param session
     * @return
     */
    default boolean onSocketOpen(javax.websocket.Session session) throws IOException {
        return false;
    }

    /**
     * Called on a new message arrives on the webSocket
     * @param session Java websocket session object.
     * @param message The message that was received by the socket.
     * @return  Return true if the event was consumed.
     */
    default boolean onSocketMessage(javax.websocket.Session session, String message) throws IOException {
        return false;
    }

    /**
     * Called on a new message arrives on the webSocket
     * @param session Java websocket session object.
     * @param buffer The bytes buffer received by the socket.
     * @return  Return true if the event was consumed.
     */
    default boolean onSocketBinary(javax.websocket.Session session, byte[] buffer) throws IOException {
        return false;
    }

    /**
     * Called on the socket PONG event
     * @param session Java websocket session object.
     * @param pongMessage The bytes buffer received by the socket.
     * @return  Return true if the event was consumed.
     */
    default boolean onSocketPong(javax.websocket.Session session, PongMessage pongMessage) throws IOException {
        return false;
    }

    /**
     * Called when there is an error in handling the socket.
     * @param session Java websocket session object.
     * @param error The error thrown by the socket.
     * @return  Return true if the event was consumed.
     */
    default boolean onSocketError(javax.websocket.Session session, Throwable error) throws IOException {
        return false;
    }

    /**
     * Called when the websocket is closed.
     * @param session Java websocket session object.
     * @param reason The reason why the socket was closed.
     * @return  Return true if the event was consumed.
     */
    default boolean onSocketClosed(javax.websocket.Session session, javax.websocket.CloseReason reason) throws IOException {
        return false;
    }

    /**
     * Called on an HTTP get.
     * @param request   The Http request object.
     * @param response  The object to be used to send the response.
     * @return  Return true if the event was consumed.
     * @throws ServletException
     * @throws IOException
     */
    default boolean onHttpGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException, WebApiCommonException {
        return false;
    }

    /**
     * Called on an HTTP Post.
     * @param request The Http request object.
     * @param response The object to be used to send the response.
     * @return  Return true if the event was consumed.
     * @throws ServletException
     * @throws IOException
     */
    default boolean onHttpPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException, WebApiCommonException {
        return false;
    }

    /**
     * Called on an HTTP Put.
     * @param request The Http request object.
     * @param response The object to be used to send the response.
     * @return  Return true if the event was consumed.
     * @throws ServletException
     * @throws IOException
     */
    default boolean onHttpPut(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException, WebApiCommonException {
        return false;
    }

    /**
     * Called on an HTTP Delete.
     * @param request The Http request object.
     * @param response The object to be used to send the response.
     * @return  Return true if the event was consumed.
     * @throws ServletException
     * @throws IOException
     */
    default boolean onHttpDelete(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException, WebApiCommonException {
        return false;
    }
}

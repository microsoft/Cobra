package com.nuance.cobra.api.websocket;

import com.nuance.cobra.plugin.PluginLoader;
import com.nuance.cobra.plugin.WebApiPlugin;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

/**
 * This endpoint allows the client application to connect using websockets
 */
@ServerEndpoint(value = "/websocket/{tenant}/{pluginEndpoint}")
public class WebsocketPluginsEndpoint
{

    @ServerEndpoint(value = "/websocket/{tenant}/{pluginEndpoint}/")
    public static class WebsocketPluginsExtraEndpoint1 extends WebsocketPluginsEndpoint
    {
        //this class is only used to work around the websocket path URI template not supporting regular expressions.
    }

    @ServerEndpoint(value = "/websocket/{tenant}/{pluginEndpoint}/{pathParam}")
    public static class WebsocketPluginsExtraEndpoint2 extends WebsocketPluginsEndpoint
    {
        //this class is only used to work around the websocket path URI template not supporting regular expressions.
    }

    @ServerEndpoint(value = "/websocket/{tenant}/{pluginEndpoint}/{pathParam}/")
    public static class WebsocketPluginsExtraEndpoint3 extends WebsocketPluginsEndpoint
    {
        //this class is only used to work around the websocket path URI template not supporting regular expressions.
    }


    private static Logger logger = LogManager.getLogger(WebsocketPluginsEndpoint.class);
    List<WebApiPlugin<IWebApiRawPlugin>> plugins = null;

    private boolean callPlugin(String methodName, javax.websocket.Session session, String tenant, String requestPath, Object data) throws IOException {
        if (!WebApiConfigManager.validateTenantKey(tenant)) {
            return false;
        }

        if (plugins == null) {
            try {
                plugins = PluginLoader.loadRawPlugins(tenant);
            } catch (IOException e) {
                logger.warn("Failed to open plugins", e);
                session.close();
                return false;
            }
        }

        for (WebApiPlugin<IWebApiRawPlugin> plugin: plugins) {
            if (requestPath.matches(plugin.getPlugin().getUrlPattern())) {

                switch (methodName) {
                    case "onSocketOpen" :
                        return plugin.getPlugin().onSocketOpen(session);
                    case "onSocketMessage" :
                        return plugin.getPlugin().onSocketMessage(session, (String) data);
                    case "onSocketBinary" :
                        return plugin.getPlugin().onSocketBinary(session, (byte[]) data);
                    case "onSocketPong" :
                        return plugin.getPlugin().onSocketPong(session, (PongMessage) data);
                    case "onSocketClose" :
                        return plugin.getPlugin().onSocketClosed(session, (CloseReason) data);
                    case "onSocketError" :
                        return plugin.getPlugin().onSocketError(session, (Throwable) data);
                }
            }
        }

        return false;
    }

    /**
     * When a client application opens a websocket connection
     *
     * @param session
     *         The session associated with the websocket connection
     */
    @OnOpen
    public void onOpen(javax.websocket.Session session, @PathParam("tenant") String tenant, @PathParam("pluginEndpoint") String pluginEndpoint) throws IOException {
        callPlugin("onSocketOpen", session, tenant, pluginEndpoint, null);
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
    public void onMessage(javax.websocket.Session session, String message, @PathParam("tenant") String tenant, @PathParam("pluginEndpoint") String pluginEndpoint)
            throws Exception
    {
        callPlugin("onSocketMessage", session, tenant, pluginEndpoint, message);
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
    public void onBinary(javax.websocket.Session session, byte[] buffer, @PathParam("tenant") String tenant, @PathParam("pluginEndpoint") String pluginEndpoint) throws IOException {
        callPlugin("onSocketBinary", session, tenant, pluginEndpoint, buffer);
    }

    @OnMessage
    public void onPong(javax.websocket.Session session, PongMessage pongMessage, @PathParam("tenant") String tenant, @PathParam("pluginEndpoint") String pluginEndpoint)
            throws Exception
    {
        callPlugin("onSocketPong", session, tenant, pluginEndpoint, pongMessage);
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
    public void onError(javax.websocket.Session session, Throwable error, @PathParam("tenant") String tenant, @PathParam("pluginEndpoint") String pluginEndpoint) throws IOException {
        callPlugin("onSocketError", session, tenant, pluginEndpoint, error);
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
    public void onClose(javax.websocket.Session session, CloseReason reason, @PathParam("tenant") String tenant, @PathParam("pluginEndpoint") String pluginEndpoint) throws IOException {
        callPlugin("onSocketClose", session, tenant, pluginEndpoint, reason);
    }
}

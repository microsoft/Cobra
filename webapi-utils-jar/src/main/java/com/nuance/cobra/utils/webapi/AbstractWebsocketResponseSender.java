package com.nuance.cobra.utils.webapi;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractWebsocketResponseSender implements ResponseSender
{
    protected abstract Logger getLogger();

    /**
     * The websocket session
     */
    protected Session webSocketSession;

    /**
     * A flag telling the sender whether or not the session should be closed after sending the response
     */
    protected Boolean closeSession;

    /**
     * Sends a string to the client application
     *
     * @param response
     *         The string to be sent
     */
    public void sendResponse(String response)
    {
        ThreadContext.put(ThreadContextRunnable.SESSION_ID, getSessionId(response));

        if (!webSocketSession.isOpen())
        {
            getLogger().trace("websocket is not open - Not sending message: {}", response);
            return;
        }

        try
        {
            getLogger().trace("websocket - Message sent: {}", response);
            webSocketSession.getAsyncRemote().sendText(response);
        }
        catch (Exception e)
        {
            getLogger().error("websocket sendText() failed -  error: {}", e.getMessage());
        }
        finally
        {
            if (Boolean.TRUE.equals(closeSession))
            {
                closeConnection(null);
            }
        }
    }

    /*
    Parse response for sessionId to be pushed to logs
     */
    protected String getSessionId(String response)
    {
        JSONObject jsonObject = new JSONObject(response);
        String sessionID = "NO SessionID Found";
        if (jsonObject.has("sessionId"))
        {
            sessionID = jsonObject.getString("sessionId");
        }
        return sessionID;
    }

    /**
     * Sends audio to the client application
     *
     * @param data
     *         The audio to be sent
     */
    public void sendBinary(byte[] data)
    {
        try
        {
            getLogger().trace("websocket - sendBinary: {} bytes", data.length);
            webSocketSession.getAsyncRemote().sendBinary(ByteBuffer.wrap(data));
        }
        catch (Exception e)
        {
            getLogger().error("websocket sendBinary() failed -  error: {}", e.getMessage());
        }
    }

    @Override
    public void closeConnection(CloseReason closeReason)
    {
        getLogger().trace("websocket - closeConnection");
        try
        {
            if (webSocketSession.isOpen())
            {
                if (closeReason != null)
                {
                    webSocketSession.close(closeReason);
                } else {
                    webSocketSession.close();
                }
            }
        }
        catch (IOException e)
        {
            getLogger().error("caught an exception while closing the websocket session: ", e);
            getLogger().warn("Exception caught at {}", e.getStackTrace());
        }
    }

}

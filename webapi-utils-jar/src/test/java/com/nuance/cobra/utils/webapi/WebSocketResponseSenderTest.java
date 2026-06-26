package com.nuance.cobra.utils.webapi;

import org.junit.Test;
import org.mockito.Mockito;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

public class WebSocketResponseSenderTest
{
    @Test
    public void sendResponse()
    {
        Session testSession = Mockito.mock(Session.class);
        RemoteEndpoint.Async testAsync = Mockito.mock(RemoteEndpoint.Async.class);
        Mockito.when(testSession.getAsyncRemote()).thenReturn(testAsync);
        WebSocketResponseSender respSender = new WebSocketResponseSender(testSession, true);
        respSender.sendResponse("{\"test\":\"1\"}");
    }

    @Test
    public void sendResponseSessionID()
    {
        Session testSession = Mockito.mock(Session.class);
        RemoteEndpoint.Async testAsync = Mockito.mock(RemoteEndpoint.Async.class);
        Mockito.when(testSession.getAsyncRemote()).thenReturn(testAsync);
        WebSocketResponseSender respSender = new WebSocketResponseSender(testSession, false);
        respSender.sendResponse("{\"sessionId\":\"1\"}");
    }

    @Test
    public void sendResponseNull()
    {
        Session testSession = Mockito.mock(Session.class);
        Mockito.when(testSession.getAsyncRemote()).thenReturn(null);
        WebSocketResponseSender respSender = new WebSocketResponseSender(testSession, true);
        respSender.sendResponse("{\"sessionId\":\"1\"}");
    }

    @Test
    public void sendBinary()
    {
        Session testSession = Mockito.mock(Session.class);
        RemoteEndpoint.Async testAsync = Mockito.mock(RemoteEndpoint.Async.class);
        Mockito.when(testSession.getAsyncRemote()).thenReturn(testAsync);
        WebSocketResponseSender respSender = new WebSocketResponseSender(testSession, true);
        respSender.sendBinary("test".getBytes());
    }

    @Test
    public void sendBinaryNull()
    {
        Session testSession = Mockito.mock(Session.class);
        Mockito.when(testSession.getAsyncRemote()).thenReturn(null);
        WebSocketResponseSender respSender = new WebSocketResponseSender(testSession, true);
        respSender.sendBinary("test".getBytes());
    }

    @Test
    public void closeConnection()
    {
        Session testSession = Mockito.mock(Session.class);
        RemoteEndpoint.Async testAsync = Mockito.mock(RemoteEndpoint.Async.class);
        Mockito.when(testSession.getAsyncRemote()).thenReturn(testAsync);
        WebSocketResponseSender respSender = new WebSocketResponseSender(testSession, true);
        respSender.closeConnection(null);
    }

}
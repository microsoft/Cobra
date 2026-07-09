package com.nuance.plugin.kryptonws.response;

import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KryptonWsResponseSenderTest
{
    Session mockSession = mock(Session.class);
    KryptonWsResponseSender kryptonWsResponseSender = new KryptonWsResponseSender(mockSession);

    @Test
    public void getLogger()
    {
        assertNotNull(kryptonWsResponseSender.getLogger());
    }

    @Test
    public void closeConnection()
    {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, "");
        kryptonWsResponseSender.closeConnection(closeReason);
        verify(mockSession, atLeastOnce()).isOpen();
    }
}
package com.nuance.cobra.utils.http;

import org.junit.Test;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.MessageHandler;

import java.io.IOException;
import java.util.Enumeration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebSocketHttpSessionTest {

    HttpSession mockHttpSession = mock(HttpSession.class);

    @Test
    public void testDataStructureClass() {
        when(mockHttpSession.getId()).thenReturn("10");
        when(mockHttpSession.getAttributeNames()).thenReturn(new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        });

        WebSocketHttpSession session = new WebSocketHttpSession(mockHttpSession);
        assertNull(session.getContainer());
        session.addMessageHandler(null);
        session.addMessageHandler(String.class, new MessageHandler.Partial<String>() {
            @Override
            public void onMessage(String s, boolean b) {

            }
        });

        session.addMessageHandler(String.class, new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {

            }
        });
        assertNotNull(session.getMessageHandlers());
        session.removeMessageHandler(null);
        assertNull(session.getProtocolVersion());
        assertNull(session.getNegotiatedSubprotocol());
        assertNotNull(session.getNegotiatedExtensions());
        assertTrue(session.isSecure());
        assertTrue(session.isOpen());

        session.setMaxIdleTimeout(1);
        assertEquals(1, session.getMaxIdleTimeout());

        session.setMaxBinaryMessageBufferSize(2);
        assertEquals(2, session.getMaxBinaryMessageBufferSize());

        session.setMaxTextMessageBufferSize(3);
        assertEquals(3, session.getMaxTextMessageBufferSize());

        assertNull(session.getAsyncRemote());
        assertNull(session.getBasicRemote());

        assertEquals("10", session.getId());

        try {
            session.close();
            session.close(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, ""));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        verify(mockHttpSession, times(2)).invalidate();

        assertNull(session.getRequestURI());
        assertNotNull(session.getRequestParameterMap());
        assertNull(session.getQueryString());
        assertNotNull(session.getPathParameters());
        assertNotNull(session.getUserProperties());
        assertNull(session.getUserPrincipal());
        assertNotNull(session.getOpenSessions());
    }

    @Test
    public void testUniqueID() {
        WebSocketHttpSession session1 = new WebSocketHttpSession(new FakeHttpSession());
        WebSocketHttpSession session2 = new WebSocketHttpSession(new FakeHttpSession());

        assertNotEquals(session1.getId(), session2.getId());
    }
}
package com.nuance.cobra.api.websocket;

import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.test.CobraTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class WebsocketEndpointTest extends CobraTest
{
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private javax.websocket.Session mockWebsocketSession;
    private WebsocketEndpoint websocketEndpoint;
    private Session mockSession;
    private Command mockCommand;

    @Before
    public void setUp() throws Exception
    {
        // Call parent setup()
        super.setUp();

        // Setup System.err to put it in a stream
        System.setOut(new PrintStream(outContent));

        // Setup mock SessionManager
        mockSession = mock(Session.class);
        when(mockSession.getConfigWithMerge(any())).thenReturn(mockServerConfigs);
        mockCommand = mock(Command.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSessionManager.getSession("12345", false)).thenReturn(mockSession);

        // Setup websocket
        mockWebsocketSession = mock(javax.websocket.Session.class);
        websocketEndpoint = new WebsocketEndpoint();
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        System.setOut(null);
    }

    @Test
    public void testAnnotation() throws Exception
    {
        assertTrue(WebsocketEndpoint.class.isAnnotationPresent(ServerEndpoint.class));
        assertTrue("/websocket".equals(WebsocketEndpoint.class.getAnnotation(ServerEndpoint.class).value()));
    }

    @Test
    public void testOnErrorWithThrowableNull()
    {
        // Setup
        String expectedLog = "onErr: Websocket err: (no message from Throwable parameter)";

        // Call method
        websocketEndpoint.onError(mockWebsocketSession, null);

        System.err.println(outContent.toString());

        // Check result
        assertTrue(outContent.toString().contains(expectedLog));
    }

    @Test
    public void testOnErrorWithThrowableGetMessageNull()
    {
        // Setup
        String expectedLog = "onErr: Websocket err: (no message from Throwable parameter)";
        java.lang.Throwable mockError = mock(java.lang.Throwable.class);
        when(mockError.getMessage()).thenReturn(null);

        // Call method
        websocketEndpoint.onError(mockWebsocketSession, mockError);

        // Check result
        //String comparison fails for no reason.
        assertTrue(outContent.toString().contains(expectedLog));
    }

    @Test
    public void testOnErrorTimeout()
    {
        // Setup
        String expectedLog = "onErr throwable message: Timeout on Read";
        java.lang.Throwable mockError = mock(java.lang.Throwable.class);
        when(mockError.getMessage()).thenReturn("Timeout on Read");

        // Call method
        websocketEndpoint.onError(mockWebsocketSession, mockError);

        // Check result
        assertTrue(outContent.toString().contains(expectedLog));
    }

    @Test
    public void testOnClose() throws Exception
    {
        // Setup
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "test");
        String expectedLog = String.format("onClose: Websocket is closing. SessionId = %s. ReasonPhrase = \"%s\". CloseCode = %d", "", "test", 1000);

        // Call method
        websocketEndpoint.onClose(mockWebsocketSession, closeReason);

        // Check result
        assertTrue(outContent.toString().contains(expectedLog));
    }

    @Test
    public void testOnErrorWithCloseReasonNull()
    {
        // Setup
        String expectedLog = String.format("onClose: Websocket is closing. SessionId = %s. ReasonPhrase = \"%s\". CloseCode = %d", "", "", -1);

        // Call method
        websocketEndpoint.onClose(mockWebsocketSession, null);

        // Check result
        assertTrue(outContent.toString().contains(expectedLog));
    }

    @Test
    public void testOnErrorWithCloseReasonGetReasonPhraseNull()
    {
        // Setup
        String expectedLog = String.format("onClose: Websocket is closing. SessionId = %s. ReasonPhrase = \"%s\". CloseCode = %d", "", "earthquake", -1);
        CloseReason mockCloseReason = mock(CloseReason.class);
        when(mockCloseReason.getReasonPhrase()).thenReturn("earthquake");

        // Call method
        websocketEndpoint.onClose(mockWebsocketSession, mockCloseReason);

        // Check result
        assertTrue(outContent.toString().contains(expectedLog));
    }
}

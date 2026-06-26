package com.nuance.cobra.logging;

import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class WapiCustomJsonLayoutTest
{
    private LogEvent mockEvent;
    private ReadOnlyStringMap mockContext;
    private String formattedMessage = "formattedMessage";
    private String threadName = "threadName";
    private String loggerName = "loggerName";
    private String loggerFqcn = "loggerFqcn";
    private long timeMillis = 12345;
    private long threadId = 54321;
    private int threadPriority = 4;

    @Before
    public void setup()
    {
        mockEvent = mock(LogEvent.class);
        Message mockMessage = mock(Message.class);
        mockContext = mock(ReadOnlyStringMap.class);
        when(mockMessage.getFormattedMessage()).thenReturn(formattedMessage);
        when(mockEvent.getMessage()).thenReturn(mockMessage);
        when(mockEvent.getContextData()).thenReturn(mockContext);
        when(mockEvent.getLevel()).thenReturn(Level.INFO);
        when(mockEvent.getTimeMillis()).thenReturn(timeMillis);
        when(mockEvent.getThreadName()).thenReturn(threadName);
        when(mockEvent.getThreadId()).thenReturn(threadId);
        when(mockEvent.getThreadPriority()).thenReturn(threadPriority);
        when(mockEvent.getLoggerName()).thenReturn(loggerName);
        when(mockEvent.getLoggerFqcn()).thenReturn(loggerFqcn);
    }

    @Test
    public void testFormatNoSessionId()
    {
        WapiCustomJsonLayout layout = WapiCustomJsonLayout.createLayout(Charset.defaultCharset());

        when(mockContext.toMap()).thenReturn(new HashMap<String, String>(0));

        String results = layout.toSerializable(mockEvent);

        JSONObject jsonObject = new JSONObject(results);
        assertNotNull(jsonObject);
        assertEquals(formattedMessage, jsonObject.getString("message"));
        assertEquals(threadName, jsonObject.getString("thread"));
        assertEquals(loggerName, jsonObject.getString("loggerName"));
        assertEquals(loggerFqcn, jsonObject.getString("loggerFqcn"));
        assertEquals(timeMillis, jsonObject.getLong("timeMillis"));
        assertEquals(threadId, jsonObject.getInt("threadId"));
        assertEquals(threadPriority, jsonObject.getLong("threadPriority"));
        assertEquals("INFO", jsonObject.getString("level"));
        assertTrue(jsonObject.getJSONObject("contextMap").has(ThreadContextRunnable.SESSION_ID));
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", jsonObject.getJSONObject("contextMap").getString(ThreadContextRunnable.SESSION_ID));
    }

    @Test
    public void testFormatWithSessionId()
    {
        WapiCustomJsonLayout layout = WapiCustomJsonLayout.createLayout(Charset.defaultCharset());

        String sessionId = "mySession/Id";
        
        Map map = new HashMap<String, String>(1);
        map.put(ThreadContextRunnable.SESSION_ID, sessionId);
        when(mockContext.toMap()).thenReturn(map);

        String results = layout.toSerializable(mockEvent);

        JSONObject jsonObject = new JSONObject(results);
        assertNotNull(jsonObject);
        assertTrue(jsonObject.getJSONObject("contextMap").has(ThreadContextRunnable.SESSION_ID));
        assertEquals(sessionId, jsonObject.getJSONObject("contextMap").getString(ThreadContextRunnable.SESSION_ID));
    }

    @Test
    public void testFormatWithEmptySessionId()
    {
        WapiCustomJsonLayout layout = WapiCustomJsonLayout.createLayout(Charset.defaultCharset());

        Map map = new HashMap<String, String>(1);
        map.put(ThreadContextRunnable.SESSION_ID, "");
        when(mockContext.toMap()).thenReturn(map);

        String results = layout.toSerializable(mockEvent);

        JSONObject jsonObject = new JSONObject(results);
        assertNotNull(jsonObject);
        assertTrue(jsonObject.getJSONObject("contextMap").has(ThreadContextRunnable.SESSION_ID));
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", jsonObject.getJSONObject("contextMap").getString(ThreadContextRunnable.SESSION_ID));
    }
}
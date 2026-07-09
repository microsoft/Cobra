package com.nuance.cobra.utils.logging;

import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomConverterTest
{
    @Test
    public void testNewInstance()
            throws Exception
    {
        CustomConverter customConverter = CustomConverter.newInstance(new String[] { "a", "b" });
    }

    @Test
    public void testFormat()
            throws Exception
    {
        LogEvent event = mock(LogEvent.class);
        ReadOnlyStringMap context = mock(ReadOnlyStringMap.class);

        when(event.getContextData()).thenReturn(context);
        when(context.getValue(ThreadContextRunnable.SESSION_ID)).thenReturn("123");

        CustomConverter customConverter = CustomConverter.newInstance(new String[] { ThreadContextRunnable.SESSION_ID });
        StringBuilder testBuilder = new StringBuilder();
        customConverter.format(event, testBuilder);

        assertEquals("[123]", testBuilder.toString());
    }

    @Test
    public void testFormatInvalidSessionID()
            throws Exception
    {
        LogEvent event = mock(LogEvent.class);
        ReadOnlyStringMap context = mock(ReadOnlyStringMap.class);

        when(event.getContextData()).thenReturn(context);
        when(context.getValue(ThreadContextRunnable.SESSION_ID)).thenReturn("");

        CustomConverter customConverter = CustomConverter.newInstance(new String[] { ThreadContextRunnable.SESSION_ID });
        StringBuilder testBuilder = new StringBuilder();
        customConverter.format(event, testBuilder);

        assertEquals("[xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx]", testBuilder.toString());
    }

    @Test
    public void testFormatCompanyName()
            throws Exception
    {
        LogEvent event = mock(LogEvent.class);
        ReadOnlyStringMap context = mock(ReadOnlyStringMap.class);

        when(event.getContextData()).thenReturn(context);
        when(context.getValue(ThreadContextRunnable.COMPANY_NAME)).thenReturn("123");
        when(context.getValue(ThreadContextRunnable.APP_NAME)).thenReturn("123");

        CustomConverter customConverter = CustomConverter.newInstance(new String[] { "company.name_app.name" });
        StringBuilder testBuilder = new StringBuilder();
        customConverter.format(event, testBuilder);

        assertEquals("[123_123]", testBuilder.toString());
    }

    @Test
    public void testFormatCompanyNameInvalidName()
            throws Exception
    {
        LogEvent event = mock(LogEvent.class);
        ReadOnlyStringMap context = mock(ReadOnlyStringMap.class);

        when(event.getContextData()).thenReturn(context);
        when(context.getValue(ThreadContextRunnable.COMPANY_NAME)).thenReturn(null);

        CustomConverter customConverter = CustomConverter.newInstance(new String[] { "company.name_app.name" });
        StringBuilder testBuilder = new StringBuilder();
        customConverter.format(event, testBuilder);

        assertEquals("", testBuilder.toString());
    }

}
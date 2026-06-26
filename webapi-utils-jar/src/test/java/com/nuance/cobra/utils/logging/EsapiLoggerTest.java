package com.nuance.cobra.utils.logging;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.esapi.Logger;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EsapiLoggerTest
{
    private static Logger mockLogger;

    @Before
    public void setUp()
    {
        mockLogger = mock(Logger.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testConstructor()
    {
        new EsapiLogger();
    }

    private void setLevelsEnabled(boolean enabled)
    {
        when(mockLogger.isFatalEnabled()).thenReturn(enabled);
        when(mockLogger.isErrorEnabled()).thenReturn(enabled);
        when(mockLogger.isWarningEnabled()).thenReturn(enabled);
        when(mockLogger.isInfoEnabled()).thenReturn(enabled);
        when(mockLogger.isDebugEnabled()).thenReturn(enabled);
        when(mockLogger.isTraceEnabled()).thenReturn(enabled);
    }

    private void doLogging()
    {
        EsapiLogger.always(mockLogger, "1 + 1 = %s", 2);
        EsapiLogger.fatal(mockLogger, "2 + 2 = %s", 4);
        EsapiLogger.error(mockLogger, "3 + 3 = %s", 6);
        EsapiLogger.warning(mockLogger, "4 + 4 = %s", 8);
        EsapiLogger.warning(mockLogger, "4 + 4 = 8", new Throwable());
        EsapiLogger.info(mockLogger, "5 + 5 = %s", 10);
        EsapiLogger.debug(mockLogger, "6 + 6 = %s", 12);
        EsapiLogger.trace(mockLogger, "7 + 7 = %s", 14);
    }

    private void verifyTimes(int times, ArgumentCaptor<String> argumentCaptor)
    {
        verify(mockLogger, times(1)).always(any(), argumentCaptor.capture());
        verify(mockLogger, times(times)).fatal(any(), argumentCaptor.capture());
        verify(mockLogger, times(times)).error(any(), argumentCaptor.capture());
        verify(mockLogger, times(times)).warning(any(), argumentCaptor.capture());
        verify(mockLogger, times(1)).warning(any(), argumentCaptor.capture(), any());
        verify(mockLogger, times(times)).info(any(), argumentCaptor.capture());
        verify(mockLogger, times(times)).debug(any(), argumentCaptor.capture());
        verify(mockLogger, times(times)).trace(any(), argumentCaptor.capture());
    }

    @Test
    public void testLogger()
    {
        List<String> logs;
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        setLevelsEnabled(false);
        doLogging();
        verifyTimes(0, argumentCaptor);

        Mockito.clearInvocations(mockLogger);
        argumentCaptor = ArgumentCaptor.forClass(String.class);

        setLevelsEnabled(true);
        doLogging();
        verifyTimes(1, argumentCaptor);

        logs = argumentCaptor.getAllValues();
        assertEquals("1 + 1 = 2", logs.get(0));
        assertEquals("2 + 2 = 4", logs.get(1));
        assertEquals("3 + 3 = 6", logs.get(2));
        assertEquals("4 + 4 = 8", logs.get(3));
        assertEquals("4 + 4 = 8", logs.get(4));
        assertEquals("5 + 5 = 10", logs.get(5));
        assertEquals("6 + 6 = 12", logs.get(6));
        assertEquals("7 + 7 = 14", logs.get(7));
    }
}
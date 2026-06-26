package com.nuance.cobra.logging.eventhub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.utils.response.EDF.AudioLog;
import com.nuance.cobra.utils.response.EDF.EventHubLog;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.nuance.cobra.session.WebApiConfig.Property.EVENTHUB_NAMESPACE;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class EventHubLogPosterTest
{
    private final CombinedConfiguration testConfs = mock(CombinedConfiguration.class);
    private Logger mockLogger;

    @Before
    public void initialize()
    {
        when(testConfs.getString(EVENTHUB_NAMESPACE.key)).thenReturn("testNamespace");

        Logger logger = LogManager.getLogger(EventHubLogPoster.class);
        mockLogger = spy(logger);
        EventHubLogPoster.logger = mockLogger;
    }

    @Test
    public void testLog_withEdfLog() {
        EdfMessageQueueable edfLog = createSampleEdfLog();
        EventHubLogPoster.log(edfLog, testConfs);
        verify(mockLogger).trace("Sending command log to EventHub");
    }

    private EdfMessageQueueable createSampleEdfLog() {
        EventHubLog eventHubLog = new EventHubLog();
        eventHubLog.loggingTopic = "testTopic";
        eventHubLog.data = Map.of("key", "value");
        return eventHubLog;
    }

    @Test
    public void testLog_withAudioLog() {
        AudioLog audioLog = new AudioLog();
        EventHubLogPoster.log(audioLog, testConfs);
        verify(mockLogger).error("Sending audio log to EventHub is not supported.");
    }

    @Test
    public void testLog_withException() throws JsonProcessingException {
        EdfMessageQueueable edfLog = mock(EventHubLog.class);
        doThrow(new RuntimeException("Test Exception")).when(edfLog).toJson(anyBoolean());
        EventHubLogPoster.log(edfLog, testConfs);
        verify(mockLogger).error(eq("Failed to post logs to EventHub."), any(Exception.class));
    }

    @Test
    public void testHandleCommandLog_withException() throws JsonProcessingException {
        EventHubLog edfLog = mock(EventHubLog.class);
        when(edfLog.toJson(anyBoolean())).thenThrow(new RuntimeException("Test Exception"));
        EventHubLogPoster.handleCommandLog(edfLog, testConfs, mockLogger);
        verify(mockLogger).error(eq("Failed to post {} logs to EventHub."), eq(null), any(Exception.class));
    }
}
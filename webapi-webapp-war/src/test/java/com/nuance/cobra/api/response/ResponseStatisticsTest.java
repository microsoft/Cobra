package com.nuance.cobra.api.response;

import com.nuance.cobra.client.VocalizerPremiumVoiceModels;
import com.nuance.cobra.command.Command;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseStatisticsTest {

    private Command.CommandStatistics createStatistics() {
        Command.CommandStatistics statistics = mock(Command.CommandStatistics.class);
        statistics.directives = new ArrayList<>();
        Command.DirectiveStatistics directiveStatistics = mock(Command.DirectiveStatistics.class);
        directiveStatistics.name = "bob";
        directiveStatistics.startTime = Calendar.getInstance();
        statistics.directives.add(directiveStatistics);
        statistics.events = new ArrayList<>();
        Command.EventStatistics eventStatistics = mock(Command.EventStatistics.class);
        eventStatistics.name = "bob";
        eventStatistics.startTime = Calendar.getInstance();
        statistics.events.add(eventStatistics);
        statistics.firstAudioBufferReceivedTime = Calendar.getInstance();
        statistics.firstAudioBufferSentTime = Calendar.getInstance();
        statistics.firstIntermediateResultTime = Calendar.getInstance();
        statistics.lastAudioBufferReceivedTime = Calendar.getInstance();
        statistics.lastAudioBufferSentTime = Calendar.getInstance();
        statistics.engineBufferedAudioChunks = 1;
        statistics.engineSentAudioChunks = 2;
        statistics.engineFirstAudioChunk = Calendar.getInstance();
        statistics.engineLastAudioChunk = Calendar.getInstance();
        statistics.mediaDuration = 5;
        statistics.lastIntermediateResultTime = Calendar.getInstance();
        statistics.startTime = Calendar.getInstance();
        statistics.endTime = Calendar.getInstance();
        statistics.clientUserID = "123456";
        statistics.enableSpeechRecognitionSpeakerProfile = true;
        when(statistics.getVoiceModel()).thenReturn(VocalizerPremiumVoiceModels.XPREMIUM_HIGH.getValue());
        when(statistics.getVoiceQuality()).thenReturn("premium");
        return statistics;
    }

    @Test
    public void testCreateResponseStatistics() {
        Command.CommandStatistics statistics = createStatistics();
        when(statistics.getVoiceModel()).thenReturn(null);
        when(statistics.getVoiceQuality()).thenReturn(null);
        ResponseStatistics repStats = new ResponseStatistics(statistics);
        assertNull(repStats.voiceModel);
        assertNull(repStats.voiceQuality);

        when(statistics.getVoiceModel()).thenReturn("");
        when(statistics.getVoiceQuality()).thenReturn("");
        repStats = new ResponseStatistics(statistics);
        assertNull(repStats.voiceModel);
        assertNull(repStats.voiceQuality);

        assertEquals(1, repStats.engineBufferedAudioChunks.intValue());
        assertEquals(2, repStats.engineSentAudioChunks.intValue());
        assertNotNull(repStats.engineFirstAudioChunk);
        assertNotNull(repStats.engineLastAudioChunk);
    }

    @Test
    public void testModelVersionStatistics()
    {
        Command.CommandStatistics statistics = createStatistics();
        when(statistics.getModelVersion()).thenReturn("notDefined");
        ResponseStatistics respStats = new ResponseStatistics(statistics);
        assertEquals("notDefined", respStats.modelVersion);
    }

    @Test
    public void testSpeechSynthesisGenreStatistics(){
        Command.CommandStatistics statistics = createStatistics();
        when(statistics.getSpeechSynthesisTuningBundle()).thenReturn("notDefined");
        ResponseStatistics responseStatistics = new ResponseStatistics(statistics);
        assertEquals("notDefined", responseStatistics.speechSynthesisTuningBundle);
    }

    @Test
    public void testCreateResponseStatistics_mediaDuration() {
        Command.CommandStatistics statistics = createStatistics();

        statistics.mediaDuration = 5;
        ResponseStatistics stats = new ResponseStatistics(statistics);
        Assert.assertEquals((Integer)5, stats.mediaDuration);

        statistics.mediaDuration = null;
        stats = new ResponseStatistics(statistics);
        assertNull(stats.mediaDuration);


        stats = new ResponseStatistics(statistics);
        assertNull(stats.mediaDuration);
    }
}

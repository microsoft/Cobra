package com.nuance.cobra.session;



import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SessionNoPowerMockTest {

    static class AudioBlobPathTestData {

        static final String TENANT = "mytenant";
        static final String TOPIC = "mytopic";
        static final String DUMMY_SESSION_ID = "eab871cd-2fed-4e5e-82c2-2b9a61fcffa8";

        int year;
        int month;
        int day;
        int hour;
        int minutes;
        int seconds;

        String sessionId;

        boolean multipleAudioPerBlob;
        int timesliceDuration;
        int expectedTimeslice;

        AudioBlobPathTestData(int year, int month, int day, int hour, int minutes, int seconds, int timesliceDuration, int expectedTimeslice) {
            this(true, year, month, day, hour, minutes, seconds, null, timesliceDuration, expectedTimeslice);
        }

        AudioBlobPathTestData(int year, int month, int day, int hour, int minutes, int seconds, String sessionId) {
            this(false, year, month, day, hour, minutes, seconds, sessionId, -1, -1);
        }

        private AudioBlobPathTestData(boolean multipleAudioPerBlob, int year, int month, int day, int hour,
                                      int minutes, int seconds, String sessionId,
                                      int timesliceDuration, int expectedTimeslice) {
            this.multipleAudioPerBlob = multipleAudioPerBlob;
            this.year = year;
            this.month = month - 1;
            this.day = day;
            this.hour = hour;
            this.minutes = minutes;
            this.seconds = seconds;
            this.sessionId = sessionId;
            this.timesliceDuration = timesliceDuration;
            this.expectedTimeslice = expectedTimeslice;
        }

        Calendar getDateTime() {
            Calendar datetime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            datetime.set(year, month, day, hour, minutes, seconds);
            return datetime;
        }
    }

    @Test
    public void makeAudioBlobPathTest() throws InterruptedException {

        AudioBlobPathTestData[] testData = {
                // configured timeslice duration = 10, different minutes.
                new AudioBlobPathTestData(2000, 12, 3, 15, 23, 42, 10, 20),
                new AudioBlobPathTestData(2000, 12, 3, 15, 20, 42, 10, 20),
                new AudioBlobPathTestData(2000, 12, 3, 15, 49, 47, 10, 40),
                new AudioBlobPathTestData(2000, 12, 3, 15, 59, 26, 10, 50),

                // configured timeslice duration = 15, different minutes.
                new AudioBlobPathTestData(2016, 7, 9, 15, 18, 42, 15, 15),
                new AudioBlobPathTestData(2016, 7, 9, 15, 12, 47, 15, 0),
                new AudioBlobPathTestData(2016, 7, 9, 15, 48, 17, 15, 45),

                new AudioBlobPathTestData(2014, 5, 26, 15, 12, 47, 15, 0),
                new AudioBlobPathTestData(2014, 5, 26, 15, 12, 47, 15, 0),

                // specify Session ID ==> one audio per blob, sessiond id will be part of the path.
                new AudioBlobPathTestData(2000, 12, 3, 15, 59, 26, AudioBlobPathTestData.DUMMY_SESSION_ID),
        };


        final String sessionId = "testSessionId";

        for (AudioBlobPathTestData data : testData) {
            doAudioBlobTest(data);
        }
    }

    private void doAudioBlobTest(AudioBlobPathTestData testData) {
        final String PATH_PATTERN = "(?<topic>[^/]+)/(?<tenant>[^/]+)/audio/(?<year>[0-9]+)/(?<month>[0-9]+)/(?<day>[0-9]+)/(?<hour>[0-9]+)/";
        final String UUID_PATTERN = "(?<uuid>[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})";
        final String TIMESLICE_PATTERN = "-(?<timeslice>[0-9]{2})";
        final String STEP_ID_PATTERN = "-(?<stepid>[0-9]+)";

        final String MULTIPLE_AUDIO_PER_BLOB_PATTERN = PATH_PATTERN + UUID_PATTERN + TIMESLICE_PATTERN;
        final String ONE_AUDIO_PER_BLOB_PATTERN = PATH_PATTERN + UUID_PATTERN + STEP_ID_PATTERN;

        final Pattern p = Pattern.compile(testData.multipleAudioPerBlob ? MULTIPLE_AUDIO_PER_BLOB_PATTERN : ONE_AUDIO_PER_BLOB_PATTERN);

        CombinedConfiguration config = mock(CombinedConfiguration.class);
        when(config.getString(WebApiConfig.Property.FLUENT_LOG_TOPIC_NAME.key)).thenReturn(AudioBlobPathTestData.TOPIC);
        when(config.getString(WebApiConfig.Property.APPLICATION_KEY.key)).thenReturn(AudioBlobPathTestData.TENANT);

        when(config.getBoolean(WebApiConfig.Property.FLUENT_MULTIPLE_AUDIO_LOGS_PER_BLOB.key)).thenReturn(testData.multipleAudioPerBlob);

        when(config.getInt(WebApiConfig.Property.FLUENT_AUDIO_LOGS_TIMESLICE_DURATION.key,
                (Integer) WebApiConfig.Property.FLUENT_AUDIO_LOGS_TIMESLICE_DURATION.defaultValue)).thenReturn(testData.timesliceDuration);

        Session session = new Session(AudioBlobPathTestData.DUMMY_SESSION_ID, config, testData.getDateTime());
        String blobPath = session.makeAudioBlobPath();

        // create matcher for pattern p and given string
        Matcher m = p.matcher(blobPath);

        assertTrue(m.find());
        assertEquals(AudioBlobPathTestData.TENANT, m.group("tenant"));
        assertEquals(AudioBlobPathTestData.TOPIC, m.group("topic"));

        assertEquals(String.format("%04d", testData.year), m.group("year"));
        assertEquals(String.format("%02d", testData.month + 1), m.group("month"));
        assertEquals(String.format("%02d", testData.day), m.group("day"));
        assertEquals(String.format("%02d", testData.hour), m.group("hour"));

        if (testData.multipleAudioPerBlob) {
            assertEquals(String.format("%02d", testData.expectedTimeslice), m.group("timeslice"));
        } else {
            // One audio per blob. uuid should be the session id.
            assertEquals(testData.sessionId, m.group("uuid"));
        }
    }
}

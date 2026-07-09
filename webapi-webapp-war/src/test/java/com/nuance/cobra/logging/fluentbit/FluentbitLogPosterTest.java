package com.nuance.cobra.logging.fluentbit;

import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.EDF.AudioLog;
import com.nuance.cobra.utils.response.EDF.NeapLog;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.owasp.esapi.ESAPI;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({LogManager.class, ESAPI.class})
public class FluentbitLogPosterTest
        extends CobraTest
{
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);
    private Logger mockLogger;
    private org.owasp.esapi.Logger mockEsapiLogger;

    @Before
    public void initialize()
            throws Exception
    {
        super.setUp();
        testConf.setProperty(WebApiConfig.Property.FLUENT_LOGGER_LOG_LEVEL.key, 190);
        testConf.setProperty(WebApiConfig.Property.FLUENT_LOGGER_LOG_LEVEL_NAME.key, "REPORTING");

        mockLogger = mock(Logger.class);
        PowerMockito.mockStatic(LogManager.class);
        BDDMockito.given(LogManager.getLogger(FluentbitLogPoster.class)).willReturn(mockLogger);
        BDDMockito.given(LogManager.getLogger(AudioLog.class)).willReturn(mockLogger);
        Mockito.reset(mockLogger);

        mockEsapiLogger = mock(org.owasp.esapi.Logger.class);
        PowerMockito.mockStatic(ESAPI.class);
        BDDMockito.given(ESAPI.getLogger(FluentbitLogPoster.class)).willReturn(mockEsapiLogger);
    }

    @Test (expected = Exception.class)
    public void testFluentbitLogPosterLogging_exception() throws Exception
    {
        NeapLog edfLog = mock(NeapLog.class);
//        when(edfLog.toJson(anyBoolean())).thenThrow(new Exception("error"));
        FluentbitLogPoster.handleCommandLog(edfLog, testConfs, mockLogger);
    }

    @Test
    public void testFluentbitLogPosterAudioLogging() throws Exception
    {
        FileInputStream mockFileStream = mock(FileInputStream.class);
        when(mockFileStream.read(any())).thenReturn(10);

        FluentbitLogPoster.handleAudioLog(dummyAudioLog(), testConfs, "testFileName", 10, mockFileStream, mockLogger);

        String customLogLevelName = testConf.getString(WebApiConfig.Property.FLUENT_LOGGER_LOG_LEVEL_NAME.key);
        int customLogLevelValue = testConf.getInt(WebApiConfig.Property.FLUENT_LOGGER_LOG_LEVEL.key);
        Level customLogLevel = Level.forName(customLogLevelName, customLogLevelValue);
        verify(mockLogger).log(eq(customLogLevel), anyString(), any(), any(), any());
    }

    @Test(expected = IOException.class)
    public void testFluentbitLogPosterHandleAudioLogEmptyFile() throws Exception
    {
        FileInputStream mockFileStream = mock(FileInputStream.class);
        when(mockFileStream.read(any())).thenReturn(0);

        Logger audioMockLogger = mock(Logger.class);
        FluentbitLogPoster.handleAudioLog(dummyAudioLog(), testConfs, "testFileName", 10, mockFileStream, audioMockLogger);

        verify(audioMockLogger).log(any(), anyString(), any(), any(), any());
    }

    @Test
    public void testFluentbitLogPosterAudioLog() throws Exception
    {
        String filename = "testfile.test";
        AudioLog audioLog = new AudioLog();
        audioLog.edfMessageFormat = EdfMessageQueueable.EdfMessageFormat.EDF_V1_FORMAT;
        audioLog.fileName = filename;
        Files.write(Paths.get(filename), filename.getBytes());
        FluentbitLogPoster.log(audioLog, testConfs);
        assertFalse(Files.exists(Paths.get(filename)));
    }

    @After
    public void destroy()
    {
    }

    AudioLog dummyAudioLog()
    {
        AudioLog audioLog = mock(AudioLog.class);
        audioLog.audioBlobPath = "dummy/path";
        audioLog.sessionId = UUID.randomUUID().toString();
        audioLog.encrypted = false;
        audioLog.tenantKey = "testCo_testApp";
        return audioLog;
    }


}
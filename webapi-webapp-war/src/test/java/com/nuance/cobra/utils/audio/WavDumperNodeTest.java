package com.nuance.cobra.utils.audio;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "org.xml.*", "jdk.xml.*", "java.xml.*", "javax.xml.*", "com.sun.org.apache.xerces.*", "javax.management.*", "org.w3c.dom.*"})
@PrepareForTest({File.class, WebApiConfigManager.class})
public class
WavDumperNodeTest {
    private WavDumperNode wavDumperNode;
    private Session session;
    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConfig = CobraConfigurationUtils.getHighestConfig(testConfigs);
    ResponseHandler mockResHandler = mock(ResponseHandler.class);

    List<AudioNodeObserver> observersTest = new ArrayList<AudioNodeObserver>();
    AudioNodeObserver mockObs = mock(AudioNodeObserver.class);

    @Before
    public void setUp() {
        testConfig.setProperty(WebApiConfig.Property.DISK_USAGE_LIMIT_PERCENTAGE.key, 0);
        testConfig.setProperty(WebApiConfig.Property.AUDIO_ENCRYPTION.key, true);
        testConfig.setProperty(WebApiConfig.Property.DEBUG_ENABLED.key, true);
        Session session = new Session("TestSessionID", CobraConfigurationUtils.newCombinedConfigurationWithDefaults());

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfigs);
        PowerMockito.when(WebApiConfigManager.getConfigPath()).thenReturn("test/path/to/return");

        observersTest.add(mockObs);

        wavDumperNode = new WavDumperNode(1600, "fileNameRaw", session, true,
                "edfv1", true, true, mockResHandler, observersTest);
    }

    @Test(expected = NullPointerException.class)
    public void testMakePath() {
        wavDumperNode = new WavDumperNode(1600, "fileName", session, true,
                "edfv1", true, true, mockResHandler, observersTest);
        assertTrue(wavDumperNode.getFolderPath() != null);
        assertTrue(wavDumperNode.getFolderPath().contains("data/eventLogs"));
    }

    @Test
    public void testOnAudio() {
        AudioChunk audioChunk = mock(AudioChunk.class);
        byte[] bytes = new byte[1];
        when(audioChunk.getBytes()).thenReturn(bytes);

        wavDumperNode.onAudio(audioChunk);
        assertNotNull(Whitebox.getInternalState(wavDumperNode, "dataByteArray"));
    }

    @Test
    public void testMakeHeader() {
        assertEquals(wavDumperNode.makeHeader(20).getClass(),
                new WavDumperNode.MyByteArrayOutputStream().toByteArray().getClass());
        assertNotNull(wavDumperNode.makeHeader(20));
    }

    @Test
    public void getEncryptedFilesTest() {
        assertNotNull(wavDumperNode.getEncryptedFiles());

        assertNotNull(wavDumperNode.getFolderPath());
        assertNotNull(wavDumperNode.getFileNameNoPath());
        assertEquals("fileNameRaw.wav", wavDumperNode.getFileNameNoPath());
    }

    @Test
    public void testOnEvent() {
        AudioNode.NodeEvent event = AudioNodeObserver.NodeEvent.TIMEOUT;
        AudioNode.NodeEvent startOfSpeechEvent = AudioNodeObserver.NodeEvent.START_OF_SPEECH;
        AudioNode.NodeEvent endOfSpeechEvent = AudioNodeObserver.NodeEvent.END_OF_SPEECH;
        AudioNode mockNode = mock(AudioNode.class);

        wavDumperNode.onEvent(mockNode, startOfSpeechEvent);
        wavDumperNode.onEvent(mockNode, endOfSpeechEvent);
        wavDumperNode.onEvent(mockNode, event);
        Whitebox.setInternalState(wavDumperNode, "fileName", "fileNameraw");
        try {
            wavDumperNode.onEvent(mockNode, event);
        } catch (NullPointerException e) {

        }


        // trying to cover dumpEndPointedFile method, which is called by onEvent
        testConfig.setProperty(WebApiConfig.Property.DEBUG_ENABLED.key, true);
        session = new Session("sessionId", testConfigs);

        wavDumperNode = new WavDumperNode(1600, "fileName", session, true,
                "edfv1", true, true, mockResHandler, observersTest);
        assertTrue(wavDumperNode.getFileNameNoPath().contains("file"));

        Whitebox.setInternalState(wavDumperNode, "debugEnabled", true);

        wavDumperNode.onEvent(mockNode, startOfSpeechEvent);
    }

    @Test
    public void testNullEdfTest() {
        byte[] bytes = {0, 1, 2, 3, 4, 5};

        wavDumperNode.edfDump(bytes, bytes, bytes, bytes);
        session = new Session("sessionId", testConfigs);

        wavDumperNode = new WavDumperNode(0, "rawSomething", session, true, "edfv1", true
                , true, mockResHandler, new ArrayList<>());

        assertTrue(wavDumperNode.getFileNameNoPath().contains("raw"));


        wavDumperNode.edfDump(bytes, bytes, bytes, bytes);

    }

    @Test
    public void testEdfDump() {
        byte[] bytes = {0, 1, 2, 3, 4, 5};

        wavDumperNode.edfDump(bytes, bytes, bytes, bytes);
        session = new Session("sessionId", testConfigs);

        wavDumperNode = new WavDumperNode(0, "rawSomething", session, true, "edfv1", true
                , true, mockResHandler, new ArrayList<>());

        assertTrue(wavDumperNode.getFileNameNoPath().contains("raw"));

        testConfig.setProperty(WebApiConfig.Property.DISK_USAGE_LIMIT_PERCENTAGE.key, 2000);
        session = new Session("sessionId", testConfigs);

        wavDumperNode = new WavDumperNode(0, "rawSomething", session,
                true, "edfv1", true, true, mockResHandler, new ArrayList<>());

        wavDumperNode.edfDump(bytes, bytes, bytes, bytes);

        Whitebox.setInternalState(wavDumperNode, "diskFilled", true);
        assertEquals("test/path/to/return", WebApiConfigManager.getConfigPath());

        wavDumperNode.edfDump(bytes, bytes, bytes, bytes);
    }

    @Test
    public void testWriteNullFile() {
        byte[] bts = {0, 1, 2, 3, 4, 5, 6};
        session = new Session("sessionId", testConfigs);

        assertEquals("sessionId", session.getSessionId());

        wavDumperNode = new WavDumperNode(1600, "fileName", session, true,
                "edfv1", true, false, mockResHandler, observersTest);
        wavDumperNode.writeFile(bts, bts, bts, bts, bts);
    }

    @Test
    public void testWriteFile() {
        byte[] bts = {0, 1, 2, 3, 4, 5, 6};
        session = new Session("sessionId", testConfigs);
        wavDumperNode = new WavDumperNode(1600, "fileName", session, true,
                "edfv1", true, false, mockResHandler, observersTest);

        testConfig.setProperty(WebApiConfig.Property.DISK_USAGE_LIMIT_PERCENTAGE.key, 2000);
        testConfig.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "c29tZUtleQ==");
        session = new Session("sessionId", testConfigs);

        wavDumperNode = new WavDumperNode(1600, "fileName", session, true,
                "edfv1", true, true, mockResHandler, observersTest);

        assertTrue(testConfig.getBoolean(WebApiConfig.Property.AUDIO_ENCRYPTION.key));
        assertTrue(testConfig.getBoolean(WebApiConfig.Property.DEBUG_ENABLED.key));

        wavDumperNode.writeFile(bts, bts, bts, bts, bts);

        // to cover condition
        wavDumperNode.writeFile(bts, bts, bts, bts, null);

        testConfig.setProperty(WebApiConfig.Property.AUDIO_ENCRYPTION.key, false);
        session = new Session("sessionId", testConfigs);

        wavDumperNode = new WavDumperNode(1600, "fileName", session, true,
                "edfv1", true, true, mockResHandler, observersTest);


        wavDumperNode.writeFile(bts, bts, bts, bts, bts);
    }

    @Test
    public void testDumpEndPointedFile(){
        assertNotNull(wavDumperNode);
        AudioNode mockNode = mock(AudioNode.class);
        AudioNodeObserver.NodeEvent event = AudioNodeObserver.NodeEvent.TIMEOUT;

        Whitebox.setInternalState(wavDumperNode,"dumpEndpointedUtterance", true);
        Whitebox.setInternalState(wavDumperNode, "debugEnabled", true);

        wavDumperNode.onEvent(mockNode, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        wavDumperNode.onEvent(mockNode, event);

    }

    @Test
    public void appendSuffixIfExists()
            throws IOException
    {
        String fileName = "appendSuffixIfExists";
        Path path = Paths.get(fileName);
        Files.deleteIfExists(path);

        Whitebox.setInternalState(wavDumperNode,"fileName", fileName);
        wavDumperNode.appendSuffixIfExists();
        assertEquals(fileName, Whitebox.getInternalState(wavDumperNode, "fileName"));

        Files.createFile(path);
        wavDumperNode.appendSuffixIfExists();
        assertNotEquals(fileName, Whitebox.getInternalState(wavDumperNode, "fileName"));

        Files.deleteIfExists(path);
    }
}

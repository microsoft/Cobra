package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseEvent;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.BoundedExecutorService;
import com.nuance.cobra.utils.audio.AudioChunk;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class SpeechRecoStubClientTest
        extends CobraTest
{
    private ResponseHandler mockResponseHandler;
    private SpeechRecoStubClient speechRecoStubClient;

    private final BoundedExecutorService boundedExecutorService = new BoundedExecutorService(5);

    @Captor
    private ArgumentCaptor<ResponseEvent> responseEventArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResponseResult> responseResultArgumentCaptor;

    @Before
    public void setUp()
            throws Exception
    {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        Session mockSession = mock(Session.class);
        mockResponseHandler = mock(ResponseHandler.class);

        TestUtils.inject(mockSessionManager.getClass(), "boundedExecutorService", boundedExecutorService);

        String sessionId = "testSessionId";
        when(mockSessionManager.getSession(sessionId, false)).thenReturn(mockSession);
        mockServerConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, true);
        mockServerConfig.setProperty(WebApiConfig.Property.STUB_RECO_DELAY_MILLISECONDS.key,100);
        mockServerConfig.setProperty(WebApiConfig.Property.STUB_RECO_SOS_EOS_DELAY_MILLISECONDS.key, 200);

        speechRecoStubClient = new SpeechRecoStubClient(sessionId, mockResponseHandler);
    }

    @After
    public void tearDown()
            throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testStartSpeechRecognition()
            throws Exception
    {
        speechRecoStubClient.startSpeechRecognition();

        byte[] audioBites = {(byte)0xff};
        AudioChunk audio = new AudioChunk(0, audioBites);
        speechRecoStubClient.onAudio(audio);

        verify(mockResponseHandler, timeout(5000).times(2)).sendEvent(responseEventArgumentCaptor.capture());
        verify(mockResponseHandler, timeout(5000).times(2)).onResponse(responseResultArgumentCaptor.capture());
        List<ResponseEvent> responseEvents = responseEventArgumentCaptor.getAllValues();
        List<ResponseResult> responseResults = responseResultArgumentCaptor.getAllValues();
        assertTrue(responseEvents.size() == 2);
        assertTrue(responseResults.size() == 2);

        assertEquals("startOfSpeech", responseEvents.get(0).getName());
        assertEquals("endOfSpeech", responseEvents.get(1).getName());

        assertEquals(responseResults.get(0).getClass(), SpeechRecoResponse.class);
        SpeechRecoResponse speechRecoResponse = (SpeechRecoResponse) responseResults.get(0);
        assertEquals(speechRecoResponse.getStatus(), ResponseResult.ResponseStatus.SUCCESS);
        assertEquals(speechRecoResponse.isFinal(), false);
        assertTrue(speechRecoResponse.getData().getTranscriptions().length > 0);

        assertEquals(responseResults.get(1).getClass(), SpeechRecoResponse.class);
        speechRecoResponse = (SpeechRecoResponse) responseResults.get(1);
        assertEquals(speechRecoResponse.getStatus(), ResponseResult.ResponseStatus.SUCCESS);
        assertEquals(speechRecoResponse.isFinal(), true);
        assertTrue(speechRecoResponse.getData().getTranscriptions().length > 0);

        assertNotNull(speechRecoResponse.getNoiseLevel());
        assertTrue(speechRecoResponse.getNoiseLevel().getMean() > 0);
        assertTrue(speechRecoResponse.getNoiseLevel().getStandardDeviation() > 0);
    }

    @Test
    public void testStopSpeechRecognition()
            throws Exception
    {
        speechRecoStubClient.stopSpeechRecognition();

        verifyNoInteractions(mockResponseHandler);
    }

    @Test
    public void testCancelSpeechRecognition()
            throws Exception
    {
        speechRecoStubClient.cancelSpeechRecognition(true);

        verify(mockResponseHandler, timeout(5000).times(1)).onResponse(responseResultArgumentCaptor.capture());
        ResponseResult result = responseResultArgumentCaptor.getValue();
        assertEquals(result.getStatus(), ResponseResult.ResponseStatus.CANCELLED);
        assertEquals(result.isFinal(), true);
    }

    @Test
    public void testOnAudio()
            throws Exception
    {
        byte[] audioBites = {(byte)0xff};
        AudioChunk audio = new AudioChunk(0, audioBites);
        speechRecoStubClient.onAudio(audio);
        assertTrue(speechRecoStubClient.gotSomeAudio());
    }

    @Test(expected = Test.None.class)
    public void startRecognitionTimers()
    {
        speechRecoStubClient.startRecognitionTimers();
    }

}
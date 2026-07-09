package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.BoundedExecutorService;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class SpeechSynthesisStubClientTest
        extends CobraTest
{
    private ResponseHandler mockResponseHandler;
    private SpeechSynthesisStubClient speechSynthesisStubClient;

    private final BoundedExecutorService boundedExecutorService = new BoundedExecutorService(5);

    @Captor
    private ArgumentCaptor<ResponseResult> responseResultArgumentCaptor;

    @Captor
    private ArgumentCaptor<byte[]> audioArgumentCaptor;

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
        mockServerConfig.setProperty(WebApiConfig.Property.STUB_TTS_BUFFER_SIZE.key, 100000);
        mockServerConfig.setProperty(WebApiConfig.Property.STUB_TTS_DELAY_MILLISECONDS.key, 500);

        speechSynthesisStubClient = new SpeechSynthesisStubClient(sessionId, mockResponseHandler);

        assertNotNull(speechSynthesisStubClient);
    }

    @After
    public void tearDown()
            throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testStartSpeechSynthesis()
            throws Exception
    {
        speechSynthesisStubClient.startSpeechSynthesis();

        verify(mockResponseHandler, timeout(60000).times(1)).onAudio(audioArgumentCaptor.capture(), anyBoolean());

        verify(mockResponseHandler, timeout(60000).times(1)).onResponse(responseResultArgumentCaptor.capture());

        ResponseResult result = responseResultArgumentCaptor.getValue();
        assertEquals(result.getStatus(), ResponseResult.ResponseStatus.SUCCESS);
        assertEquals(result.isFinal(), true);
    }

    @Test
    public void testStopSpeechSynthesis()
            throws Exception
    {
        speechSynthesisStubClient.stopSpeechSynthesis();

        verifyNoInteractions(mockResponseHandler);
    }

    @Test
    public void testIsSpeechSynthesisDone()
            throws Exception
    {
        assertEquals(true, speechSynthesisStubClient.isSpeechSynthesisDone());

        speechSynthesisStubClient.startSpeechSynthesis();

        assertEquals(false, speechSynthesisStubClient.isSpeechSynthesisDone());

        verify(mockResponseHandler, timeout(60000).times(1)).onAudio(audioArgumentCaptor.capture(), anyBoolean());

        verify(mockResponseHandler, timeout(60000).times(1)).onResponse(responseResultArgumentCaptor.capture());

        assertEquals(true, speechSynthesisStubClient.isSpeechSynthesisDone());
    }

}
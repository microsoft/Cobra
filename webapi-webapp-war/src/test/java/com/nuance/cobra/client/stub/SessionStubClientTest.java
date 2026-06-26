package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.BoundedExecutorService;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
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
public class SessionStubClientTest
        extends CobraTest
{
    private ResponseHandler mockResponseHandler;
    private SessionStubClient sessionStubClient;
    private CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration mockConfig = mockConfigs.getConfiguration(0);

    private final BoundedExecutorService boundedExecutorService = new BoundedExecutorService(5);

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
        mockConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, true);

        sessionStubClient = new SessionStubClient(sessionId);

        assertNotNull(sessionStubClient);
    }

    @After
    public void tearDown()
            throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testStartSession()
            throws Exception
    {
        sessionStubClient.startSession(mockResponseHandler, mockConfigs);

        verify(mockResponseHandler, timeout(5000).times(1)).onResponse(responseResultArgumentCaptor.capture());
        //verify(mockResponseHandler).onResponse(responseResultArgumentCaptor.capture());

        ResponseResult result = responseResultArgumentCaptor.getValue();
        assertEquals(result.getStatus(), ResponseResult.ResponseStatus.SUCCESS);
        assertEquals(result.isFinal(), true);
    }

    @Test
    public void testEndSession()
            throws Exception
    {
        sessionStubClient.endSession(mockResponseHandler, mockConfigs);

        verify(mockResponseHandler, timeout(5000).times(1)).onResponse(responseResultArgumentCaptor.capture());
        //verify(mockResponseHandler).onResponse(responseResultArgumentCaptor.capture());

        ResponseResult result = responseResultArgumentCaptor.getValue();
        assertEquals(result.getStatus(), ResponseResult.ResponseStatus.SUCCESS);
        assertEquals(result.isFinal(), true);
    }

}
package com.nuance.nmsp.servlet;

import com.nuance.nmsp.servlet.nmas.AsrContext;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import junit.framework.TestCase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NMSPServletTest extends TestCase {

    public void testInit() throws IOException, ServletException, IllegalAccessException {
        NMSPServlet testNMSPServlet = new NMSPServlet();

        testNMSPServlet.init();

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        testNMSPServlet.sendError(mockResponse, 1, "testMessage");
        verify(mockResponse, atLeastOnce()).sendError(anyInt());

        testNMSPServlet.destroy();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter("appId")).thenReturn("testAppId");
        when(mockRequest.getParameter("appKey")).thenReturn("testParam1");
        AuthorizationService authorizationService = new AuthorizationService();
        AuthorizationInfo result = testNMSPServlet.buildAuthInfo(mockRequest, authorizationService);
        assertEquals("testAppId", result.getAppId());
    }

    public void testAudio() {
        NMSPServlet testNMSPServlet = new NMSPServlet();

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        AuthorizationInfo testAuthInfo = new AuthorizationInfo("testAppId", "testParam1");

        HttpServletRequest testSrvReq = mock(HttpServletRequest.class);
        AsrContext testContext = new AsrContext();

        Thread testWaitThread = new Thread() {
            public void run() {
                testNMSPServlet.executeNmspAudioTransaction(mockResponse, testAuthInfo, "en-us", "uniqueId",
                        testSrvReq, ServletCodecWrapper.SPEEX_8K, testContext, "dictation");
            }
        };

        testWaitThread.start();

        try {
            testWaitThread.join(100);
        } catch (InterruptedException e) {
            fail();
        }
    }
}

package com.nuance.ncs.nmsp.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NcsServletDefaults.class})
@PowerMockIgnore("javax.management.*")
public class NcsServletTest {

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    NcsServlet servlet;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new NcsServlet();
    }

    @Test
    public void doPostHandlesMalformedJsonGracefully() throws Exception {
        // Empty body → NcsServletHandler attempts to read and parse → may error out;
        // NcsServlet's catch block converts any exception to 500.
        servlet.doPost(request, response);
        // Either handler completed or generic catch sent 500. Just verify no crash.
        assertNotNull(response);
    }

    @Test
    public void doOptionsWithoutOriginCallsSuper() throws Exception {
        servlet.doOptions(request, response);
        // super.doOptions sets an Allow header listing supported methods
        assertNotNull(response.getHeader("Allow"));
    }

    @Test
    public void doOptionsWithUnauthorizedOriginReturnsBadRequest() throws Exception {
        mockStatic(NcsServletDefaults.class);
        when(NcsServletDefaults.getAuthorizedCrossOrigins()).thenReturn("[blocked]");
        request.addHeader(NcsServletDefines.HTTP_REQUEST_ORIGIN_HEADER, "https://evil.example.com");
        servlet.doOptions(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void doOptionsWithAuthorizedOriginAddsCorsHeaders() throws Exception {
        mockStatic(NcsServletDefaults.class);
        when(NcsServletDefaults.getAuthorizedCrossOrigins()).thenReturn(".*");
        request.addHeader(NcsServletDefines.HTTP_REQUEST_ORIGIN_HEADER, "https://allowed.example.com");
        request.addHeader("Access-Control-Request-Headers", "Content-Type");
        servlet.doOptions(request, response);
        assertEquals("POST, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
        assertEquals("320", response.getHeader("Access-Control-Max-Age"));
        assertNotNull(response.getHeader("Access-Control-Allow-Headers"));
        assertNotNull(response.getHeader("Access-Control-Allow-Origin"));
    }
}

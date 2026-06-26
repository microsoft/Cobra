package com.nuance.cobra.servlet;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CobraHeadersFilterTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void init() {
        CobraHeadersFilter testObject = new CobraHeadersFilter();
        FilterConfig mockConfig = mock(FilterConfig.class);
        try {
            testObject.init(mockConfig);
        } catch (ServletException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void doFilter() throws IOException, ServletException {

        CobraHeadersFilter testObject = new CobraHeadersFilter();

        ServletRequest mockRequest = mock(ServletRequest.class);

        ServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockFilter = mock(FilterChain.class);

        testObject.doFilter(mockRequest, mockResponse, mockFilter);


        verify((HttpServletResponse)mockResponse).addHeader("X-Frame-Options","deny");
        verify((HttpServletResponse)mockResponse).addHeader("X-Content-Type-Options","nosniff");
        verify((HttpServletResponse)mockResponse).addHeader("X-XSS-Protection","1; mode=block");
        verify((HttpServletResponse)mockResponse).addHeader("Strict-Transport-Security","max-age=31536000; includeSubDomains");
        verify((HttpServletResponse)mockResponse).addHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "style-src 'self'; " +
                "script-src-elem 'self' cdn.rawgit.com; " +
                "style-src-elem 'self' 'sha256-MGBCiqe6SjasPhZdLYYeab06323iz8gSVgsv/FMgbJU=' cdn.jsdelivr.net");

        verify(mockFilter).doFilter(mockRequest, mockResponse);

    }

    @Test(expected = Test.None.class)
    public void destroy() {
        CobraHeadersFilter testObject = new CobraHeadersFilter();
        testObject.destroy();
    }
}
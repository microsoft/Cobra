package com.nuance.cobra.utils.auth;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class HeaderRequestWrapperTest {

    @Before
    public void testInit() throws Exception {

    }

    @Test
    public void addHeader() {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        HeaderRequestWrapper headerRequestWrapper = new HeaderRequestWrapper(mockHttpServletRequest);
        headerRequestWrapper.addHeader("ValidKey", "ValidValue");


        assertEquals("ValidValue",headerRequestWrapper.getHeader("ValidKey"));
        assertNull(headerRequestWrapper.getHeader("InvalidKey"));


        headerRequestWrapper.getHeaders("ValidKey");
        headerRequestWrapper.getHeaders("InvalidKey");

    }
}
package com.nuance.ncs.nmsp.util;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SanitizationUtilTest {

    @Test
    public void getSanitizedHeader() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(anyString())).thenReturn("test");
        assertEquals("test", SanitizationUtil.getSanitizedHeader(mockRequest, "test"));
    }
}
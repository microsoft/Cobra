package com.nuance.cobra.servlet;

import com.nuance.cobra.client.mix.MixClientCredentialsManager;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
public class AuthTokenCacheServletTest {

    @Test
    public void doPost() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        PrintWriter mockWriter = mock(PrintWriter.class);

        doReturn(mockWriter).when(mockResponse).getWriter();

        doReturn("Basic YXBwSUQlM0FOTURQVFJJQUxfc2FtZXJfa2Fya291dGlfbnVhbmNlX2NvbV8yMDE5MTAyNFQxNDE2MDgwMzYxNDQ6UENrVVNFTEZSeTRXUVFta3hTbDAxMmx1TW5lTlQ4Uy03dXZOV3EyZWN6dw==").when(mockRequest).getHeader("Authorization");

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("scope", new String[]{"tts"});
        parameterMap.put("authUri", new String[]{"local"});
        parameterMap.put("randomParam", new String[]{"random"});

        doReturn(Collections.enumeration(parameterMap.keySet())).when(mockRequest).getParameterNames();
        doReturn(parameterMap.get("scope")[0]).when(mockRequest).getParameter("scope");
        doReturn(parameterMap.get("authUri")[0]).when(mockRequest).getParameter("authUri");
        doReturn(parameterMap.get("authUri")[0]).when(mockRequest).getHeader("Mix-Auth-Uri");
        doReturn(parameterMap.get("randomParam")[0]).when(mockRequest).getParameter("randomParam");

        MixClientCredentialsManager.init();

        AuthTokenCacheServlet testServlet = new AuthTokenCacheServlet();
        testServlet.doPost(mockRequest, mockResponse);

        verify(mockWriter).print(anyString());
    }

    @Test
    public void doErrorPost() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        PrintWriter mockWriter = mock(PrintWriter.class);

        doReturn(mockWriter).when(mockResponse).getWriter();

        AuthTokenCacheServlet testServlet = new AuthTokenCacheServlet();
        testServlet.doPost(mockRequest, mockResponse);

        verify(mockWriter).print(anyString());
    }
}
package com.nuance.cobra.servlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ReadListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class TenantValidationServletTest {
    private TenantValidationServlet servlet;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;

    @Before
    public void setUp(){
        servlet = new TenantValidationServlet();
        httpServletRequest = Mockito.mock(HttpServletRequest.class);
        httpServletResponse = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        Mockito.when(httpServletResponse.getWriter()).thenReturn(writer);

        servlet.doGet(httpServletRequest, httpServletResponse);
        writer.flush();

        Assert.assertNotNull(stringWriter);
    }

    @Test
    public void testDoPost_fail() throws ServletException, IOException {
        String json = "{\"jwtIssuer\" : \"webapiQA\",\n" +
                "  \"jwtForceExpirationCheck\" : true,\n" +
                " \"jwtHashAlgorithmWhitelist\" : [ \"HS256\", \"HS384\" ],\n" +
                " \"activeAuthenticationMethods\" : [ \"jws\", \"hashcode\" ], \n" +
                "  \"sensitiity\" : \"open\"}";

        httpServletRequest.setAttribute("config", json);
        servlet.doPost(httpServletRequest, httpServletResponse);
        Assert.assertNotNull(httpServletResponse);
    }

    @Test
    public void testDoPost() throws Exception {
        String json = "{\"jwtIssuer\" : \"webapiQA\",\n" +
                "  \"jwtForceExpirationCheck\" : true,\n" +
                " \"jwtHashAlgorithmWhitelist\" : [ \"HS256\", \"HS384\" ],\n" +
                " \"activeAuthenticationMethods\" : [ \"jws\", \"hashcode\" ], \n" +
                "  \"sensitiity\" : \"open\"}";
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(httpServletRequest.getInputStream()).thenReturn(createServletInputStream(json));
        when(httpServletResponse.getWriter()).thenReturn(writer);
        servlet.doPost(httpServletRequest, httpServletResponse);
        String result = stringWriter.getBuffer().toString().trim();
        assertEquals("{\"sensitiity\":{\"Error\":\"Wrong Key\"}}", result);
        System.out.println(result);
    }

    @Test
    public void init() throws ServletException {
        servlet = new TenantValidationServlet();
        ServletConfig mockConf = mock(ServletConfig.class);
        when(mockConf.getServletName()).thenReturn("testServlet");
        servlet.init(mockConf);

        servlet.destroy();

        assertNotNull(servlet);
    }

    public ServletInputStream createServletInputStream(String object) {
        final InputStream byteArrayInputStream = new ByteArrayInputStream(object.getBytes());

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }
}

package com.nuance.cobra.status;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by xyz on 14/06/17.
 */
public class PingServletTest
{
    @Test
    public void doGet() throws Exception
    {
        PingServlet ss = new PingServlet();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        StringWriter out = new StringWriter();
        PrintWriter os = new PrintWriter(out);

        when(mockResponse.getWriter()).thenReturn(os);

        ss.doGet(mockRequest, mockResponse);

        assertTrue("Ping servlet did not answer with a PONG", out.toString().startsWith("PONG"));
    }

}
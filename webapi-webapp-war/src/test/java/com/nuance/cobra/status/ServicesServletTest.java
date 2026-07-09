package com.nuance.cobra.status;

import com.nuance.cobra.utils.HtmlGen;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.nuance.cobra.utils.HtmlGen.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by xyz on 14/06/17.
 */
public class ServicesServletTest
{
    @Test
    public void doServices() throws Exception
    {
        ServicesServlet ss = new ServicesServlet();

        final HtmlGen.HtmlElement html = HtmlGen.html();
        final HtmlGen.HeadElement head = html.head();
        head.stylesheet("services.css");
        head.title("Services");

        final HtmlGen.Element body = html.body();

        final HtmlGen.Element table = body.add(TABLE);
        final HtmlGen.Element header = table.add(TR);
        header.add(TH);
        header.add(TH, "Service Name");
        header.add(TH, "Type");
        header.add(TH, "Req.");
        header.add(TH, "Status");

        //I don't know how we can validate the execution of this method.
        //The only thing I can do right now is to exercise the code and hope no exception is thrown.
        ss.doServices(table);
    }

    @Test
    public void doGet() throws Exception
    {
        final String expectedResult = "<!DOCTYPE html>\n" +
                                      "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"services.css\"><title>Services</title>" +
                                      "</head><body><table><tr><th></th><th>Service Name</th><th>Type</th><th>Req.</th><th>Status</th></tr>" +
                                      "</table></body></html>";
        ServicesServlet ss = new ServicesServlet();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        ServletOutputStream os = new ServletOutputStream()
        {
            private StringBuilder data = new StringBuilder();
            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener)
            {

            }

            @Override
            public void write(int b) throws IOException
            {
                data.append((char) b);
            }

            public String toString()
            {
                return data.toString();
            }
        };
        when(mockResponse.getOutputStream()).thenReturn(os);

        ss.doGet(mockRequest, mockResponse);
        assertTrue("Generated HTML is different from the expected one", os.toString().equalsIgnoreCase(expectedResult));
    }

}
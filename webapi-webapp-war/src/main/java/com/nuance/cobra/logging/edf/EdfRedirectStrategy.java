package com.nuance.cobra.logging.edf;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import java.net.URI;

public class EdfRedirectStrategy extends DefaultRedirectStrategy
{
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException
    {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        int statusCode = response.getStatusLine()
                                 .getStatusCode();
        switch (statusCode)
        {
            case 301:
            case 302:
            case 303:
            case 307:
            case 308:
                return true;
            case 304:
            case 305:
            case 306:
            default:
                return false;
        }
    }

    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException
    {
        URI uri = this.getLocationURI(request, response, context);
        String method = request.getRequestLine()
                               .getMethod();
        if (method.equalsIgnoreCase("HEAD"))
        {
            return new HttpHead(uri);
        }
        else if (method.equalsIgnoreCase("GET"))
        {
            return new HttpGet(uri);
        }
        else
        {
            int status = response.getStatusLine()
                                 .getStatusCode();
            HttpUriRequest toReturn;
            if (status == 307 || status == 308)
            {
                toReturn = RequestBuilder.copy(request)
                                         .setUri(uri)
                                         .build();
                //toReturn.removeHeaders("Content-Length"); //Workaround for an apparent bug in HttpClient
            }
            else
            {
                toReturn = new HttpGet(uri);
            }
            return toReturn;
        }
    }

}

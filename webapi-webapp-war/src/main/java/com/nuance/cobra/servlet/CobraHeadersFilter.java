package com.nuance.cobra.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CobraHeadersFilter implements Filter
{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        //Sonarqube deodorant
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException
    {
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.addHeader("X-Frame-Options","deny");
        httpResponse.addHeader("X-Content-Type-Options","nosniff");
        httpResponse.addHeader("X-XSS-Protection","1; mode=block");
        httpResponse.addHeader("Strict-Transport-Security","max-age=31536000; includeSubDomains");
        httpResponse.addHeader("Content-Security-Policy",
                "default-src 'self'; " +
                    "style-src 'self'; " +
                    "script-src-elem 'self' cdn.rawgit.com; " +
                    "style-src-elem 'self' 'sha256-MGBCiqe6SjasPhZdLYYeab06323iz8gSVgsv/FMgbJU=' cdn.jsdelivr.net");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
        //Sonarqube deodorant
    }
}
package com.nuance.cobra.utils.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebFilter("/websocket")

public class WebsocketFilter implements Filter{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final Map<String, String[]> props = new HashMap<>();
        // Add properties of interest from session; session ID
        // is just for example
        props.put("authentication", new String[] {
                httpRequest.getHeader("authentication")
        });
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
            @Override
            public Map<String, String[]> getParameterMap() {
                return props;
            }
        };
        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException { }
    @Override
    public void destroy() { }
}

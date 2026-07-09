package com.nuance.cobra.utils.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class HeaderRequestWrapper extends HttpServletRequestWrapper {
    private Map<String,List<String>> additionalHeaders = new HashMap<String,List<String>>();

    public HeaderRequestWrapper (HttpServletRequest request) {
        super(request);
    }

    public HeaderRequestWrapper addHeader(String name, String value) {
        List<String> values = additionalHeaders.get(name);
        if (values == null) {
            additionalHeaders.put(name, values = new LinkedList<String>());
        }
        values.add(value);
        return this;
    }

    public Enumeration<String> getHeaders(String name) {
        if (additionalHeaders.containsKey(name)) {
            return Collections.enumeration(additionalHeaders.get(name));
        }
        return super.getHeaders(name);
    }

    public String getHeader(String name) {
        if (additionalHeaders.containsKey(name)) {
            List<String> values = additionalHeaders.get(name);
            return values.size() > 0 ? values.get(0) : null;
        }
        else {
            return super.getHeader(name);
        }
    }

    public Enumeration getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(additionalHeaders.keySet());
        return Collections.enumeration(names);
    }
}

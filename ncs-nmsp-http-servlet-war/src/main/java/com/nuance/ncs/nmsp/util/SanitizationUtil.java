package com.nuance.ncs.nmsp.util;

import org.owasp.esapi.ESAPI;

import javax.servlet.http.HttpServletRequest;

public class SanitizationUtil
{
    private SanitizationUtil() {
        //hide constructor
    }

    public static String getSanitizedHeader(HttpServletRequest request, String header)
    {
        return ESAPI.encoder().encodeForHTML(request.getHeader(header));
    }
}

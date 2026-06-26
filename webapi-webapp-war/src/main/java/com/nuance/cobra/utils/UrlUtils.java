package com.nuance.cobra.utils;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class UrlUtils
{
    private static final Logger logger = LogManager.getLogger(UrlUtils.class);

    UrlUtils() { throw new IllegalStateException("Utility class!"); }

    public static URL getServerUrl(String urlString)
    {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            logger.error("Invalid url: {}", urlString, e);
            return null;
        }
        return url;
    }

    public static String getServerHost(URL url) {
        if (url == null) return null;
        return url.getHost();
    }

    public static Integer getServerPort(URL url) {
        if(url == null) return null;
        if(url.getPort() == -1) return url.getDefaultPort();
        return url.getPort();
    }

    public static String getProtocol(URL url)
    {
        if (url == null) return null;
        return url.getProtocol();
    }

    public static boolean isPlainTextProtocol(URL url)
    {
        if (url == null) return false;
        return isPlainTextProtocol(url.getProtocol());
    }


    public static boolean isPlainTextProtocol(String protocol)
    {
        if(protocol == null) return false;
        switch (protocol.toLowerCase())
        {
            case "https":
                return false;
            case "grpcs":
                return false;
            case "wss":
                return false;
            case "rediss":
                return false;
            //https://en.wikipedia.org/wiki/List_of_URI_schemes
            case "aaas":
                return false;
            case "coaps":
                return false;
            case "ssh":
                return false;
            case "sips":
                return false;
            case "ipps":
                return false;
            case "ircs":
                return false;
            case "iris.xpcs":
                return false;
            case "stuns":
                return false;
            case "turns":
                return false;
            case "xmlrpc.beeps":
                return false;
            default:
                return true;
        }
    }

    public static boolean isSSRF(String uriString)
    {
        try
        {
            URI uri = new URI(uriString);
            return isSSRF(uri);
        }
        catch (Exception e)
        {
            return true;
        }
    }

    public static boolean isSSRF(URI uri)
    {
        if (!isHttpScheme(uri.getScheme()))
        {
            return true;
        }
        return !DomainValidator.getInstance(true)
                               .isValid(uri.getHost());
    }

    public static boolean isHttpScheme(String urlScheme)
    {
        return urlScheme.matches("https?");
    }

    public static void initalize()
    {
        DomainValidator.updateTLDOverride(DomainValidator.ArrayType.LOCAL_PLUS, new String[]{"local"}); //k8s svc.cluster.local
    }
}

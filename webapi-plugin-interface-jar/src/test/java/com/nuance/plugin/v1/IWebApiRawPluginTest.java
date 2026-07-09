package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

public class IWebApiRawPluginTest
{

    @Test
    public void webApiRawPluginTest()
            throws IOException, ServletException, WebApiCommonException {
        final String URL_PATTERN = "/test-pattern/*";
        IWebApiRawPlugin testPlugin = new IWebApiRawPlugin()
        {
            @Override
            public String getUrlPattern()
            {
                return URL_PATTERN;
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList)
            {
                // intentionally left empty
            }
        };

        assertEquals(URL_PATTERN, testPlugin.getUrlPattern());

        assertFalse(testPlugin.onSocketMessage(null, null));
        assertFalse(testPlugin.onSocketOpen(null));
        assertFalse(testPlugin.onHttpPost(null, null));
        assertFalse(testPlugin.onHttpGet(null, null));
        assertFalse(testPlugin.onHttpDelete(null, null));
        assertFalse(testPlugin.onHttpPut(null, null));
        assertFalse(testPlugin.onSocketBinary(null, null));
        assertFalse(testPlugin.onSocketClosed(null, null));
        assertFalse(testPlugin.onSocketPong(null, null));
        assertFalse(testPlugin.onSocketError(null, null));

        assertFalse(testPlugin.getPluginFeatures().getJwtValidationsConfigurator().isPresent());
    }

}
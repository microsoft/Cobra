package com.nuance.cobra.plugin;

import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.junit.Test;

import javax.websocket.PongMessage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class WebApiPluginTest {
    IWebApiRawPlugin plugin = new IWebApiRawPlugin() {
        @Override
        public String getUrlPattern() {
            return "test";
        }

        @Override
        public void onInitPlugin(IWebApiFeatures featuresList) {
        }
    };

    @Test
    public void getPlugin() throws IOException {
        WebApiPlugin<IWebApiRawPlugin> testPlugin = new WebApiPlugin<>(plugin, "testTenant", "plugin");

        javax.websocket.Session Session = mock(javax.websocket.Session.class);

        assertEquals("plugin", testPlugin.getPluginName());
        assertEquals("testTenant", testPlugin.getTenantName());

        assertFalse(plugin.onSocketOpen(Session));
        assertFalse(plugin.onSocketMessage(Session, "test"));
        assertFalse(plugin.onSocketBinary(Session, "test".getBytes()));
        assertFalse(plugin.onSocketPong(Session, new PongMessage() {
            @Override
            public ByteBuffer getApplicationData() {
                return null;
            }
        }));

        assertFalse(plugin.onSocketClosed(Session, null));
        assertFalse(plugin.onSocketError(Session, null));
    }
}
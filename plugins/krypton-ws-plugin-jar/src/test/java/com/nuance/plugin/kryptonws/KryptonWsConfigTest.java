package com.nuance.plugin.kryptonws;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class KryptonWsConfigTest
{

    @Test
    public void generateMarkdownDocs() {
        assertFalse((Boolean) KryptonWsConfig.Property.KRYPTON_WS_CLOSE_WS_SESSION.getDefaultValue());
        assertEquals("Boolean indicating if we close the session.",
                KryptonWsConfig.Property.KRYPTON_WS_CLOSE_WS_SESSION.getDescription());
        assertEquals("KRYPTON_WS_CLOSE_WS_SESSION", KryptonWsConfig.Property.KRYPTON_WS_CLOSE_WS_SESSION.getConstantName());
        assertEquals("kryptonWsCloseWsSession", KryptonWsConfig.Property.KRYPTON_WS_CLOSE_WS_SESSION.getKey());
    }

}
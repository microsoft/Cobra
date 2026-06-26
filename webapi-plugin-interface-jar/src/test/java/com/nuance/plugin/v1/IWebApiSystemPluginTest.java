package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IWebApiSystemPluginTest
{

    @Test
    public void webApiSystemPluginTest(){
        IWebApiSystemPlugin testPlugin = new IWebApiSystemPlugin() {
        };

        assertFalse(testPlugin.onServerStart(null));
        testPlugin.onServerStop();
    }

}
package com.nuance.cobra.plugin;

import org.junit.Test;

import static org.junit.Assert.*;

public class PluginServiceLoaderTest
{
    @Test
    public void loadAndIterateTest()
    {
        PluginServiceLoader<TestInterface> services = PluginServiceLoader.load(TestInterface.class,
                Thread.currentThread()
                      .getContextClassLoader());

        for (TestInterface service : services)
        {
            service.doSomething();
        }

        assertTrue(TestInterfaceImpl.nbInvocations == 1);
    }
}
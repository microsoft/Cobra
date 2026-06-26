package com.nuance.plugin.nmsp.v1;

import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class MainThreadTest {

    @Test
    public void testRun() throws InterruptedException {

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
        config.setProperty(PdxInterfaceConfig.Property.SSL_ENABLED.getKey(), false);

        CombinedConfiguration mockConfig = PowerMockito.mock(CombinedConfiguration.class);
        PowerMockito.when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(config);

        IWebApiFeatures testFeatures = new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return mockConfig;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return mockConfig;
            }

            @Override
            public void close() { }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) throws IOException {
                return null;
            }
        };

        MainThread testThread = new MainThread(testFeatures);
        testThread.start();

        Thread.yield();
        Thread.sleep(10);

        assertTrue(testThread.isAlive());

        do {
            try {
                testThread.terminate();
                break;
            } catch (NullPointerException e) {
                //waiting for server to start.
            }
            Thread.yield();
            Thread.sleep(10);
        } while (true);
    }
}
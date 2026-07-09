package com.nuance.plugin.nmsp.v1;

import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import junit.framework.TestCase;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;

public class PdxInterfacePluginTest extends TestCase {

    public void testOnServerStart() throws InterruptedException {

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
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

        PdxInterfacePlugin plugin = new PdxInterfacePlugin();
        assertTrue(plugin.onServerStart(testFeatures));

        // wait for main thread to start.
        Thread.yield();
        Thread.sleep(100);
    }
}
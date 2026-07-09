package com.nuance.plugin.nmsp.v1.nmspgw.server;

import com.nuance.cobra.utils.Tools;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.server.utils.Quiesce;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*", "jdk.xml.*", "java.xml.*", "javax.xml.*", "com.sun.org.apache.xerces.*", "javax.management.*", "org.w3c.dom.*", "sun.security.*", "javax.net.ssl.*"})
@PrepareForTest({Tools.class})
public class NVCGWServerTest {

    @Test
    public void onApplicationEvent() throws IOException, InterruptedException {
        String currentPath = Tools.getJettyPath();
        mockStatic(Tools.class);
        PowerMockito.when(Tools.getJettyPath()).thenReturn(currentPath + "/src/test/resources/");

        Quiesce testQuiesce = new Quiesce(1, 1);

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
        config.setProperty(PdxInterfaceConfig.Property.SSL_ENABLED.getKey(), false);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(config);

        IWebApiFeatures testFeatures = new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() {
                return mockConfig;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
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

        NVCGWServer testServer = new NVCGWServer(testQuiesce, testFeatures);

        testServer.onApplicationEvent("start");
        Thread.yield();

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 8911));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        testServer.onApplicationEvent("stop");

        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 8911));
            fail("Connection should have failed.");
        } catch (IOException e) {
            //success
        }
    }
}
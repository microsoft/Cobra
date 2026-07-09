package com.nuance.cobra.servlet;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class})
@PowerMockIgnore({"javax.net.ssl.*","org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class WebApiPlatformServletTest {

    private Configuration testConf;
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();

    @Before
    public void setUp()
            throws Exception {
        testConf = testConfs.getConfiguration(0);
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfs);
    }

    @Test
    public void init() throws ServletException {
        WebApiPlatformServlet testServlet = new WebApiPlatformServlet();
        ServletConfig mockConf = mock(ServletConfig.class);
        when(mockConf.getServletName()).thenReturn("testServlet");
        testServlet.init(mockConf);

        testServlet.destroy();
    }

}
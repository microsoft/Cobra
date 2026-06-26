package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Executors.class, WebApiConfigManager.class})
@SuppressStaticInitializationFor({"com.nuance.cobra.utils.http.CachingHttpClient"})
@PowerMockIgnore({"javax.net.ssl.*","javax.security.*","org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class HttpErrorListenerTest {
    private ExecutorService executor = Executors.newCachedThreadPool();
    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration testConfig = testConfigs.getConfiguration(0);
    private String cfgPath =  WebApiConfigManager.getConfigPath();

    @Before
    public void testSetup () {
        PowerMockito.mockStatic(Executors.class);
        PowerMockito.when(Executors.newCachedThreadPool()).thenReturn(executor);

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfigs);
        PowerMockito.when(WebApiConfigManager.getConfigPath()).thenReturn(cfgPath);
    }

    @Test
    public void testNotifyAlarmDisabled() throws InterruptedException {
        IResponse mockResponse = mock(IResponse.class);

        HttpErrorListener testObj = new HttpErrorListener();
        ThreadContext.put(ThreadContextRunnable.TENANT_ID, "test");
        testObj.notify(mockResponse);
        assertTrue(((ThreadPoolExecutor)executor).getQueue().isEmpty());
        Thread.yield();
        executor.shutdown(); //force execution
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        List<Runnable> notExecutedTasks = executor.shutdownNow();
        assertTrue(notExecutedTasks.isEmpty());
        assertTrue(((ThreadPoolExecutor)executor).getCompletedTaskCount() == 0);
    }

    @Test
    public void testNotifyAlarmEnabled() throws InterruptedException {
        IResponse mockResponse = mock(IResponse.class);

        //Set an endpoit to enable the alarm
        testConfig.setProperty(WebApiConfig.Property.HTTP_ALARM_ENDPOINT_URL.key, "http://127.0.0.1/");
        testConfig.setProperty(WebApiConfig.Property.HTTP_ALARM_TEMPLATE_FILE.key, "velocity_alert.vm.tmpl");

        HttpErrorListener testObj = new HttpErrorListener();
        ThreadContext.put(ThreadContextRunnable.TENANT_ID, "test");
        testObj.notify(mockResponse);
        Thread.yield();
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
        List<Runnable> notExecutedTasks = executor.shutdownNow();
        assertTrue(notExecutedTasks.isEmpty());
    }
}
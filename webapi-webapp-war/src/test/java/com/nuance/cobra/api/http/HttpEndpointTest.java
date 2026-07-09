package com.nuance.cobra.api.http;

import com.nuance.cobra.plugin.PluginLoader;
import com.nuance.cobra.plugin.WebApiPlugin;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.nuance.cobra.utils.errors.RestPluginsError.JSON_PARSING_ERROR;
import static com.nuance.cobra.utils.errors.RestPluginsError.UNAUTHORIZED;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpEndpointTest.class, PluginLoader.class, WebApiConfigManager.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class HttpEndpointTest {
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);
    private CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration config = configs.getConfiguration(0);

    @Test
    public void doNegativeHttpMethods() {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("webapi/tenant/test");

        try {
            endpoint.doGet(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        try {
            endpoint.doPost(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        
        try {
            endpoint.doPut(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        try {
            endpoint.doDelete(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void doHttpMethodsFailedAuth() {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("/webapi/tenant/test");
        mockStatic(PluginLoader.class);

        List<WebApiPlugin<IWebApiRawPlugin>> plugins = new ArrayList<>();
        WebApiPlugin plugin = new WebApiPlugin(new IWebApiRawPlugin() {
            @Override
            public String getUrlPattern() {
                return "tenant/test/";
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {

            }
        }, "webapi", "test");

        plugins.add(plugin);

        try {
            PowerMockito.when(PluginLoader.loadRawPlugins("webapi")).thenReturn(plugins);
            PowerMockito.when(PluginLoader.buildPluginConfigs("webapi", "test")).thenReturn(CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig());
        } catch (IOException | CobraException e) {
            fail();
        }

        try {
            endpoint.doGet(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void doHttpMethods() {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("/webapi/tenant/test/");
        mockStatic(PluginLoader.class);

        config.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, "no_auth" );
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.validateTenantKey(anyString())).thenReturn(true);

        List<WebApiPlugin<IWebApiRawPlugin>> plugins = new ArrayList<>();
        WebApiPlugin plugin = new WebApiPlugin(new IWebApiRawPlugin() {
            @Override
            public String getUrlPattern() {
                return "tenant/test/";
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {

            }
        }, "webapi", "test");

        plugins.add(plugin);
        try {
            PowerMockito.when(PluginLoader.loadRawPlugins("webapi")).thenReturn(plugins);
            PowerMockito.when(plugin.getPluginSettings()).thenReturn(configs);
            PowerMockito.when(PluginLoader.buildPluginConfigs("webapi", "test")).thenReturn(configs);
        } catch (IOException | CobraException e) {
            fail();
        }

        try {
            endpoint.doGet(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        try {
            endpoint.doPost(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        try {
            endpoint.doPut(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        try {
            endpoint.doDelete(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void quietErrorResponse() {
        HttpEndpoint.quietErrorResponse(response, 10);
        try {
            verify(response, times(1)).sendError(10);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void wrongPluginFrameworkTest() throws IOException, ServletException {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("/webapi/tenant/wrong_path/");
        mockStatic(PluginLoader.class);

        config.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, "no_auth" );
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.validateTenantKey(anyString())).thenReturn(true);

        List<WebApiPlugin<IWebApiRawPlugin>> plugins = new ArrayList<>();
        WebApiPlugin plugin = new WebApiPlugin(new IWebApiRawPlugin() {
            @Override
            public String getUrlPattern() {
                return "tenant/test/";
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {

            }
        }, "webapi", "test");

        plugins.add(plugin);
        try {
            PowerMockito.when(PluginLoader.loadRawPlugins("webapi")).thenReturn(plugins);
            PowerMockito.when(plugin.getPluginSettings()).thenReturn(configs);
            PowerMockito.when(PluginLoader.buildPluginConfigs("webapi", "test")).thenReturn(configs);
        } catch (IOException | CobraException e) {
            fail();
        }

        endpoint.doGet(request, response);
        verify(response, times(1)).sendError(404);
    }

    @Test
    public void wrongTenantTest() {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("/webapi/tenants/tests");
        mockStatic(PluginLoader.class);

        config.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, "no_auth" );
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.validateTenantKey(anyString())).thenReturn(true);


        try {
            PowerMockito.when(PluginLoader.loadRawPlugins("webapi")).thenThrow(IOException.class);
            endpoint.doGet(request, response);
            verify(response, times(1)).sendError(404);

        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void callPluginUnauthorized() throws IOException {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("/webapi/tenant/test/");
        mockStatic(PluginLoader.class);

        config.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, "jws" );
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.validateTenantKey(anyString())).thenReturn(true);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockWriter);

        List<WebApiPlugin<IWebApiRawPlugin>> plugins = new ArrayList<>();
        WebApiPlugin plugin = new WebApiPlugin(new IWebApiRawPlugin() {
            @Override
            public String getUrlPattern() {
                return "tenant/test/";
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {

            }
        }, "webapi", "test");

        plugins.add(plugin);
        try {
            PowerMockito.when(PluginLoader.loadRawPlugins("webapi")).thenReturn(plugins);
            PowerMockito.when(plugin.getPluginSettings()).thenReturn(configs);
            PowerMockito.when(PluginLoader.buildPluginConfigs("webapi", "test")).thenReturn(configs);
        } catch (IOException | CobraException e) {
            fail();
        }

        try {
            endpoint.doPost(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        JSONObject obj = new JSONObject();
        obj.put("errorCode", UNAUTHORIZED.getValue());

        verify(mockWriter, times(1)).write(obj.toString());
    }

    public void callPluginThrowPluginException() throws IOException {
        HttpEndpoint endpoint = new HttpEndpoint();
        when(request.getPathInfo()).thenReturn("/webapi/tenant/test/");
        mockStatic(PluginLoader.class);

        config.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, "no_auth" );
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.validateTenantKey(anyString())).thenReturn(true);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockWriter);

        List<WebApiPlugin<IWebApiRawPlugin>> plugins = new ArrayList<>();
        WebApiPlugin plugin = new WebApiPlugin(new IWebApiRawPlugin() {
            @Override
            public String getUrlPattern() {
                return "tenant/test/";
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {

            }

            @Override
            public boolean onHttpPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, WebApiCommonException {
                throw new WebApiCommonException(JSON_PARSING_ERROR);
            }
        }, "webapi", "test");

        plugins.add(plugin);
        try {
            PowerMockito.when(PluginLoader.loadRawPlugins("webapi")).thenReturn(plugins);
            PowerMockito.when(plugin.getPluginSettings()).thenReturn(configs);
            PowerMockito.when(PluginLoader.buildPluginConfigs("webapi", "test")).thenReturn(configs);
        } catch (IOException | CobraException e) {
            fail();
        }

        try {
            endpoint.doPost(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }

        JSONObject obj = new JSONObject();
        obj.put("Error Code", JSON_PARSING_ERROR.getValue());

        verify(mockWriter, times(1)).write(obj.toString());
    }

}
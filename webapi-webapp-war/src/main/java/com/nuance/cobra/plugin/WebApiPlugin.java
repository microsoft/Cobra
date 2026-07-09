package com.nuance.cobra.plugin;

import com.nuance.cobra.utils.CobraException;
import com.nuance.plugin.v1.IWebApiTenantPlugin;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.io.IOException;

public class WebApiPlugin <T extends IWebApiTenantPlugin> {
    private final T plugin;
    private final String tenantName;
    private final String pluginName;

    public WebApiPlugin(T plugin, String tenantName, String pluginName) {
        this.plugin = plugin;
        this.tenantName = tenantName;
        this.pluginName = pluginName;
    }

    public T getPlugin() {
        return plugin;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public CombinedConfiguration getPluginSettings() throws IOException {
        try {
            return PluginLoader.buildPluginConfigs(tenantName, pluginName);
        } catch (CobraException e) {
            throw new IOException(e);
        }
    }
}

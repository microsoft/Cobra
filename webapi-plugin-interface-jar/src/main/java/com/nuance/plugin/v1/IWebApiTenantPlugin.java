package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IWebApiFeatures;

public interface IWebApiTenantPlugin extends IWebApiPlugin {
    /**
     * Called when WebApi initialized its plugins.
     * This method will only be called once per tenant. Loaded plugins will be cached.
     * @param featuresList  List of features that this webApi offers (and maybe allow the plugin to disable?)
     */
    void onInitPlugin(IWebApiFeatures featuresList);

    /**
     * Returns the supported plugin features. The object will always be present.
     * @return Plugin features.
     */
    default IPluginFeatures getPluginFeatures() {
        return new IPluginFeatures() {};
    }
}

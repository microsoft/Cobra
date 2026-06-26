package com.nuance.cobra.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.plugin.v1.shareable.edf.IEdfMessage;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;


/**
 * for handling EDF v1 messages.

 * This version is used for the  V1 messages, and is fully backward compatible with the existing plugin framework
 * and plugin (most of the implementation reused from anonymous class previously defined in the
 */
public class PluginEdfV1MessageQueueable
        extends PluginEdfMessageQueueableBase
{
    private IEdfMessage logV1;

    PluginEdfV1MessageQueueable(IEdfMessage log, String tenantName, CombinedConfiguration config)
    {
        super(log, tenantName, config);
        this.logV1 = log;
    }

    @Override
    public String toJson(boolean prettyPrint)
            throws JsonProcessingException
    {
        JSONObject logJson = new JSONObject();
        logJson.put("API", logV1.getApiName());
        logJson.put("logId", logV1.getLogId());
        logJson.put("commandLogString", logV1.toJson(prettyPrint));
        logJson.put("tenantKey", getTenantKey());
        logJson.put("enabledPosting", postingEnabled());
        logJson.put("clientApplicationId",
                config.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key));
        logJson.put("clientApplicationVersion",
                config.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key));
        return logJson.toString();
    }

}

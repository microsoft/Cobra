package com.nuance.cobra.plugin;

import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.plugin.v1.shareable.edf.IEdfMessageBase;
import org.apache.commons.configuration2.CombinedConfiguration;


/**
 * Common base for the V1 and V2 version of the message queueable. these class provide a bridge between the plugin
 * framework log messages and the ones used by EDF log posting.
 */
public abstract class PluginEdfMessageQueueableBase
        implements EdfMessageQueueable
{
    private IEdfMessageBase log;
    private String tenantName;

    protected CombinedConfiguration config;

    PluginEdfMessageQueueableBase(IEdfMessageBase log, String tenantName, CombinedConfiguration config)
    {
        this.log = log;
        this.tenantName = tenantName;
        this.config = config;
    }

    @Override
    public JmsMessageType getJmsType()
    {
        switch (log.getMessageType())
        {
            case binaryBlobMessage:
                return JmsMessageType.AUDIO_LOG;
            case dataMessage:
            default:
                return JmsMessageType.EDF_LOG;
        }
    }

    @Override
    public String getTenantKey()
    {
        return tenantName;
    }

    @Override
    public boolean postingEnabled()
    {
        return config.getBoolean(WebApiConfig.Property.EDF_POSTING_ENABLED_TENANT.key);
    }

    @Override
    public WebApiConfig.Property getEdfEndpointUrlProperty()
    {
        return getJmsType().getUrlPathProperty();
    }


    /**
     * Get the log returned by the plugin. Subclasses can override with a more specific type.
     */
    IEdfMessageBase getPluginLog() {
        return log;
    }
}

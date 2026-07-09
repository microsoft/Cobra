package com.nuance.cobra.plugin;


import com.nuance.cobra.logging.edf.EdfMessageQueueableConstants;
import com.nuance.plugin.v1.shareable.edf.IEdfV2Message;
import org.apache.commons.configuration2.CombinedConfiguration;


/**
 * For handling NEAP messages from plugins.
 */
public class PluginNeapMessageQueueable
        extends PluginEdfV2MessageQueueable
{
    PluginNeapMessageQueueable(IEdfV2Message log, String tenantName, CombinedConfiguration config) {
        super(log, tenantName, config);
    }

    @Override
    public String getEdfLogTag()
    {
        return EdfMessageQueueableConstants.BASE_NEAP_LOG_TAG +
               EdfMessageQueueableConstants.NEAP_V1_LOG_TAG + getPluginLog().getPluginLogTag();
    }
}

package com.nuance.plugin.v1.shareable.edf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


/**
 * Message interface for plugins using the EDF v2 format.
 */
public interface IEdfV2Message
        extends IEdfMessageBase
{
    /**
     * The plugin log tag is used to tag Edf logs in the traces. It is a qualified string (e.g. "pluginLog.special.")
     * The plugin EDF poster will use this value to build the tag. (With this default will result in plugin traces
     * being tagged with "edfLog.v2.pluginLog.(tenant-key)"
     * Plugins could override this to tag their logs with a more specific value/
     */
    @JsonIgnore
    default String getPluginLogTag()
    {
        return EdfMessageConstants.DEFAULT_PLUGIN_LOG_TAG;
    }


    @JsonProperty("schemaversion")
    default String getSchemaVersion()
    {
        return EdfMessageConstants.EDF_V2_VERSION_STRING;
    }

    /**
     * Return the channel
     * @return channel
     */
    default String getChannel()
    {
        return EdfMessageConstants.DEFAULT_CHANNEL;
    }

    /**
     * Return the application Id
     * @return
     */
    default String getApplication()
    {
        return EdfMessageConstants.DEFAULT_APPLICATION;
    }

    /**
     * Return the source identifier, defined as customer/application/channel.
     * @return Source identifier
     */
    default String getSourceIdentifier()
    {
        return getCustomer() + "/" + getApplication() + "/" + getChannel();
    }

    /**
     * Return the customer. In Web API the customer is mapped to the "projectId" in the configuration, and
     * referred to as ClientID in the implementation.
     * @return Customer
     */
    String getCustomer();

    @JsonProperty("sessionid")
    String getSessionId();

    /**
     * Return a property map.
     * @return Sequence number
     */
    default Map<String,String> getProperties()
    {
        return null;
    }

    /**
     * Return the sequence number, an incrementing index for each command/event within a WebAPI session
     * @return Sequence number
     */
    Integer getSequenceNumber();

    /**
     * Return a timestamp for this log. It is the number of milliseconds since Unix Epoch.
     * A null value will be replaced by the current system time before logging the request.
     * @return Sequence number
     */
    default String getTimestamp()
    {
        return null;
    }

    /**
     * Return client application id (i.e. of the application using the plugin.)
     * Values from the tenant config will be used if the plugin return null (default)
     */
    default String getClientApplicationId()
    {
        return null;
    }

    /**
     * Return the sequence number, an incrementing index for each command/event within a WebAPI session
     * Values from the tenant config will be used if the plugin return null (default)
     * @return Sequence number
     */
    default String getClientApplicationVersion()
    {
        return null;
    }

    /**
     * Return the application payload in JSON format.
     * @return data field
     */
    String getData();

}

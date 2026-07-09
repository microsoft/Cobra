package com.nuance.cobra.logging.edf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.session.WebApiConfig;

public interface EdfMessageQueueable
{
    enum EdfMessageFormat {
        EDF_V1_FORMAT,
        EDF_V2_FORMAT,
        NEAP_V1_FORMAT,
        NEAP_V2_FORMAT
        }

    enum JmsMessageType {
        AUDIO_LOG(WebApiConfig.Property.EDF_BINARY_ENDPOINT),
        EDF_LOG(WebApiConfig.Property.EDF_DATA_ENDPOINT);

        JmsMessageType(WebApiConfig.Property urlPorp) {
            this.urlPathProperty = urlPorp;
        }
        private WebApiConfig.Property urlPathProperty;

        public WebApiConfig.Property getUrlPathProperty() {
            return urlPathProperty;
        }
    }

    String toJson(boolean prettyPrint) throws JsonProcessingException;

    JmsMessageType getJmsType();

    String getTenantKey();

    /**
     * returns the message format supported. Default is for edf v1, but some implementations may support V2
     * (or both depending on other criteria)
     */
    @JsonIgnore
    default EdfMessageFormat getEdfMessageFormat()
    {
        return EdfMessageFormat.EDF_V1_FORMAT;
    }

    /**
     * Returns the tag that will be used in the logs to mark the data that must be sent to EDF.
     * @return EDF tag
     */
    @JsonIgnore
    default String getEdfLogTag()
    {
        String tag = "";
        if (getEdfMessageFormat().equals(EdfMessageFormat.EDF_V1_FORMAT))
        {
            tag += EdfMessageQueueableConstants.BASE_EDF_LOG_TAG;
        }
        else if (getEdfMessageFormat().equals(EdfMessageFormat.EDF_V2_FORMAT))
        {
            tag += EdfMessageQueueableConstants.BASE_EDF_LOG_TAG;
            tag += EdfMessageQueueableConstants.EDF_V2_LOG_TAG;
        }
        else if (getEdfMessageFormat().equals(EdfMessageFormat.NEAP_V1_FORMAT))
        {
            tag += EdfMessageQueueableConstants.BASE_NEAP_LOG_TAG;
            tag += EdfMessageQueueableConstants.NEAP_V1_LOG_TAG;
        }
        else if (getEdfMessageFormat().equals(EdfMessageFormat.NEAP_V2_FORMAT))
        {
            tag += EdfMessageQueueableConstants.BASE_NEAP_LOG_TAG;
            tag += EdfMessageQueueableConstants.NEAP_V2_LOG_TAG;
        }

        if (getJmsType().equals(JmsMessageType.EDF_LOG))
        {
            tag += EdfMessageQueueableConstants.COMMAND_LOG_TAG;
        }
        else if (getJmsType().equals(JmsMessageType.AUDIO_LOG))
        {
             tag += EdfMessageQueueableConstants.AUDIO_LOG_TAG;
        }

        return tag;
    }

    boolean postingEnabled();

    WebApiConfig.Property getEdfEndpointUrlProperty();
}

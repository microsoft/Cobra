package com.nuance.cobra.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.logging.edf.EdfMessageQueueableConstants;
import com.nuance.plugin.v1.shareable.edf.IEdfMessageBase;
import com.nuance.plugin.v1.shareable.edf.IEdfV2Message;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.io.IOException;
import java.io.StringWriter;

/**
 * For handling EDF v2 messages.
 */
public class PluginEdfV2MessageQueueable
        extends PluginEdfMessageQueueableBase
{

    class JsonException extends JsonProcessingException
    {
        JsonException(String msg, Exception e)
        {
            super(msg, e);
        }
    }


    PluginEdfV2MessageQueueable(IEdfV2Message log, String tenantName, CombinedConfiguration config)
    {
        super(log, tenantName, config);
    }

    /**
     * This derived class is used for logs to format using the v2 format.
     */
    @Override
    public EdfMessageFormat getEdfMessageFormat()
    {
        return EdfMessageFormat.EDF_V2_FORMAT;
    }

    /**
     * Returns the tag used in the logs to mark the data that must be sent to EDF.
     * The default will be edfLog.v2.pluginLog.
     *
     * @return EDF tag
     */
    @Override
    public String getEdfLogTag()
    {
        return EdfMessageQueueableConstants.BASE_EDF_LOG_TAG +
               EdfMessageQueueableConstants.EDF_V2_LOG_TAG + getPluginLog().getPluginLogTag();
    }

    @Override
    public String toJson(boolean prettyPrint)
            throws JsonProcessingException
    {
        try
        {
            StringWriter stringWriter = new StringWriter();
            try (JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stringWriter))
            {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.writeValue(jsonGenerator, getPluginLog());
                stringWriter.flush();
            }

            return stringWriter.toString();
        }
        catch (IOException ioex)
        {
            throw new JsonException("EDF message json generation failed", ioex);
        }
    }

    @Override
    IEdfV2Message getPluginLog() {
        return (IEdfV2Message) super.getPluginLog();
    }
}

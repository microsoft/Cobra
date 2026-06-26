package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.audio.WavDumperNode;
import com.rits.cloning.Cloner;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioLog
        implements Serializable, Cloneable, EdfMessageQueueable
{
    private static final Logger logger = LogManager.getLogger(AudioLog.class);

    /*Json used for deserialization*/
    @JsonProperty("fileName")
    public String fileName;

    @JsonIgnore
    public String sessionId;

    @JsonIgnore
    public String audioBlobPath;

    @JsonIgnore
    public String audioLogKey;

    @JsonIgnore
    public int stepId;

    @JsonIgnore
    public static final JmsMessageType jmsType = JmsMessageType.AUDIO_LOG;

    @JsonProperty("audioLogEnabledPosting")
    public boolean enabledPosting;

    @JsonProperty("audioLogBasePath")
    public String basePath;

    @JsonProperty("tenantKey")
    public String tenantKey;

    @JsonProperty("encrypted")
    public boolean encrypted;

    @JsonIgnore
    public EdfMessageFormat edfMessageFormat;

    @JsonIgnore
    public EdfMessageFormat getEdfMessageFormat()
    {
        return edfMessageFormat;
    }

    public String getFileNameNoExtension()
    {
        return FilenameUtils.removeExtension(getFile().getName());
    }

    public File getFile()
    {
        return new File(fileName);
    }

    public static AudioLog fromJson(String json) throws IOException {
        AudioLog audioLog;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        audioLog = mapper.readValue(json, AudioLog.class);
        return audioLog;
    }

    @Override
    public String toJson(boolean prettyPrint)
            throws JsonProcessingException
    {
        if (prettyPrint)
        {
            return Command.getMapper()
                          .writerWithDefaultPrettyPrinter()
                          .writeValueAsString(this);
        }
        else
        {
            return Command.getMapper()
                          .writer()
                          .writeValueAsString(this);
        }
    }

    @JsonIgnore
    @Override
    public JmsMessageType getJmsType()
    {
        return jmsType;
    }

    @JsonIgnore
    @Override
    public String getTenantKey()
    {
        return tenantKey;
    }

    @JsonIgnore
    @Override
    public boolean postingEnabled()
    {
        return enabledPosting;
    }

    @JsonIgnore
    @Override
    public WebApiConfig.Property getEdfEndpointUrlProperty()
    {
        return jmsType.getUrlPathProperty();
    }

    @Override
    public AudioLog clone()
    {
        return new Cloner().deepClone(this);
    }

    public void delete()
    {
        String filepath = getFile().getAbsolutePath();
        logger.debug("DELETING AUDIO FILE : {}", filepath);
        try
        {
            Files.deleteIfExists(Paths.get(filepath));
        } catch (IOException e)
        {
            logger.warn("IOException while trying to delete file: {}. Exception msg: {}", filepath, e.getMessage());
        }

        if (WavDumperNode.getEncryptedFiles().containsKey(getFileNameNoExtension()))
        {
            logger.debug("Removing File Name From WavDumperNode map");
            WavDumperNode.getEncryptedFiles().remove(getFileNameNoExtension());
        }
    }

}

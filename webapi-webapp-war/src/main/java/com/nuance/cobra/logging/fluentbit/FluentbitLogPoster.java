package com.nuance.cobra.logging.fluentbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.response.InternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.response.EDF.AudioLog;
import com.nuance.cobra.utils.response.EDF.NeapLog;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * This class enables WebAPI integration with Fluentbit for EDF log forwarding/aggregation.
 */
public class FluentbitLogPoster
{
    private static final Logger logger = LogManager.getLogger(FluentbitLogPoster.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(FluentbitLogPoster.class);
    private static ResponseErrorHandler errorHandler = null;
    private static final Level customLogLevel = Level.forName(WebApiConfigManager.getInstance().getString(WebApiConfig.Property.FLUENT_LOGGER_LOG_LEVEL_NAME.key),
            WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.FLUENT_LOGGER_LOG_LEVEL.key));

    private FluentbitLogPoster() {}

    private static ResponseErrorHandler getErrorHandler()
    {
        if (errorHandler == null)
        {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new LoggerErrorListener(logger));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }

    public static void log(EdfMessageQueueable edfMessage, CombinedConfiguration cobraConfig)
    {
        try {
            if (edfMessage.getJmsType().equals(EdfMessageQueueable.JmsMessageType.EDF_LOG))
            {
                handleCommandLog(edfMessage, cobraConfig, logger);
            }
            else if (edfMessage instanceof AudioLog)
            {
                AudioLog audioLog = (AudioLog) edfMessage;
                File file = audioLog.getFile();
                try (FileInputStream in = new FileInputStream(file)) {
                    handleAudioLog(audioLog,cobraConfig, audioLog.getFileNameNoExtension(), (int)file.length(), in, logger);
                } finally
                {
                    audioLog.delete();
                }

            }
        }
        catch (Exception e)
        {
            logger.error("Failed to post logs to fluentBit.", e);
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.FLUENTBIT_LOG_POSTING_ERROR, e.getMessage()));
            getErrorHandler().handleError(responseResult);
        }
    }

    protected static void handleCommandLog(EdfMessageQueueable edfLog, CombinedConfiguration cobraConfig, Logger logger)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String edfLogString = edfLog.toJson(false);
            if(cobraConfig.getBoolean(WebApiConfig.Property.ENABLE_FLUENT_LOG_SYSOUT.key))
            {
                EsapiLogger.info(esapiLogger, "EDF Command log to be sent to fluentbit: %s", edfLogString);
            }
            JsonNode edfLogJsonNode = mapper.readTree(edfLogString);
            String escapedTenant = StringEscapeUtils.escapeJava(edfLog.getTenantKey());
            logger.log(customLogLevel, "{}{} {}", edfLog.getEdfLogTag(), escapedTenant, edfLogJsonNode);
        }
        catch (Exception e)
        {
            NeapLog neapLog = (NeapLog) edfLog;
            logger.error("Failed to post {} logs to fluentBit.", neapLog.commandName, e);
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.FLUENTBIT_JSON_ERROR, e.getMessage()));
            getErrorHandler().handleError(responseResult);
        }
    }

    protected static void handleAudioLog(AudioLog audioLog, CombinedConfiguration cobraConfig, String fileName, int dataLength, FileInputStream in, Logger logger)
            throws IOException
    {
        byte[] fileData = new byte[dataLength];

        if(in.read(fileData) <= 0)
        {
            throw new IOException("Content of file " + fileName + " is empty");
        }

        String tenant = cobraConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key);

        JSONObject data = new JSONObject();
        data.put("tenant", tenant);
        data.put("path", audioLog.audioBlobPath);
        if (audioLog.audioLogKey != null) {
            data.put("audioLogKey", audioLog.audioLogKey);
        }
        data.put("wavData", java.util.Base64.getEncoder().encodeToString(fileData));
        data.put("wavDataEncoding", "audio/wav;base64");
        data.put("isEncrypted", audioLog.encrypted);

        logger.log(customLogLevel, "{}{} {}", audioLog.getEdfLogTag(),audioLog.tenantKey, data);
    }
}



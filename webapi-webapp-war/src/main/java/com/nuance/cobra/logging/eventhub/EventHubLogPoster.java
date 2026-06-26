package com.nuance.cobra.logging.eventhub;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.messaging.eventhubs.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nuance.cobra.api.response.InternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.response.EDF.AudioLog;
import com.nuance.cobra.utils.response.EDF.EventHubLog;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHubLogPoster {

    protected static Logger logger = LogManager.getLogger(EventHubLogPoster.class);
    protected static ResponseErrorHandler errorHandler = null;
    protected static EventHubProducerAsyncClient producer = null;

    private static Logger getLogger()
    {
        if (logger == null)
        {
            logger = LogManager.getLogger(EventHubLogPoster.class);
        }
        return logger;
    }

    private static EventHubProducerAsyncClient getProducer(String clientId, String fullyQualifiedNamespace, String topic)
    {
        if (producer == null)
        {
            ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                    .clientId(clientId)
                    .build();

            producer = new EventHubClientBuilder()
                    .credential(fullyQualifiedNamespace, topic, managedIdentityCredential)
                    .buildAsyncProducerClient();
        }
        return producer;
    }

    private static ResponseErrorHandler getErrorHandler()
    {
        if (errorHandler == null)
        {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new LoggerErrorListener(getLogger()));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }

    private EventHubLogPoster() {}

    public static void log(EdfMessageQueueable edfMessage, CombinedConfiguration cobraConfig)
    {
        try {
            if (edfMessage.getJmsType().equals(EdfMessageQueueable.JmsMessageType.EDF_LOG))
            {
                handleCommandLog(edfMessage, cobraConfig, getLogger());
            }
            else if (edfMessage instanceof AudioLog)
            {
                getLogger().error("Sending audio log to EventHub is not supported.");
            }
        }
        catch (Exception e)
        {
            getLogger().error("Failed to post logs to EventHub.", e);
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.EVENTHUB_POST_ERROR, e.getMessage()));
            getErrorHandler().handleError(responseResult);
        }
    }

    protected static void handleCommandLog(EdfMessageQueueable edfLog, CombinedConfiguration cobraConfig, Logger logger)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String edfLogString = edfLog.toJson(false);
            JsonNode edfLogJsonNode = mapper.readTree(edfLogString);
            ((ObjectNode)edfLogJsonNode).put("webapiNeapLogVersion", "3.0.0");
            String topic = ((ObjectNode)edfLogJsonNode).remove("topic").asText();
            getLogger().trace("Sending command log to EventHub");

            String clientId = cobraConfig.getString(WebApiConfig.Property.EVENTHUB_CLIENT_ID.key);
            String namespace = cobraConfig.getString(WebApiConfig.Property.EVENTHUB_NAMESPACE.key);
            EventHubProducerAsyncClient producer = getProducer(clientId, namespace, topic);

            producer.createBatch().flatMap(batch -> {
                batch.tryAdd(new EventData(edfLogJsonNode.toString()));
                return producer.send(batch);
            }).subscribe(unused -> {
                    },
                    error -> {
                        EventHubLog eventHubLog = (EventHubLog) edfLog;
                        getLogger().error("Failed to post {} logs to EventHub.", eventHubLog.commandName, error);
                    },
                    () -> getLogger().trace("Send complete."));
        }
        catch (Exception e)
        {
            EventHubLog eventHubLog = (EventHubLog) edfLog;
            getLogger().error("Failed to post {} logs to EventHub.", eventHubLog.commandName, e);
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.EVENTHUB_POST_ERROR, e.getMessage()));
            getErrorHandler().handleError(responseResult);
        }
    }
}
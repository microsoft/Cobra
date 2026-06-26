package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventHubLog implements EdfMessageQueueable
{
    static final String REQUEST_HEADER = "requestHeader";
    static final String REQUEST = "request";

    @JsonProperty("data")
    public Map<String, Object> data;

    @JsonProperty("topic")
    public String loggingTopic;

    private static final Logger logger = LogManager.getLogger(EventHubLog.class);

    @JsonIgnore
    public static final JmsMessageType jmsType = JmsMessageType.EDF_LOG;

    @JsonIgnore
    private CommandLog commandLogRef;

    @JsonIgnore
    private CombinedConfiguration config = null;

    @JsonProperty
    public String audioBlobPath;

    @JsonProperty
    public String audioLogKey;

    @JsonIgnore
    public String commandName;

    static class ReportingHeader {
        @JsonProperty
        public Map<String, String> tenantData = null;
    }

    @JsonProperty
    public ReportingHeader reportingHeader = new ReportingHeader();


    public EventHubLog()
    {
    }

    public EventHubLog(CommandLog commandLog)
    {
        buildLog(commandLog, null);
    }

    public EventHubLog(EventHubLog log)
    {
        buildLog(log.commandLogRef, log.config);
    }

    public EventHubLog(String chainName, List<EdfMessageQueueable> logs, Map<String, Object> metadata, CombinedConfiguration configuration) throws JsonProcessingException
    {
        config = configuration;
        var commands = new JSONArray();

        for (EdfMessageQueueable log : logs) {
            if (!(log instanceof EventHubLog)) {
                throw new IllegalArgumentException("List of logs isn't of the correct type (NeapLog)");
            }
            var neapLog = (EventHubLog) log;
            var neapLogData = new JSONObject(neapLog.toJson(false));
            var neapLogDataString = neapLogData.getString("data");
            neapLogData.put("data", new JSONObject(neapLogDataString));
            commands.put(neapLogData);
            if(neapLog.audioBlobPath != null) {
                if (audioBlobPath == null) {
                    audioBlobPath = neapLog.audioBlobPath;
                } else {
                    logger.warn("More than one audio blob path found. Ignoring {}", neapLog.audioBlobPath);
                }
            }
        }

        var neapLog0 = (EventHubLog)logs.get(0);
        var neapLogN = (EventHubLog)logs.get(logs.size() - 1);

        commandLogRef = neapLog0.commandLogRef;

        var dataObject = new JSONObject();
        dataObject.put("commands", commands);
        var headers = new JSONObject(neapLog0.data).getJSONObject(REQUEST_HEADER);
        headers.put("metadata", metadata);
        dataObject.put(REQUEST_HEADER, headers);

        var chainRequestLog = new RequestLog();
        var requestLog0 = neapLog0.commandLogRef.request;
        var requestLogN = neapLogN.commandLogRef.request;

        chainRequestLog.httpEquivalentStatusCode = requestLogN.httpEquivalentStatusCode;
        chainRequestLog.responseCode = requestLogN.responseCode;
        chainRequestLog.outcome = requestLogN.outcome;
        chainRequestLog.userInfo = requestLog0.userInfo;
        chainRequestLog.stepID = requestLog0.stepID;
        chainRequestLog.sessionId = requestLog0.sessionId;
        chainRequestLog.command = chainName;

        dataObject.put(REQUEST, new JSONObject(chainRequestLog.toJson(false)));
        data = dataObject.toMap();

        reportingHeader = neapLog0.reportingHeader;

        fillLoggingTopic(dataObject);
    }

    public EventHubLog(String commandName, CommandLog commandLog, CombinedConfiguration config)
    {
        this.commandName = commandName;
        buildLog(commandLog, config);
    }

    private void buildLog(CommandLog commandLog, CombinedConfiguration config)
    {
        final var SPEECH_RECO_COMMAND = "speechRecognition";

        this.config = config;

        HierarchicalConfiguration<ImmutableNode> neapHeadersConfig = null;
        if (this.config != null) {
            try {
                neapHeadersConfig = config.configurationAt(WebApiConfig.Property.REPORTING_TENANT_DATA.getKey());

                // creates and adds the tenantData field in neapHeader.
                reportingHeader.tenantData = new HashMap<>();

                // Adds the neapHeaders found in tenant config to the tenantData field.
                for (Iterator<String> it = neapHeadersConfig.getKeys(); it.hasNext(); ) {
                    String key = it.next();
                    reportingHeader.tenantData.put(key, neapHeadersConfig.getString(key));
                }
            } catch (ConfigurationRuntimeException e) {
                // exception intentionally not handled, the NEAP_HEADERS element is optional.
            }
        }

        if (commandLog == null)
        {
            throw new IllegalArgumentException("NeapLog -- CommandLog for constructor must not be null");
        }

        if (null == commandLog.session)
        {
            logger.debug("Session was never started, commandLog is being ignored");
            return;
        }
        commandLogRef = commandLog;
        try {
            var commandLogJson = new JSONObject(commandLog.toJson(false));
            var dataObject = new JSONObject();
            dataObject.put(REQUEST, commandLogJson.getJSONObject(REQUEST));
            dataObject.put(REQUEST_HEADER, commandLogJson.getJSONObject(REQUEST_HEADER));
            data = dataObject.toMap();

            fillLoggingTopic(dataObject);

            if (commandLog.request.command.equals(SPEECH_RECO_COMMAND)) {
                audioBlobPath = commandLog.session.makeAudioBlobPath();
                audioLogKey = commandLog.session.getAudioLogKey();
            }
        }
        catch (Exception e)
        {
            logger.error("Couldn't convert CommandLog to json, commandLog is being ignored");
        }
    }

    private void fillLoggingTopic(JSONObject dataObject) {
        if(this.config != null &&
           this.config.getString(WebApiConfig.Property.FLUENT_LOG_TOPIC_NAME.key) != null)
        {
            loggingTopic = this.config.getString(WebApiConfig.Property.FLUENT_LOG_TOPIC_NAME.key);
        } else
        {
            loggingTopic = dataObject.getJSONObject(REQUEST_HEADER)
                                     .getString("Channel") + "-" + dataObject.getJSONObject(REQUEST_HEADER)
                                                                             .get("ClientID");
        }
    }

    public static EventHubLog fromJson(String json)
            throws JsonProcessingException
    {
        EventHubLog neapLog;
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        neapLog = mapper.readValue(json, EventHubLog.class);
        return neapLog;
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
    public EdfMessageFormat getEdfMessageFormat()
    {
        return EdfMessageFormat.NEAP_V2_FORMAT;
    }

    @JsonIgnore
    @Override
    public String getTenantKey()
    {
        return commandLogRef.requestHeader.tenant;
    }

    @JsonIgnore
    @Override
    public boolean postingEnabled()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public WebApiConfig.Property getEdfEndpointUrlProperty()
    {
        return jmsType.getUrlPathProperty();
    }

    @JsonIgnore
    @Override
    public JmsMessageType getJmsType()
    {
        return jmsType;
    }
}

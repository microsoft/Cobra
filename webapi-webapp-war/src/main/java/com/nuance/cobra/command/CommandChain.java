package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.nuance.cobra.api.endpoint.WebApiEndpoint;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestCommand;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.logging.EdfFluentbitListener;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.EdfLogListener;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.logging.eventhub.EventHubListener;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.EventHubLog;
import com.nuance.cobra.utils.response.EDF.NeapLog;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.json.*;
import javax.websocket.Session;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.*;

/**
 * CommandChain command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/command-chain.md
 * for documentation.
 */
public class CommandChain implements CommandChainer, EdfLogListener
{
    private static final Logger logger = LogManager.getLogger(CommandChain.class); // TODO add more logging

    public enum CommandChainLogType {
        PER_REQUEST("perRequest"),
        PER_REQUEST_AND_SUMMARY("perRequestAndSummary"),
        SUMMARY_ONLY("summaryOnly");

        private final String value;

        CommandChainLogType(String name)
        {
            value = name;
        }

        @Override
        public String toString()
        {
            return value;
        }

    }

    private WebApiEndpoint endpoint;
    private Session websocketSession;
    protected Request commandChainRequest;
    private IResponseSender responseSender;
    private CombinedConfiguration configForTenant;
    private boolean hasGeneratedReportingLog;

    private int commandCount;
    private int commandCounter;

    protected List<String> sentResponses = new ArrayList<>();

    protected List<EdfMessageQueueable> commandLogs = new ArrayList<>();

    public static void initalize()
    {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    protected CommandChain() {
        // only for testing purposes
    }

    public CommandChain(WebApiEndpoint endpoint,
                        Session websocketSession,
                        Request commandChainRequest,
                        IResponseSender responseSender,
            CombinedConfiguration configForTenant)
    {
        this.endpoint = endpoint;
        this.websocketSession = websocketSession;
        this.commandChainRequest = commandChainRequest;
        this.responseSender = responseSender;
        this.configForTenant = configForTenant;

        if(commandChainRequest.chainId == null) {
            commandChainRequest.chainId = UUID.randomUUID().toString();
        }

        commandCount = commandChainRequest.requestCommands.size();
        commandCounter = 0;
    }

    @Override
    public String getChainId() {
        return commandChainRequest.chainId;
    }

    @Override
    public Map<String, Object> getChainMetadata() {
        return commandChainRequest.metadata;
    }

    @Override
    public int getCurrentCommandIndex() {
        return commandCounter - 1; // Index is increment as execute is called
    }

    @Override
    public int getCommandCount() {
        return commandCount;
    }

    public Command execute() throws CobraException
    {
        // the first command has already been validated
        String singleCommandRequestJson = Request.toSingleCommandRequestJson(commandChainRequest, commandCounter++);
        var singleCommandRequest = Request.fromJson(singleCommandRequestJson);
        singleCommandRequest.tenantKey = commandChainRequest.tenantKey;

        return execute(singleCommandRequest);
    }

    private Command execute(Request singleCommandRequest) throws CobraException
    {
        var command = CommandFactory.createCommand(singleCommandRequest, responseSender, configForTenant, this);

        String commandName = singleCommandRequest.requestCommand.name;
        if(null != singleCommandRequest.tenantKey && null != commandName){
            WebApiEndpoint.commandsCounter.labels(singleCommandRequest.tenantKey, commandName).inc();
        }

        command.execute();

        return command;
    }

    @Override
    public void onResponseWillBeSent(ResponseResult response)
    {

    }

    @Override
    public void onResponseWasSent(ResponseResult response, String formattedResponse)
    {
        if (!response.isFinal())
        {
            return;
        }

        if (response.getStatus() != ResponseResult.ResponseStatus.SUCCESS)
        {
            var commandChainError = ResponseCode.createResponseError(ExternalResponseErrorCode.COMMAND_CHAIN_PREVIOUS_COMMAND_ERROR);
            sendCommandChainResponse(new ResponseResult(commandChainError));
            return;
        }

        if (commandCounter < commandCount)
        {
            sentResponses.add(formattedResponse);

            if (commandChainRequest.sessionId == null) // if the chain's first command was a start session command
            {
                commandChainRequest.sessionId = SessionManager.getInstance().getSession(websocketSession).getSessionId();
            }

            executeNextCommand();
        }
        else
        {
            sendCommandChainResponse(new ResponseResult());
        }
    }

    private void executeNextCommand()
    {
        Request nextSingleCommandRequest = null;

        try
        {
            String nextSingleCommandRequestJson = Request.toSingleCommandRequestJson(commandChainRequest, commandCounter++);
            nextSingleCommandRequest = Request.fromJson(nextSingleCommandRequestJson);
            nextSingleCommandRequest.tenantKey = commandChainRequest.tenantKey;

            String nextCommandName = nextSingleCommandRequest.requestCommand.name;
            sendCommandChainResponse(new ResponseResult(nextCommandName));
        }
        catch (CobraException e)
        {
            var nextCommandName = "error";
            sendCommandChainResponse(new ResponseResult(nextCommandName));

            endpoint.onInternalError(websocketSession, e.getResult(), nextSingleCommandRequest);

            var commandChainError = ResponseCode.createResponseError(ExternalResponseErrorCode.COMMAND_CHAIN_NEXT_COMMAND_ERROR, e.getMessage());
            sendCommandChainResponse(new ResponseResult(commandChainError));
            return;
        }

        try
        {
            nextSingleCommandRequest.requestCommand = resolveJsonPointers(nextSingleCommandRequest.requestCommand);
        }
        catch (Exception e)
        {
            var commandChainError = ResponseCode.createResponseError(ExternalResponseErrorCode.COMMAND_CHAIN_JSON_POINTER_ERROR, e.getMessage());
            sendCommandChainResponse(new ResponseResult(commandChainError));
        }

        try
        {
            execute(nextSingleCommandRequest);
        }
        catch (CobraException e)
        {
            endpoint.onInternalError(websocketSession, e.getResult(), nextSingleCommandRequest);

            var commandChainError = ResponseCode.createResponseError(ExternalResponseErrorCode.COMMAND_CHAIN_CURRENT_COMMAND_ERROR, e.getMessage());
            sendCommandChainResponse(new ResponseResult(commandChainError));
        }
    }

    private void sendCommandChainResponse(ResponseResult commandChainResult)
    {
        if(commandChainResult.getStatus() == ResponseResult.ResponseStatus.ERROR) {
            sendLogs();
        }
        sendCommandChainResponse(commandChainRequest, commandChainResult, responseSender);
    }

    public static void sendCommandChainResponse(Request commandChainRequest, ResponseResult commandChainResult, IResponseSender responseSender)
    {
        var commandChainResponse = Response.fromCommandChainRequest(commandChainRequest, true);
        commandChainResponse.setResult(commandChainResult);

        String response;

        try
        {
            response = commandChainResponse.toJson();
        }
        catch (CobraException exceptionFormattingResponse)
        {
            try
            {
                ResponseResult result = exceptionFormattingResponse.getResult();
                response = result.toJson();
            }
            catch (CobraException exceptionFormattingException)
            {
                logger.error("Cannot serialize result: {}", exceptionFormattingException.getMessage());
                response = ResponseCode.createResponseErrorString(ExternalResponseErrorCode.RESPONSE_SERIALIZE_ERROR);
            }
        }

        responseSender.sendResponse(response); // TODO reuse same sender? what about the closeSession boolean?
    }

    protected RequestCommand resolveJsonPointers(RequestCommand command)
            throws JsonProcessingException
    {
        var commandString = command.toJsonString();

        Matcher matcher = Pattern.compile("_\\((?<responseIndex>\\d+(?=:))?(?:(?<=\\d):)?(?<jsonPointer>\\S*?)\\)_")
                                 .matcher(commandString);

        while (matcher.find()) // for each json pointer
        {
            String jsonPointerRaw = matcher.group();
            String jsonPathPointer = matcher.group("jsonPointer");
            int responseIndex =
                    matcher.group("responseIndex") != null ? Integer.parseInt(matcher.group("responseIndex")) : 0;

            Object value = getJsonPointerValueJsonPath(responseIndex, jsonPathPointer);
            if (value instanceof String) {
                commandString = commandString.replace(jsonPointerRaw, value.toString());
            } else {
                commandString = commandString.replace("\"" + jsonPointerRaw + "\"", value.toString());
            }
        }
        return new ObjectMapper().readValue(commandString, RequestCommand.class);
    }

    protected Object getJsonPointerValueJsonPath(int responseIndex, String jsonPointer)
    {
        String sentResponse = sentResponses.get(responseIndex);
        Object jsonPointerValue = new JSONObject(sentResponse).query("/" + jsonPointer.replace(".", "/"));

        if (jsonPointerValue == null)
        {
            throw new JsonPathException("JSON pointer value is null");
        }

        return jsonPointerValue;
    }

    protected String getJsonPointerValueJavax(int responseIndex, String jsonPointerString)
    {
        String javaxJsonPointer = "/" + jsonPointerString.replace(".", "/");
        JsonString jsonPointerValue;

        String sentResponse = sentResponses.get(responseIndex);

        try (var sentResponseStringReader = new StringReader(sentResponse);
             JsonReader reader = Json.createReader(sentResponseStringReader))
        {
            var jsonStructure = reader.read();
            var jsonPointer = Json.createPointer(javaxJsonPointer);
            jsonPointerValue = (JsonString) jsonPointer.getValue(jsonStructure);
        }

        return jsonPointerValue.getString();
    }

    public void forwardLog(EdfMessageQueueable edfLog, CombinedConfiguration commandConfig) {
        commandLogs.add(edfLog);
        if(commandLogs.size() == commandCount) {
            sendLogs();
        }
    }

    public void destroy() {
        // Command chains are not static
    }

    void sendLogs() {
        // In case the last command fails, we don't want to report twice.
        if(!hasGeneratedReportingLog) {
            var commandChainLogType = configForTenant.getString(WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.key, (String) WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.defaultValue);
            if (!commandChainLogType.equals(CommandChain.CommandChainLogType.PER_REQUEST.toString())) {
                var edfLogForwarder = getEdfLogForwarder();

                if (configForTenant.getBoolean(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key, (boolean) WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.defaultValue)) {
                    edfLogForwarder.addListener(new EdfFluentbitListener());
                }
                if (configForTenant.getBoolean(WebApiConfig.Property.ENABLE_EVENTHUB_LOG_POSTER.key, (boolean) WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.defaultValue)) {
                    edfLogForwarder.addListener(new EventHubListener());
                }

                edfLogForwarder.forwardLog(getEdfMessageQueueable(commandLogs), configForTenant);
            }
            hasGeneratedReportingLog = true;
        }
    }

    protected EdfLogForwarder getEdfLogForwarder() {
        return new EdfLogForwarder();
    }
    protected EdfMessageQueueable getEdfMessageQueueable(List<EdfMessageQueueable> logs){
        try {
            List<String> commandNames = new ArrayList<>();
            for(Map<String,Object> command : commandChainRequest.requestCommands) {
                commandNames.add((String)command.get("name"));
            }
            String chainName = String.format("chain_%s", String.join("_", commandNames));

            if (configForTenant.getString(WebApiConfig.Property.FLUENT_LOG_FORMAT.key).equalsIgnoreCase(NEAP_V1_LOG_FORMAT_NAME)) {
                return new NeapLog(chainName, logs, getChainMetadata(), configForTenant);
            } else if (configForTenant.getString(WebApiConfig.Property.FLUENT_LOG_FORMAT.key).equalsIgnoreCase(NEAP_V2_LOG_FORMAT_NAME)) {
                return new EventHubLog(chainName, logs, getChainMetadata(), configForTenant);
            } else if (configForTenant.getString(WebApiConfig.Property.FLUENT_LOG_FORMAT.key).equalsIgnoreCase(EDF_V1_LOG_FORMAT_NAME)) {
                logger.warn("Edf isn't adapted to consume command chain logs, none will be produced.");
                return null;
            }
            logger.error("Fluent log format not defined properly. Refer to tenant config documentation for field fluentLogFormat");
        } catch (Exception e) {
            logger.error("Command chain couldn't create concatenated log: {}", e.getMessage());
        }
        return null;
    }
}

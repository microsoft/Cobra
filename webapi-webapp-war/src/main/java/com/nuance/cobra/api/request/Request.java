package com.nuance.cobra.api.request;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.command.CommandFactory;
import com.nuance.cobra.utils.CobraException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class implements a json request sent by the client application. A request can be a command
 * or a directive. It is populated by deserialization of the json sent into the object Request
 */
@JsonFilter("RequestFilter")
public class Request
{
    /**
     * This enum implements all request types sent by the client application
     */
    public enum RequestType
    {
        @JsonProperty("command")COMMAND,
        @JsonProperty("commandChain")COMMAND_CHAIN,
        @JsonProperty("directive")DIRECTIVE
    }

    private static final Logger logger = LogManager.getLogger(Request.class);

    @JsonProperty("type")
    public RequestType type;

    @JsonProperty("command")
    public RequestCommand requestCommand;

    @JsonProperty("commands")
    public List<Map<String, Object>> requestCommands;

    @JsonProperty("version")
    public String version;

    @JsonProperty("sessionId")
    public String sessionId;

    @JsonProperty("sessionParameters")
    public Map<String, Object> sessionParameters;

    @JsonProperty("directive")
    public RequestDirective requestDirective;

    @JsonProperty("metadata")
    public Map<String,Object> metadata;

    @JsonProperty("chainId")
    public String chainId;

    @JsonIgnore
    public String tenantKey;

    private Request()
    {
    }

    /**
     * Build json schema from resource fileName in the class
     *
     * @return Schema
     */
    protected static Schema buildJsonSchemaFromFileName(String fileName)
            throws Exception
    {
        // Open file as input stream
        JSONObject rawSchema;
        InputStream inputStream = null;
        Schema rc = null;
        try
        {
            inputStream = Request.class.getClassLoader()
                                       .getResourceAsStream(fileName);
            if (inputStream == null)
            {
                throw new Exception("Cannot get json schema: " + fileName + " with getResourceAsStream()");
            }

            // Build schema
            rawSchema = new JSONObject(new JSONTokener(inputStream));
            rc = SchemaLoader.load(rawSchema);
        }
        finally
        {
            if (inputStream != null)
            {
                inputStream.close();
            }
        }

        return rc;
    }

    /**
     * Build json schema to validate a specific command/directive
     *
     * @return Schema
     */
    protected static Schema buildJsonSchemaFromRequest(RequestBasic request)
            throws Exception
    {
        String fileName = null;

        // Find json schema name  based on request
        if (request.type == RequestType.DIRECTIVE)
        {
            fileName = "schema/RequestDirective.json";
        }
        else if (request.type == RequestType.COMMAND)
        {
            if (request.command.getName() != null) {
                String input = request.command.getName();
                fileName = String.format("schema/Request%s.json", CommandFactory.toCamelCase(input));
            }
        }

        // Check fileName
        if (fileName == null)
        {
            throw new Exception("Cannot get schema from command name: " + request.command.getName());
        }

        return buildJsonSchemaFromFileName(fileName);
    }

    protected static JsonSchema buildSchemaFromRequest(RequestBasic requestBasic)
            throws Exception
    {
        String fileName = null;

        // Find json schema name  based on request
        if (requestBasic.type == RequestType.DIRECTIVE)
        {
            fileName = "schema/RequestDirective.json";
        }
        else if (requestBasic.type == RequestType.COMMAND)
        {
            if (requestBasic.command.getName() != null) {
                String input = requestBasic.command.getName();
                fileName = String.format("schema/Request%s.json", CommandFactory.toCamelCase(input));
            }
        }

        // Check fileName
        if (fileName == null)
        {
            throw new Exception("Cannot get schema from command name: " + requestBasic.command.getName());
        }

        return buildJsonSchemaFromFileNameWithFactory(fileName);
    }

    private static RequestBasic validateRequestType(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Build object from json
        JsonNode jsonNode = mapper.readTree(json);

        // Get schema to validate type and command
        JsonSchema theSchema  = buildJsonSchemaFromFileNameWithFactory("schema/RequestType.json");

        // Validate json input against schema
        ProcessingReport report = theSchema.validate(jsonNode);
        if (!report.isSuccess())
        {
            Iterator<ProcessingMessage> i = report.iterator();
            while (i.hasNext())
            {
                ProcessingMessage processingMessage = i.next();

                if ( processingMessage.getLogLevel() == LogLevel.ERROR)
                {
                    JsonNode j = processingMessage.asJson();
                    JsonNode instance = j.findValue("instance");
                    JsonNode pointer = instance.findValue("pointer");
                    throw new ValidationException(pointer.toString() + " -- " + processingMessage.getMessage());
                }
            }
        }
        // Create Request basic object based on json just validated
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        RequestBasic requestBasic = mapper.readValue(json, RequestBasic.class);

        return requestBasic;
    }

    private static Request validateCommandOrDirective(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Build object from json
        JsonNode jsonNode = mapper.readTree(json);

        // Get schema to validate type and command
        JsonSchema theSchema  = buildJsonSchemaFromFileNameWithFactory("schema/RequestBasic.json");

        // Validate json input against schema
        ProcessingReport report = theSchema.validate(jsonNode);
        if (!report.isSuccess())
        {
            Iterator<ProcessingMessage> i = report.iterator();
            while (i.hasNext())
            {
                ProcessingMessage processingMessage = i.next();

                if ( processingMessage.getLogLevel() == LogLevel.ERROR)
                {
                    JsonNode j = processingMessage.asJson();
                    JsonNode instance = j.findValue("instance");
                    JsonNode pointer = instance.findValue("pointer");
                    throw new ValidationException(pointer.toString() + " -- " + processingMessage.getMessage());
                }
            }
        }
        // Create Request basic object based on json just validated
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        RequestBasic requestBasic = mapper.readValue(json, RequestBasic.class);

        // Get schema to validate the specific command/directive
//            schema = buildJsonSchemaFromRequest(requestBasic);

        theSchema = buildSchemaFromRequest(requestBasic);

        // Validate json input against specific schema
        report = theSchema.validate(jsonNode);
        if (!report.isSuccess())
        {
            Iterator<ProcessingMessage> i = report.iterator();
            while (i.hasNext())
            {
                ProcessingMessage processingMessage = i.next();
                if ( processingMessage.getLogLevel() == LogLevel.ERROR)
                {
                    JsonNode j = processingMessage.asJson();
                    JsonNode instance = j.findValue("instance");
                    JsonNode pointer = instance.findValue("pointer");
                    throw new ValidationException(pointer.toString() + " -- " + processingMessage.getMessage());
                }
            }
        }
        // Create Request object based on fully validated json
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        Request request = mapper.readValue(json, Request.class);

        return request;
    }

    private static Request validateCommandChain(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Build object from json
        JsonNode jsonNode = mapper.readTree(json);

        // Get schema to validate type and command
        JsonSchema theSchema  = buildJsonSchemaFromFileNameWithFactory("schema/CommandChain.json");

        // Validate json input against schema
        ProcessingReport report = theSchema.validate(jsonNode);
        if (!report.isSuccess())
        {
            Iterator<ProcessingMessage> i = report.iterator();
            while (i.hasNext())
            {
                ProcessingMessage processingMessage = i.next();

                if ( processingMessage.getLogLevel() == LogLevel.ERROR)
                {
                    JsonNode j = processingMessage.asJson();
                    JsonNode instance = j.findValue("instance");
                    JsonNode pointer = instance.findValue("pointer");
                    throw new ValidationException(pointer.toString() + " -- " + processingMessage.getMessage());
                }
            }
        }
        // Create Request object based on fully validated json
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        Request request = mapper.readValue(json, Request.class);

        return request;
    }

    public static boolean isCommandChainRequest(String json)
    {
        try
        {
            RequestBasic requestBasic = validateRequestType(json);
            return requestBasic.type == RequestType.COMMAND_CHAIN;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Creates Request object by serializing json input
     *
     * @param json
     *         json that is received from the client
     *
     * @return Request
     */
    public static Request fromJson(String json)
            throws CobraException
    {
        Request request;
        try
        {
            RequestBasic requestBasic = validateRequestType(json);

            if (requestBasic.type == RequestType.COMMAND ||
                requestBasic.type == RequestType.DIRECTIVE)
            {
                request = validateCommandOrDirective(json);
            }
            else
            {
                request = validateCommandChain(json);
            }
        }
        catch (ValidationException e)
        {
            logger.error(
                    "Cannot create request object from json. Json input cannot be validated against the json schema");
            logger.error("---error(s) from json schema validation: " + e.getMessage()
                                                                        , e);
            throw new CobraException(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_PARSE_ERROR,
                            e.getMessage()
                             ));
        }
        catch (Exception e)
        {
            logger.error("Cannot create request object from json.");
            logger.error("---error(s): " + e.getMessage(), e);
            throw new CobraException(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_PARSE_ERROR,
                            e.getMessage()));
        }

        return request;
    }

    protected static JsonSchema buildJsonSchemaFromFileNameWithFactory(String fileName)
            throws Exception
    {
        String theUrl = Request.class.getClassLoader().getResource(fileName).getPath();

        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.byDefault();
        final File file = new File(theUrl);
        final URI uri = file.toURI();
        JsonSchema theSchema  = jsonSchemaFactory.getJsonSchema(uri.toString());

        return theSchema;
    }

    /**
     * Creates a string by deserializing request object
     *
     * @param request
     *         Request object
     *
     * @return String
     */
    public static String toJson(Request request)
            throws CobraException
    {
        // Set mapper. Set parameter to suppress serializing properties with null values
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try
        {
            // Check request
            if(request == null)
            {
                throw new Exception("request can't be null");
            }

            // Create json
            SimpleFilterProvider filterProvider;

            if (request.type == RequestType.COMMAND_CHAIN) {
                filterProvider = new SimpleFilterProvider().addFilter("RequestFilter", SimpleBeanPropertyFilter.serializeAllExcept("command"));
            } else {
                filterProvider = new SimpleFilterProvider().addFilter("RequestFilter", SimpleBeanPropertyFilter.serializeAll());
            }

            return mapper.writer(filterProvider).writeValueAsString(request);
        }
        catch (Exception e)
        {
            logger.error("Cannot create json string from object");
            logger.error("--- error: " + e.getMessage(), e);
            throw new CobraException(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR, e.getMessage()));
        }
    }

    /**
     * Used to convert a command chain into a single command.
     */
    public static String toSingleCommandRequestJson(Request request)
            throws CobraException
    {
        return toSingleCommandRequestJson(request, 0);
    }
    
    public static String toSingleCommandRequestJson(Request request, int commandIndex)
            throws CobraException
    {
        // Set mapper. Set parameter to suppress serializing properties with null values
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try
        {
            // Check request
            if(request == null)
            {
                throw new Exception("request can't be null");
            }

            // Create json
            RequestType oldType = request.type;
            String chainId = request.chainId;

            request.type = RequestType.COMMAND;
            request.chainId = null;

            SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter("RequestFilter", SimpleBeanPropertyFilter.serializeAllExcept("commands"));
            String requestJson = mapper.writer(filterProvider).writeValueAsString(request);

            request.type = oldType;
            request.chainId = chainId;

            JsonNode singleCommandRequest = new ObjectMapper().readTree(requestJson);
            JsonNode firstCommand = mapper.valueToTree(request.requestCommands.get(commandIndex));

            ((ObjectNode) singleCommandRequest).set("command", firstCommand);

            return singleCommandRequest.toString();
        }
        catch (Exception e)
        {
            logger.error("Cannot create json string from object");
            logger.error("--- error: " + e.getMessage(), e);
            throw new CobraException(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR, e.getMessage()));
        }
    }

    /**
     * Creates a string to print out request object
     *
     * @return String
     */
    @Override
    public String toString()
    {
        if (type == RequestType.COMMAND_CHAIN) {

            String stringValue =      "Type: " + type + "\n" +
                                      "SessionId: " + sessionId;

            for (int i = 0; i < requestCommands.size(); i++) {
                int commandNumber = i + 1;
                stringValue += "\n" + "Command" + commandNumber + ": " + requestCommands.get(i);
            }            
            return stringValue;

        } else {
            return "Type: " + type + "\n" +
                   "Version: " + version + "\n" +
                   "SessionId: " + sessionId + "\n" +
                   "Command: " + requestCommand + "\n" +
                   "Directive: " + requestDirective;
        }
    }
}
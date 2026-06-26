package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.InterpretationClient;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.encryption.Encryptor;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Interpretation command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/interpretation-command.md
 * for documentation. *
 */

public class InterpretationCommand
        extends Command
        implements EDFResponse
{
    protected static final Logger logger = LogManager.getLogger();
    private ClientFactory clientFactory = new ClientFactory();
    private InterpretationClient interpretationClient = null;

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public InterpretationCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer)
    {
        super(request, commandConfigs, chainer);
    }

    /**
     * Used in testing to replace the client factory by a mock.
     */
    public void setClientFactory(ClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }

    /**
     * Log response to send
     */
    protected void logResponse(ResponseResult responseResult)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("send result: {}", responseResult.toJson());
            }
        }
        catch (Exception e)
        {
            logger.warn("can't log response to send since can't get json from response - error: {}", e.getMessage());
        }
    }

    /**
     * Starts the command
     */
    @Override
    public void executeImplementation()
    {
        // Log
        logRequest(request, logger);
        // Get current session
        var session = getActiveSession();
        if (session == null)
        {
            sendErrorResponse(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR);
            return;
        }

        // Get all parameters for the command
        CombinedConfiguration requestConfig;
        try
        {
            requestConfig = session.getConfigWithMerge(request.requestCommand.parameters);
        }
        catch (IllegalArgumentException e)
        {
            sendErrorResponse(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR, e.getMessage());
            return;
        }

        // Save current command in session
        session.setCommand(this);

        List<DynamicVocabulary> dynamicVocabularies = session.retrieveActiveDynamicVocabularies(requestConfig);

        statistics.dataLength = request.requestCommand.parameters.get(WebApiConfig.Property.COMMAND_DATA.key).toString().length();
        if(requestConfig.getProperty(WebApiConfig.Property.COMMAND_DATA.key) == null) {
            requestConfig.addProperty(WebApiConfig.Property.COMMAND_DATA.key, request.requestCommand.parameters.get(WebApiConfig.Property.COMMAND_DATA.key));
        }
        
        interpretationClient = clientFactory.createInterpretationClient(
                requestConfig,
                request.sessionId,
                dynamicVocabularies,
                getResponseHandler()
        );
        interpretationClient.startInterpretation();
    }

    /**
     * When the endpoint receives a directive from the client application, this method
     * will be called on the active command
     *
     * @param requestDirective
     *         The directive received from the client application
     */
    @Override
    public void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal)
    {
        if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.STOP)) {
            try
            {
                interpretationClient.stopInterpretation();
            }
            catch (Exception e)
            {
                sendErrorResponse(ExternalResponseErrorCode.CANNOT_EXECUTE_DIRECTIVE_ERROR);
            }
        }
        else if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.CANCEL)) {
            try
            {
                interpretationClient.cancelInterpretation(isFinal);
                terminateCommand();
            }
            catch (Exception e)
            {
                sendErrorResponse(ExternalResponseErrorCode.CANNOT_EXECUTE_DIRECTIVE_ERROR);
            }
        }
    }

    /**
     * Check if the command is done then reset active command
     *
     * @param response
     *          The formatted response received from the NIM client
     */
    @Override
    public boolean onResponseImplementation(ResponseResult response)
    {
        // Log
        logResponse(response);
        // Reset active command if the command is done, make sure first that interpretationClient exists, won't be
        // the case if this is an error parsing the request.
        if (interpretationClient != null)
        {
            resetSessionActiveCommand();
        }
        return true;
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        CommandLog baseResponse = super.formatLog(response);
        Response responseClone = null;
        InterpretationResponse interpretationResponse = null;

        switch (CommandSensitivityType.valueOf(baseResponse.request.sensitivity.toUpperCase()))
        {
            case OPEN:
                break;
            case MASKED:
            case ENCRYPTED:
                try
                {
                    logger.info("sensitivity is set to sensitivity, encrypting!");
                    var dataString = baseResponse.request.params.get(WebApiConfig.Property.COMMAND_DATA.key).toString();
                    dataString = getEncryptor().processString(dataString);
                    logger.info("Encrypted string is {}", dataString);
                    baseResponse.request.params.put(WebApiConfig.Property.COMMAND_DATA.key, dataString);
                    responseClone = ((Response)response).clone();
                    ResponseResult theResult = responseClone.getResult();
                    if (theResult != null
                            && theResult.getStatus() == ResponseResult.ResponseStatus.SUCCESS)
                    {
                        interpretationResponse = encryptResponse((InterpretationResponse) theResult);
                    }

                    responseClone.setResult(interpretationResponse);

                } catch (Exception e)
                {
                    logger.error("EXCEPTION {}", e.getMessage(), e);
                }
                break;
            default:
                break;
        }

        baseResponse.request.statistics = getStatistics();

        /*
        We need to do this to pretty-print the EDF response without duplicate parameters
         */
        if (responseClone != null)
        {
            baseResponse.request.response = prettyFormatResponse(responseClone);
        }
        else
        {
            baseResponse.request.response = prettyFormatResponse((Response)response);
        }

        baseResponse.request.sessionId = getSessionId();

        try {
            if (logger.isInfoEnabled())
            {
                logger.debug("{}--EDF Response : {} ", getCommandName(), baseResponse.toJson(false));
            }
        } catch (JsonProcessingException e) {
            logger.trace("Failed to log", e);
        }

        return baseResponse;
    }

    private InterpretationResponse encryptResponse(InterpretationResponse theResult) {
        InterpretationResponse interpretationResponse;
        interpretationResponse = theResult;
        InterpretationResponse.Interpretation[] interpretations = interpretationResponse.getInterpretations();
        if (interpretations != null)
        {
            for (var i = 0 ; i < interpretations.length; i++)
            {
                var interpretation = interpretations[i];
                String literal = interpretation.getLiteral();
                String intent = interpretation.getIntent();
                String origin = interpretation.getOrigin();

                var encLiteral = getEncryptor().processString(literal);
                var encIntent = getEncryptor().processString(intent);
                var encOrigin = getEncryptor().processString(origin);

                interpretation.setIntent(encIntent);
                interpretation.setLiteral(encLiteral);
                interpretation.setOrigin(encOrigin);

                InterpretationResponse.Concept[] concepts = interpretation.getConcepts();
                if (concepts != null)
                {
                    for (var y = 0; y < concepts.length; y++)
                    {
                        var concept = concepts[y];
                        String cLiteral = concept.getLiteral();
                        Object value = concept.getValue();
                        var conceptString = concept.getConceptName();

                        var encCLiteral = getEncryptor().processString(cLiteral);
                        var encValue = getEncryptor().processString(value.toString());
                        var encConceptString = getEncryptor().processString(conceptString);

                        concept.setLiteral(encCLiteral);
                        concept.setValue(encValue);
                        concept.setConceptName(encConceptString);
                        concepts[y] = concept;
                    }
                    interpretation.setConcepts(concepts);

                }

                interpretations[i] = interpretation;
            }
        }
        Map<String, Object> processedVerboseResults = new HashMap<>();
        Map<String, Object> verboseResults = interpretationResponse.getVerboseResult();

        for (Object key : verboseResults.keySet()) {
            String encryptedKey = getEncryptor().processString((String)key);
            Object encryptedValue = encryptJSONObject(getEncryptor(), verboseResults.get(key));
            processedVerboseResults.put(encryptedKey, encryptedValue);
        }

        interpretationResponse.setVerboseResult(processedVerboseResults);
        return interpretationResponse;
    }

    /**
     * Send error response
     *
     * @param errorCode
     *         The error code to build the response
     * @param args
     *         A list of strings (might be none) to build the response
     */
    protected void sendErrorResponse(ExternalResponseErrorCode errorCode, String... args)
    {
        var error = ResponseCode.createResponseError(errorCode, args);
        getErrorHandler(logger).handleError(new InterpretationResponse(error));
    }
}

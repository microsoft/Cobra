package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.response.DynamicVocabularyResponse;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * UploadDynamicVocabularyCommand command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/upload-dynamic-vocabulary-command.md
 * for documentation.
 **/

@SuppressWarnings("unchecked")
public class UploadDynamicVocabularyCommand
        extends Command
        implements EDFResponse
{
    private static final Logger logger = LogManager.getLogger(UploadDynamicVocabularyCommand.class);

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public UploadDynamicVocabularyCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer) {
        super(request, commandConfigs, chainer);
    }

    @Override
    public void executeImplementation()
    {
        // Get current session
        var session = getActiveSession();
        if (session == null)
        {
            sendErrorResponse(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR));
            return;
        }


        // Get all parameters for the command
        CombinedConfiguration requestConfig;
        requestConfig = session.getConfigWithMerge(request.requestCommand.parameters);

        Object dynamicVocabProperty = requestConfig.getProperty(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key);
        ArrayList<HashMap<String, Object>> requestDynamicVocabularies;

        if(dynamicVocabProperty instanceof HashMap)
        {
            requestDynamicVocabularies = new ArrayList<>();
            requestDynamicVocabularies.add((HashMap<String, Object>)dynamicVocabProperty);
        }
        else
        {
            requestDynamicVocabularies = (ArrayList<HashMap<String, Object>>) dynamicVocabProperty;
        }

        Map<String,DynamicVocabulary> vocabularies = new LinkedHashMap<>();

        for (HashMap<String, Object> requestDynamicVocabulary: requestDynamicVocabularies) {
            try
            {
                var dynamicVocabulary = DynamicVocabulary.fromJsonMap(requestDynamicVocabulary,false, requestConfig);
                vocabularies.put(dynamicVocabulary.getId(),dynamicVocabulary);
                statistics.wordCount += dynamicVocabulary.getSpokenListSize();
            }
            catch (IllegalArgumentException e)
            {
                logger.error("Caught an exception while parsing the params of the request : {}", e.getMessage());
                sendErrorResponse(ResponseCode.createResponseError(
                        ExternalResponseErrorCode.UPLOAD_DYNAMIC_VOCABULARY_GENERIC_ERROR),
                        ResponseCode.createResponseError(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR,
                                e.getMessage()));
                return;
            }
        }

        requestConfig.setProperty(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key, vocabularies);

        // Creation of the Dynamic Vocabulary Client
        var clientFactory = getClientFactory();
        var dynamicVocabularyClient = clientFactory.createDynamicVocabularyClient(
                requestConfig,
                request.sessionId,
                getResponseHandler()
        );

        // Save current command in session
        session.setCommand(this);

        // Upload Dynamic Vocabulary
        dynamicVocabularyClient.uploadDynamicVocabulary();
    }

    @Override
    public boolean onResponseImplementation(ResponseResult response)
    {
        logger.trace("UploadDynamicVocabularyCommand onResponse - sessionId: {}", request.sessionId);
        if (response instanceof DynamicVocabularyResponse && response.isFinal())
        {
            //This is a final result, need to remove the command from the session.
            try
            {
                resetSessionActiveCommand();
            }
            catch (Exception e)
            {
                logger.error("caught an exception while trying to remove the command from the session : {}", e.getMessage());
            }
        }
        return true;

    }

    /**
     * Send error response
     */
    private void sendErrorResponse(ResponseCode... error)
    {
        getErrorHandler(logger).handleError(new ResponseResult(error));
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        CommandLog baseResponse = super.formatLog(response);
        logger.info("Formatting DynamicVocabulary EDF Response");
        try {
            var baseResponseString = getMapper().writer().writeValueAsString(baseResponse);
            logger.info("DYNAMIC VOCABULARY -- BASE RESPONSE {} ", baseResponseString);
        } catch (JsonProcessingException e) {
            logger.info("Failed to log baseResponse.", e);
        }

        String dynamicVocabularyString;
        switch (CommandSensitivityType.valueOf(baseResponse.request.sensitivity.toUpperCase()))
        {
            case OPEN:
                break;
            case MASKED:
            case ENCRYPTED:
                Optional<Object> dynamicVocabularies = Optional.ofNullable(
                        baseResponse.request.params.get(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key));
                if (dynamicVocabularies.isPresent())
                {
                    dynamicVocabularyString = dynamicVocabularies.get()
                                                                 .toString();
                    dynamicVocabularyString = getEncryptor().processString(dynamicVocabularyString);
                    baseResponse.request.params.put(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key,
                            dynamicVocabularyString);
                }
                else
                {
                    logger.info("Could not get 'dynamicVocabularies' parameter, not encrypting");
                }
                break;
            default:
                break;
        }

        /*
        We need to do this to pretty-print the EDF response without duplicate parameters
         */
        baseResponse.request.response = prettyFormatResponse((Response)response);

        try {
            var baseResponseString = baseResponse.toJson(false);
            logger.debug("{}--EDF Response : {} ", getCommandName(), baseResponseString);
        } catch (JsonProcessingException e) {
            logger.info("Failed to log baseResponse.", e);
        }

        return baseResponse;
    }

}

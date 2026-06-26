package com.nuance.cobra.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.response.DynamicVocabularyResponse;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The client used for uploading dynamic vocabularies with the Mrec engine
 */
public class DynamicVocabularyMrecClient
        extends DynamicVocabularyClient
{
    private static final Logger logger = LogManager.getLogger(DynamicVocabularyMrecClient.class);

    //The config associated with the active command
    private final CombinedConfiguration requestParams;

    //The responseHandler object used to send the response back
    private final ResponseHandler responseHandler;

    /**
     * The client constructor
     * @param sessionId
     *  The sessionId of the active session
     * @param params
     *  The params associated with the active command
     * @param responseHandler
     *  The response handler used by the active command to send the responses back to the client application
     */
    public DynamicVocabularyMrecClient(String sessionId, CombinedConfiguration params, ResponseHandler responseHandler)
    {
        super(sessionId);
        this.requestParams = params;
        this.responseHandler = responseHandler;
    }

    /**
     * Method that allows to store the dynamic vocabulary into the active session
     */
    @SuppressWarnings("unchecked")
    @Override
    public void uploadDynamicVocabulary()
    {
        logger.trace("DynamicVocabularyMrecClient uploadDynamicVocabulary - sessionId: {}", sessionId);

        HierarchicalConfiguration<ImmutableNode> vocabulariesHierConfig = requestParams.configurationAt(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key);
        String vocabulariesString = CobraConfigurationUtils.hierarchicalConfigurationToJson(vocabulariesHierConfig);

        Map<String, Object> vocabularies = null;
        try
        {
            vocabularies = new ObjectMapper().readValue(vocabulariesString, Map.class);
        }
        catch (JsonProcessingException e)
        {
            return;
        }

        Map<String, DynamicVocabulary> realDynamicVocabularies = new HashMap<>();
        for(Map.Entry<String, Object> entry : vocabularies.entrySet())
        {
            Map<String, Object> dynVoc = (Map<String, Object>) entry.getValue();
            DynamicVocabulary dynVocab = DynamicVocabulary.fromJsonMap(dynVoc, false, requestParams);
            realDynamicVocabularies.put(dynVocab.getId(), dynVocab);
        }

        Session session = SessionManager.getInstance()
                                        .getSession(sessionId, false);

        Map<String, DynamicVocabulary> actualDynamicVocabularies = (HashMap)session.getProperty(Session.SessionPropertyKeys.DYNAMICVOCABULARIES);
        if(actualDynamicVocabularies == null)
        {
            actualDynamicVocabularies = new HashMap<>();
        }
        actualDynamicVocabularies.putAll(realDynamicVocabularies);

        //Storing the dynamic vocabularies within the session. They will be available for the whole session when activated during speechRecognitions commands
        session.setProperty(Session.SessionPropertyKeys.DYNAMICVOCABULARIES, actualDynamicVocabularies);

        //Once stored, sending back a Success response.
        responseHandler.onResponse(new DynamicVocabularyResponse(ResponseResult.ResponseStatus.SUCCESS, true));
    }

}

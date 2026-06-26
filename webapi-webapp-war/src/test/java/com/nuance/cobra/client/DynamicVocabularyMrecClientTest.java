package com.nuance.cobra.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.JavaCastUtils;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class DynamicVocabularyMrecClientTest
        extends CobraTest
{
    private ResponseHandler mockResponseHandler;
    private CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private WebApiJSONConfiguration mockConfig = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(mockConfigs);

    private Map<String, DynamicVocabulary> realDynamicVocabularies;

    private String sessionId = "233005b4-ee80-40e1-a2c9-9b31bfaf82ff";

    private Session mockSession;


    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        mockSession = new Session(sessionId, mockConfigs);
        mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(any(), anyBoolean())).thenReturn(mockSession);

        Request request;
        String requestString = "{ \"type\": \"command\", \"sessionId\": \"233005b4-ee80-40e1-a2c9-9b31bfaf82ff\", \"command\": { \"name\": \"uploadDynamicVocabulary\", \"id\": \"1\", \"parameters\": { \"dynamicVocabularies\": [{ \"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset\", \"body\": { \"place-name\": [{ \"literal\": \"Los Angeles\", \"spoken\": [\"l a\", \"los angeles\"] }, { \"literal\": \"San Francisco\" }], \"hockey-team\": [{ \"literal\": \"Montreal Canadiens\", \"spoken\": [\"montreal canadiens\", \"habs\"] }, { \"literal\": \"Boston Bruins\" }] } }] } } }";
        request = Request.fromJson(requestString);

        List<LinkedHashMap> dynamicVocabularies = JavaCastUtils.castList(LinkedHashMap.class, ArrayList.class.cast(request.requestCommand.parameters.get(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key)));

        //The Map to be returned
        realDynamicVocabularies = new HashMap<>();

        for(LinkedHashMap<String, Object> dynVoc : dynamicVocabularies)
        {
            //Returns a validated dynamic vocabulary object, will throw an illegalArgumentException if fails to validate
            DynamicVocabulary dynVocab = DynamicVocabulary.fromJsonMap(dynVoc, false, mockConfigs);

            //Adding to the map to be returned
            realDynamicVocabularies.put(dynVocab.getId(), dynVocab);
        }

    }

    @After
    public void cleanUp() throws Exception
    {
        super.tearDown();

    }

    @Test
    public void testUploadDynamicVocabularyHappy()
            throws JsonProcessingException, ConfigurationException
    {
        ClientFactory clientFactory = new ClientFactory();

        DynamicVocabularyClient dynamicVocabularyClient = clientFactory.createDynamicVocabularyClient(mockConfigs, sessionId, mockResponseHandler);

        Map<String, Object> dynamicVocabularies = new HashMap<>();
        dynamicVocabularies.put(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key, realDynamicVocabularies);
        String dynamicVocabulariesJson = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(dynamicVocabularies);

        mockConfig.read(new StringReader(dynamicVocabulariesJson));

        dynamicVocabularyClient.uploadDynamicVocabulary();

        Map<String, DynamicVocabulary> uploadedVocabs =
                (Map<String, DynamicVocabulary>) mockSession.getProperty(Session.SessionPropertyKeys.DYNAMICVOCABULARIES);
        assertNotNull(uploadedVocabs.get("some_id1"));

        verify(mockResponseHandler).onResponse(any());
    }

}

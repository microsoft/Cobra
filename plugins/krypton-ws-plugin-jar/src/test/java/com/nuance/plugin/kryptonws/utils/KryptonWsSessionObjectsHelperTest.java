package com.nuance.plugin.kryptonws.utils;

import com.nuance.plugin.kryptonws.KryptonWsConfig;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.utils.exceptions.DuplicateObjectException;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsSessionObjectsHelperTest
{
    KryptonWsSession kryptonWsSession;
    KryptonWsSessionObjectsHelper kryptonWsSessionObjectsHelper;
    IWebApiEndpoint mockWebApiEndpoint = mock(IWebApiEndpoint.class);
    IWebApiFeatures mockWebApiFeatures = mock(IWebApiFeatures.class);
    CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
    ISession mockSession = mock(ISession.class);
    CombinedConfiguration configs = KryptonWsConfig.getKryptonWsDefaultCombinedConfig();

    @Before
    public void prepare()
            throws IOException
    {
        kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        kryptonWsSession.setWebapiFeatures(mockWebApiFeatures);
        when(mockWebApiFeatures.getConfigs()).thenReturn(configs);
        kryptonWsSessionObjectsHelper = new KryptonWsSessionObjectsHelper(kryptonWsSession);

        kryptonWsSession.setWebApiEndpoint(mockWebApiEndpoint);
        kryptonWsSession.setWebapiSessionId("test");
        when(mockWebApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getString(KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN.getKey())).thenReturn((String)KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN.getDefaultValue());
    }

    @Test
    public void testMergeSessionObjects() throws Exception
    {
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\": \"some_id1\",\"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"urn://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",\"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\",\"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]");
        JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
        objectsToLoad.remove(2);
        assertEquals(objectsToLoad.toString().replace("urn://", "urn:"), uploadDynamicVocabs.toString());
    }

    @Test
    public void testMergeSessionObjectsNoWordSet() throws Exception
    {
        String requestId = "myRequest";
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id3\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/domainlm\"}]");
        JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
        assertEquals(0, uploadDynamicVocabs.length());
    }

    @Test
    public void testMergeDuplicateId()
    {
        String requestId = "myRequest";
        kryptonWsSession.setSessionObjects(Map.of("some_id1", new JSONObject("{\"whatever\":5}")));
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id1\",\"type\":\"application/x-nuance-wordset-pkg\",\"url\":\"https://host/path/wordset1\"},{\"id\":\"some_id1\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/domainlm\"}]");

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
        }catch (DuplicateObjectException e){
            assertEquals("Duplicate object id.", e.getMessage());
            assertEquals("some_id1", e.getExtra());
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMergeDuplicateUrl()
    {
        String requestId = "myRequest";

        Map<String, JSONObject> sessionObjectsMap = new HashMap<>();

        kryptonWsSession.setSessionObjects(sessionObjectsMap);
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id1\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordset\"},{\"id\":\"some_id2\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordset\"}]");

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
        }
        catch (DuplicateObjectException e) {
            assertEquals("Duplicate object url.", e.getMessage());
            assertEquals("https://host/path/some_wordset", e.getExtra());
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }
    @Test
    public void invalidIdLoad()
    {
        kryptonWsSession.setSessionObjects(Map.of("some_1id1", new JSONObject("{\"whatever\":5}")));
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\":\"\",\"type\":\"application/x-nuance-wordset-pkg\",\"url\":\"https://host/path/some_wordset\"}]");

        try {

            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());

        }
        catch (Exception e) {
            assertEquals("ID should not be shorter than 1 character.", e.getMessage());
        }
    }

    @Test
    public void testMergeMalformedWordset()
    {
        String requestId = "myRequest";
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id1\",\"type\":\"application/x-nuance-wordset-pkg\",\"url\":\"https://host/path/wordset1\"},{\"id\":\"some_id2\",\"type\":\"application/x-nuance-wordset\"},{\"id\":\"some_id3\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/domainlm\"}]");

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
        }
        catch (Exception e) {
            assertEquals("Missing body", e.getMessage());
        }
    }

    @Test
    public void testMergeMissingUrl()
    {
        String requestId = "myRequest";
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id1\",\"type\":\"application/x-nuance-wordset-pkg\"},{\"id\":\"some_id2\",\"type\":\"application/x-nuance-wordset\",\"body\":{\"<category-name_1>\":[{\"literal\":\"<writtenform>\",\"spoken\":[\"<spokenform1>\",\"<spokenformn>\"]}],\"<category-name_M>\":[{\"literal\":\"<writtenform>\",\"spoken\":[\"<spokenform1>\",\"<spokenformn>\"]}]}},{\"id\":\"some_id3\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/domainlm\"}]");

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
        }
        catch (Exception e) {
            assertEquals("Missing url", e.getMessage());
        }
    }

   @Test
   public void testDuplicateCheck(){
       String requestId = "myRequest";

       Map<String, JSONObject> sessionObjectsMap = new HashMap<>();
       JSONObject sessionObj = new JSONObject("{\"id\":\"some_id2\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordset\"}");
       sessionObjectsMap.put("some_id2", sessionObj);

       kryptonWsSession.setSessionObjects(sessionObjectsMap);
       JSONArray loadedObjects = new JSONArray();
       JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id1\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordse1t\"},{\"id\":\"some_id2\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordset\"}]");

       try {
           JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
       }
       catch (DuplicateObjectException e) {
           assertEquals("Duplicate object url.", e.getMessage());
           assertEquals("https://host/path/some_wordset", e.getExtra());
       }catch (Exception e){
           e.printStackTrace();
           fail();
       }
   }

   @Test
   public void testCreateSessionDuplicateCheck(){
       String requestId = "myRequest";

        JSONArray createSessionObjectArray = new JSONArray();
       JSONObject sessionObj = new JSONObject("{\"id\":\"some_id2\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordset\"}");
       createSessionObjectArray.put(sessionObj);

       JSONArray loadedObjects = new JSONArray();
       JSONArray objectsToLoad = new JSONArray("[{\"id\":\"some_id2\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"https://host/path/some_wordset\"}]");

       try {
           JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects,createSessionObjectArray);
       } catch (Exception e) {
           fail();
       }
   }

    @Test
    public void testSetSessionObjectsForSpeechRecognition()
    {
        HierarchicalConfiguration<ImmutableNode>  kryptonDlmsPerLanguage = mock(HierarchicalConfiguration.class);
        when(kryptonDlmsPerLanguage.getList(any(), anyString())).thenReturn(new ArrayList<>(List.of(Map.of("url", "someUrl", "weight", "medium"))));
        when(mockConfig.configurationAt("kryptonDomainLmsPerLanguage", true)).thenReturn(kryptonDlmsPerLanguage);
        kryptonWsSession.setSessionObjects(Map.of("someId2", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\"}")));
        kryptonWsSession.setCreateSessionLanguagePack(new JSONObject("{\"language\":\"eng-USA\"}"));

        kryptonWsSessionObjectsHelper.setSessionObjectsForSpeechRecognition(Map.of("someId2", "heavy"));
        verify(mockConfig).removeConfiguration(eq("kryptonWsPluginDlmConfig"));
        verify(mockConfig).addConfiguration(any(), eq("kryptonWsPluginDlmConfig"));
    }

    @Test
    public void testSetSessionObjectsForSpeechRecognitionNoWeightSet()
    {
        HierarchicalConfiguration<ImmutableNode>  kryptonDlmsPerLanguage = mock(HierarchicalConfiguration.class);
        when(kryptonDlmsPerLanguage.getList(any(), anyString())).thenReturn(new ArrayList<>(List.of(Map.of("url", "someUrl", "weight", "medium"))));
        when(mockConfig.configurationAt("kryptonDomainLmsPerLanguage", true)).thenReturn(kryptonDlmsPerLanguage);
        kryptonWsSession.setSessionObjects(Map.of("someId2", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\"}")));
        kryptonWsSession.setCreateSessionLanguagePack(new JSONObject("{\"language\":\"eng-USA\"}"));

        kryptonWsSessionObjectsHelper.setSessionObjectsForSpeechRecognition(Map.of());
        verify(mockConfig).removeConfiguration(eq("kryptonWsPluginDlmConfig"));
        verify(mockConfig).addConfiguration(any(), eq("kryptonWsPluginDlmConfig"));
    }

    @Test
    public void testSetSessionObjectsForSpeechRecognition_requestTimeout()
    {
        HierarchicalConfiguration<ImmutableNode> kryptonDlmsPerLanguage = mock(HierarchicalConfiguration.class);
        when(kryptonDlmsPerLanguage.getList(any(), anyString())).thenReturn(new ArrayList<>(List.of(Map.of("url", "someUrl", "weight", "medium"))));
        when(mockConfig.configurationAt("kryptonDomainLmsPerLanguage", true)).thenReturn(kryptonDlmsPerLanguage);
        kryptonWsSession.setSessionObjects(Map.of("someId2", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"requestTimeout\":2500}")));
        kryptonWsSession.setCreateSessionLanguagePack(new JSONObject("{\"language\":\"eng-USA\"}"));

        ArgumentCaptor<BaseHierarchicalConfiguration> argumentCaptor = ArgumentCaptor.forClass(BaseHierarchicalConfiguration.class);

        kryptonWsSessionObjectsHelper.setSessionObjectsForSpeechRecognition(Map.of("someId2", "heavy"));
        verify(mockConfig).removeConfiguration(eq("kryptonWsPluginDlmConfig"));
        verify(mockConfig).addConfiguration(argumentCaptor.capture(), eq("kryptonWsPluginDlmConfig"));

        BaseHierarchicalConfiguration addedConfiguration = argumentCaptor.getAllValues().get(0);

        HierarchicalConfiguration<ImmutableNode> domainLmsPerLang = addedConfiguration.configurationAt("kryptonDomainLmsPerLanguage");
        List<Map> domainLms = domainLmsPerLang.getList(Map.class, "eng-USA");

        assertEquals(2500, domainLms.get(0).get("requestTimeout"));
    }

    @Test
    public void testUrn() {
        String id = "some_id";
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray(String.format("[{\"id\":\"%s\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"urn:nuance-mix:tag:model/COMICS/mix.asr?=nlptype=krypton;language=eng-USA;dlm_weight=0.1\"}]", id));

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
            assertEquals(1, loadedObjects.length());
            assertEquals(0, uploadDynamicVocabs.length());
            assertEquals(1, kryptonWsSession.getSessionObjects().size());
            assertTrue(kryptonWsSession.getSessionObjects().containsKey(id));
            assertEquals("some_id", kryptonWsSession.getSessionObjects().get(id).getString("id"));
            assertEquals("application/x-nuance-domainlm", kryptonWsSession.getSessionObjects().get(id).getString("type"));
            assertEquals("0.1", kryptonWsSession.getSessionObjects().get(id).getString("weight"));
            assertEquals("urn:nuance-mix:tag:model/COMICS/mix.asr?=language=eng-USA", kryptonWsSession.getSessionObjects().get(id).getString("url"));
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUrnSemanticModel() {
        String id = "some_id";
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray(String.format("[{\"id\":\"%s\",\"type\":\"application/x-nuance-semantic-model\",\"url\":\"urn:nuance-mix:tag:model/COMICS/mix.asr?=nlptype=nle;language=eng-USA\"}]", id));

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
            assertEquals(1, loadedObjects.length());
            assertEquals(0, uploadDynamicVocabs.length());
            assertEquals(0, kryptonWsSession.getSessionObjects().size());
            assertNotNull(kryptonWsSession.getSemanticModel());
            assertEquals("urn:nuance-mix:tag:model/COMICS/mix.asr?=language=eng-USA", kryptonWsSession.getSemanticModel().getString("url"));
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUrnNoWeight() {
        String id = "some_id";
        JSONArray loadedObjects = new JSONArray();
        JSONArray objectsToLoad = new JSONArray(String.format("[{\"id\":\"%s\",\"type\":\"application/x-nuance-domainlm\",\"url\":\"urn:nuance-mix:tag:model/COMICS/mix.asr?=nlptype=krypton;language=eng-USA\"}]", id));

        try {
            JSONArray uploadDynamicVocabs = kryptonWsSessionObjectsHelper.mergeSessionObjects(objectsToLoad, loadedObjects, new JSONArray());
            assertEquals(1, loadedObjects.length());
            assertEquals(0, uploadDynamicVocabs.length());
            assertEquals(1, kryptonWsSession.getSessionObjects().size());
            assertTrue(kryptonWsSession.getSessionObjects().containsKey(id));
            assertEquals("some_id", kryptonWsSession.getSessionObjects().get(id).getString("id"));
            assertEquals("application/x-nuance-domainlm", kryptonWsSession.getSessionObjects().get(id).getString("type"));
            assertFalse(kryptonWsSession.getSessionObjects().get(id).has("weight"));
            assertEquals("urn:nuance-mix:tag:model/COMICS/mix.asr?=language=eng-USA", kryptonWsSession.getSessionObjects().get(id).getString("url"));
        }
        catch (Exception e) {
            fail();
        }
    }
}

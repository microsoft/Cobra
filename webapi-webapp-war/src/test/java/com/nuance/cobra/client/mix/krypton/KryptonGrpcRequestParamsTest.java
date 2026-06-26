package com.nuance.cobra.client.mix.krypton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.DynamicVocabularyDb;
import com.nuance.cobra.session.DynamicVocabularyTest;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.UrlUtils;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.plugin.v1.shareable.IConfigProperty;
import com.nuance.rpc.asr.v1.*;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.StringReader;
import java.net.URL;
import java.util.*;

import static com.nuance.cobra.session.WebApiConfig.Property.KRYPTON_DOMAIN_LMS_PER_LANGUAGE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DynamicVocabularyDb.class })
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class KryptonGrpcRequestParamsTest {
    private CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration config = CobraConfigurationUtils.getHighestConfig(configs);
    private KryptonGrpcRequestParams params;
    private RecognitionInitMessage.Builder builder;

    @Before
    public void runsBeforeEachTest()
    {
        builder = RecognitionInitMessage.newBuilder();
        configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        config = CobraConfigurationUtils.getHighestConfig(configs);
    }

    @Test
    public void testGetUri() {
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_SERVER_URL.key, "http://host:443");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        URL url = UrlUtils.getServerUrl(params.getUrl());
        assertEquals("host", UrlUtils.getServerHost(url));

        //invalid url test
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_SERVER_URL.key, "host:443");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        assertNull(UrlUtils.getServerUrl(params.getUrl()));
    }

    @Test
    public void testAddDomainLmWeightParade() throws Exception
    {
        testAddDomainLmWeight("0", 0.0f);
        testAddDomainLmWeight("0.5", 0.5f);
        testAddDomainLmWeight("1", 1.0f);
        testAddDomainLmWeight("150", 0.15f);
        testAddDomainLmWeight("1000", 1.0f);
    }

    public void testAddDomainLmWeight(String weight, float expected) throws Exception
    {
        String uri = "someUri";
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        Map<String, String> loadedDlm = params.addDlm(builder, uri, weight, "");
        assertEquals(uri, loadedDlm.get("uri"));
        assertEquals(expected, Float.parseFloat(loadedDlm.get("weight")), 0.00001);
    }

    @Test
    public void testAddDomainLmWeightEnum() throws Exception {
        String uri = "someUri";
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        Map<String, String> loadedDlm = params.addDlm(builder, uri, "lowest", "");
        assertEquals(uri, loadedDlm.get("uri"));
        assertEquals("lowest", loadedDlm.get("weight"));
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAddDomainLmWeight_illegal_negative() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.addDlm(builder, "", "-10", "");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAddDomainLmWeight_illegal_greaterThan1000() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.addDlm(builder, "", "20000", "");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAddDomainLmWeight_illegal_whitespace() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.addDlm(builder, "", "  ", "");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAddDomainLmWeight_illegal_empty() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.addDlm(builder, "", "", "");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAddDomainLmWeight_illegal_alpha() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.addDlm(builder, "", "abdsy1", "");
    }

    @Test
    public void testAddDomainLmWeightFloat_illegal_null() throws Exception {
        String uri = "someUri";
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        Map<String, String> loadedDlm = params.addDlm(builder, uri, null, "");
        assertEquals(uri, loadedDlm.get("uri"));
        assertNull(loadedDlm.get("weight"));
    }

    @Test
    public void testAddDomainLmUrl() throws Exception {
        String uri = "someUri";
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        Map<String, String> loadedDlm = params.addDlm(builder, uri, "1", "");
        assertEquals(uri, loadedDlm.get("uri"));
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAddDomainLmUrl_illegal_null() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.addDlm(builder, null, "1", "");
    }

    @Test
    public void testAddDlmsPerLang_1Dlm() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List fakeList = List.of(Map.of("uri", "someUri", "weight", "1"));
            configs.setProperty(KRYPTON_DOMAIN_LMS_PER_LANGUAGE.key, fakeList);
            List<Map<String, String>> loadedDlms = params.addDlmsPerLang(builder);
            assertEquals(fakeList.size(), loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddDlmsPerLang_empty() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addDlmsPerLang(builder);
            assertEquals(0, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddKryptonDlm() {
        try {
            config.setProperty(WebApiConfig.Property.KRYPTON_DOMAIN_LM_URL.key, "someUri");
            configs.setProperty(WebApiConfig.Property.KRYPTON_DOMAIN_LM_WEIGHT.key, "10");
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addKryptonDlm(builder);
            assertEquals(1, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddKryptonDlm_noWeight() {
        try {
            config.setProperty(WebApiConfig.Property.KRYPTON_DOMAIN_LM_URL.key, "someUri");
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addKryptonDlm(builder);
            assertEquals(1, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddKryptonDlm_empty() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addKryptonDlm(builder);
            assertEquals(0, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddDlmFromDialogModelConfig() {
        try {
            configs.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "7");
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addDlmFromDialogModelConfig(builder);
            assertEquals(1, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddDlmFromDialogModelConfig_empty() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addDlmFromDialogModelConfig(builder);
            assertEquals(0, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddDlmFromDynamicVocabularies() {
        try {
            configs.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "7");

            var contextTag = "context-tag";
            List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
            DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_DOMAINLM, "urn:nuance-mix:tag:model/"+contextTag+"/mix.asr?=language=eng-USA", null, "lowest");
            dynamicVocabularies.add(dynamicVocabulary);

            params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addDlmFromDynamicVocabularies(builder);
            assertEquals(1, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddDlmFromDynamicVocabularies_empty() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            List<Map<String, String>> loadedDlms = params.addDlmFromDynamicVocabularies(builder);
            assertEquals(0, loadedDlms.size());
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddBuiltinsWithDynamicVocabularies() {
        try {
            var builtinsMap = new ObjectMapper().readValue("{\"builtin\": \"DURATION\", \"weight\": \"low\"}", Map.class);
            var tenantConfigBuiltins = new ArrayList<Map>();
            tenantConfigBuiltins.add(builtinsMap);
            configs.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_BUILTINS.key ,tenantConfigBuiltins);

            List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
            DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_BUILTIN, null, "TEMPERATURE", "0.05");
            dynamicVocabularies.add(dynamicVocabulary);

            params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
            params.addBuiltins(builder);
            assertNotNull(builder.getResources(0));
            assertEquals("DURATION", builder.getResources(0).getBuiltin());
            assertEquals("TEMPERATURE", builder.getResources(1).getBuiltin());
        }
        catch (CobraException | ExceptionWithExternalResponseErrorCode | JsonProcessingException e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddBuiltinsDynamicVocabulariesNull() {
        try {
            var builtinsMap = new ObjectMapper().readValue("{\"builtin\": \"DURATION\", \"weight\": \"low\"}", Map.class);
            var tenantConfigBuiltins = new ArrayList<Map>();
            tenantConfigBuiltins.add(builtinsMap);
            configs.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_BUILTINS.key, tenantConfigBuiltins);
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            params.addBuiltins(builder);
            assertNotNull(builder.getResources(0));
            assertEquals("DURATION", builder.getResources(0).getBuiltin());
        }
        catch (CobraException | ExceptionWithExternalResponseErrorCode | JsonProcessingException e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddBuiltinsFloatWeight() {
        try {
            var builtinsMap = new ObjectMapper().readValue("{\"builtin\": \"DURATION\", \"weight\": \"0.05\"}", Map.class);
            var tenantConfigBuiltins = new ArrayList<Map>();
            tenantConfigBuiltins.add(builtinsMap);
            configs.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_BUILTINS.key, tenantConfigBuiltins);

            List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
            DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_BUILTIN, null, "TEMPERATURE", "0.05");
            dynamicVocabularies.add(dynamicVocabulary);

            params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
            params.addBuiltins(builder);
            assertNotNull(builder.getResources(0));
            assertEquals(0.05F, builder.getResources(0).getWeightValue(), 0.00001);
            assertEquals(0.05F, builder.getResources(1).getWeightValue(), 0.00001);
        }
        catch (CobraException | ExceptionWithExternalResponseErrorCode | JsonProcessingException e) {
            assertNull(e);
        }
    }

    @Test
    public void testAddBuiltinsEnumWeight() {
        try {
            var builtinsMap = new ObjectMapper().readValue("{\"builtin\": \"DURATION\", \"weight\": \"low\"}", Map.class);
            var tenantConfigBuiltins = new ArrayList<Map>();
            tenantConfigBuiltins.add(builtinsMap);
            configs.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_BUILTINS.key, tenantConfigBuiltins);

            List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
            DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_BUILTIN, null, "TEMPERATURE", "low");
            dynamicVocabularies.add(dynamicVocabulary);

            params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
            params.addBuiltins(builder);
            assertNotNull(builder.getResources(0));
            assertNotNull(builder.getResources(0).getWeightEnum());
            assertNotNull(builder.getResources(1).getWeightEnum());
        }
        catch (CobraException | ExceptionWithExternalResponseErrorCode | JsonProcessingException e) {
            assertNull(e);
        }
    }

    @Test
    public void testConvertWeightFloat() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            var weight = params.convertWeight("0.05");
            assertTrue(weight instanceof Float);
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testConvertWeightFloatOverOne() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            var weight = params.convertWeight("50");
            assertTrue(weight instanceof Float);
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test
    public void testConvertWeightEnum() {
        try {
            params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
            var weight = params.convertWeight("low");
            assertTrue(weight instanceof EnumWeight);
        }
        catch (ExceptionWithExternalResponseErrorCode e) {
            assertNull(e);
        }
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testConvertWeight_illegal_negative() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.convertWeight("-10");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testConvertWeight_illegal_greaterThan1000() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.convertWeight("20000");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testConvertWeight_illegal_whitespace() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.convertWeight("  ");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testConvertWeight_illegal_empty() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.convertWeight("");
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testConvertWeight_illegal_alpha() throws Exception {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        params.convertWeight("abdsy1");
    }

    @Test
    public void testGetResultType() {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertEquals(EnumResultType.PARTIAL, params.getResultType());

        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_IMMUTABLE_PARTIAL_RESULTS.key, true);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertEquals(EnumResultType.IMMUTABLE_PARTIAL, params.getResultType());

        config.setProperty(WebApiConfig.Property.WORD_STREAM.key, false);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertEquals(EnumResultType.FINAL, params.getResultType());
    }

    @Test
    public void testShouldStopOnEndOfSpeech() {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertTrue(params.shouldStopOnEndOfSpeech());

        config.setProperty(WebApiConfig.Property.STOP_ON_END_OF_SPEECH.key, false);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertFalse(params.shouldStopOnEndOfSpeech());
    }

    @Test
    public void testGetAudioFormat_pcm_16_16k() {
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "pcm_16_16k");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        AudioFormat expectedFormat = AudioFormat.newBuilder()
                                                .setPcm(PCM.newBuilder()
                                                .setSampleRateHz(16000))
                                                .build();

        assertEquals(expectedFormat, params.getAudioFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAudioFormat_unknown() {
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "unknown");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        params.getAudioFormat();
    }

    @Test
    public void testIsEndpointingActivated() {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertTrue(params.isEndpointingActivated());

        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertFalse(params.isEndpointingActivated());
    }

    @Test
    public void testShouldMaskUserUtterance() {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertTrue(params.shouldMaskUserUtterance());

        config.setProperty(WebApiConfig.Property.SENSITIVITY.key, "masked");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertTrue(params.shouldMaskUserUtterance());

        config.setProperty(WebApiConfig.Property.SENSITIVITY.key, "encrypted");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertTrue(params.shouldMaskUserUtterance());

        config.setProperty(WebApiConfig.Property.KRYPTON_SUPPRESS_CALL_RECORDING.key, true);
        config.setProperty(WebApiConfig.Property.SENSITIVITY.key, "open");
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertTrue(params.shouldMaskUserUtterance());
    }

    @Test
    public void testBuildSpeechDomain()
    {
        String speechDomain = "ivr";
        config.setProperty(WebApiConfig.Property.SPEECH_DOMAIN.key, speechDomain);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        RecognitionParameters.Builder recognitionParametersBuilder = RecognitionParameters.newBuilder();
        params.buildSpeechDomain(recognitionParametersBuilder);
        assertEquals(speechDomain, recognitionParametersBuilder.getSpeechDomain());
    }

    @Test
    public void testBuildTopic()
    {
        RecognitionParameters.Builder recognitionParametersBuilder = RecognitionParameters.newBuilder();
        assertEquals("", recognitionParametersBuilder.getTopic());

        String kryptonTopic = "kryptonTopic";
        config.setProperty(WebApiConfig.Property.KRYPTON_TOPIC.key, kryptonTopic);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        params.buildTopic(recognitionParametersBuilder);
        assertEquals(kryptonTopic, recognitionParametersBuilder.getTopic());

        String topic = "topic";
        config.setProperty(WebApiConfig.Property.TOPIC.key, topic);

        params.buildTopic(recognitionParametersBuilder);
        assertEquals(topic, recognitionParametersBuilder.getTopic());
    }

//    @Test
//    public void testGetDlmPropertyFromPerLang()
//            throws ConfigurationException, ExceptionWithExternalResponseErrorCode
//    {
//        String weight = "medium";
//        String uri = "https://host/dlms/eng-USA.zip";
//        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
//        webApiJSONConfiguration.read(new StringReader("{\n" +
//                                                      "  \"kryptonDomainLmsPerLanguage\": {" +
//                                                      "    \"eng-USA\": [{\"weight\": \"" + weight +"\", \"uri\":\"" + uri + "\"}]" +
//                                                      "   }" +
//                                                      "}"));
//        configs.addConfiguration(webApiJSONConfiguration);
//
//        config.setProperty(WebApiConfig.Property.KRYPTON_LANGUAGE_PACK.key, "eng-USA");
//        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
//
//        assertEquals(uri, params.getDlmPropertyFromPerLang("uri", 0));
//        assertEquals(weight, params.getDlmPropertyFromPerLang("weight", 0));
//
//        assertEquals(KryptonGrpcRequestParams.DomainLmWeightType.ENUM, params.getResourceDomainLmWeightType(0));
//        assertEquals(EnumWeight.MEDIUM, params.getResourceDomainLmWeightEnum(0));
//        assertEquals(uri, params.getResourceDomainLmUri(0));
//    }

    @Test
    public void testAddResourceHeaders()
            throws ConfigurationException
    {
        String header = "cache-control";
        String value = "max-age=600,max-stale=300";
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        webApiJSONConfiguration.read(new StringReader("{\n" +
                                                      "  \"kryptonGrpcResourceHeaders\": {" +
                                                      "    \"" + header + "\": \"" + value + "\"" +
                                                      "   }" +
                                                      "}"));
        configs.addConfiguration(webApiJSONConfiguration);

        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        ResourceReference.Builder resourceReferenceBuilder = ResourceReference.newBuilder();
        params.addResourceHeaders(resourceReferenceBuilder);
        assertEquals(value, resourceReferenceBuilder.getHeadersMap().get(header));
    }

    @Test
    public void testGetDlmRequestTimeout()
    {
        final int CONFIG_RESOURCE_TIMEOUT_MS = 4000;
        final int CONFIG_DLM_TIMEOUT_MS = 5000;

        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.key, CONFIG_RESOURCE_TIMEOUT_MS);
        KryptonGrpcRequestParams params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        // no timeout specified in map, value from config is used
        Map dlm = new HashMap();
        assertEquals(CONFIG_RESOURCE_TIMEOUT_MS, (int) params.getDlmRequestTimeout(dlm));

        // timeout specified in request takes precedence
        final int REQUEST_DLM_TIMEOUT_MS = 2500;
        dlm.put("requestTimeout", REQUEST_DLM_TIMEOUT_MS);
        assertEquals(REQUEST_DLM_TIMEOUT_MS, (int) params.getDlmRequestTimeout(dlm));

        // fallback to configured value if value is invalid
        dlm.put("requestTimeout", "invalid");
        assertEquals(CONFIG_RESOURCE_TIMEOUT_MS, (int) params.getDlmRequestTimeout(dlm));

        // configure a more specific value for DLM in config, has precedence over
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_DLM_RESOURCE_REQUEST_TIMEOUT_MS.key, CONFIG_DLM_TIMEOUT_MS);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        assertEquals(CONFIG_DLM_TIMEOUT_MS, (int) params.getDlmRequestTimeout(dlm));
    }

    @Test
    public void setUtteranceEndSilenceMs()
    {
        config.setProperty(WebApiConfig.Property.KRYPTON_UTTERANCE_END_SILENCE_MS.key, 125);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        RecognitionParameters.Builder recognitionParametersBuilder = RecognitionParameters.newBuilder();
        params.setUtteranceEndSilenceMs(recognitionParametersBuilder);
        assertEquals(125, recognitionParametersBuilder.getUtteranceEndSilenceMs());

        config.setProperty(WebApiConfig.Property.KRYPTON_UTTERANCE_END_SILENCE_MS.key, -1);
        params.setUtteranceEndSilenceMs(recognitionParametersBuilder);
        assertEquals(125, recognitionParametersBuilder.getUtteranceEndSilenceMs());
    }

    @Test
    public void setSpeechDetectionSensitivty()
    {
        config.setProperty(WebApiConfig.Property.KRYPTON_SPEECH_DETECTION_SENSITIVITY.key, 600);
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        RecognitionParameters.Builder recognitionParametersBuilder = RecognitionParameters.newBuilder();
        params.setSpeechDetectionSensitivty(recognitionParametersBuilder);
        assertEquals(0.6, recognitionParametersBuilder.getSpeechDetectionSensitivity(), 0.0000003);

        config.setProperty(WebApiConfig.Property.KRYPTON_SPEECH_DETECTION_SENSITIVITY.key, -1);
        params.setSpeechDetectionSensitivty(recognitionParametersBuilder);
        assertEquals(0.6, recognitionParametersBuilder.getSpeechDetectionSensitivity(), 0.0000003);

        config.setProperty(WebApiConfig.Property.KRYPTON_SPEECH_DETECTION_SENSITIVITY.key, 1000);
        params.setSpeechDetectionSensitivty(recognitionParametersBuilder);
        assertEquals(1.0, recognitionParametersBuilder.getSpeechDetectionSensitivity(), 0.0000003);

        config.setProperty(WebApiConfig.Property.KRYPTON_SPEECH_DETECTION_SENSITIVITY.key, 0);
        params.setSpeechDetectionSensitivty(recognitionParametersBuilder);
        assertEquals(0.0, recognitionParametersBuilder.getSpeechDetectionSensitivity(), 0.0000003);
    }

    @Test
    public void setDiscardSpeakerAdaptation()
    {
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        RecognitionFlags.Builder recognitionFlagsBuilder = RecognitionFlags.newBuilder();
        params.setDiscardSpeakerAdaptation(recognitionFlagsBuilder);
        assertFalse(recognitionFlagsBuilder.getDiscardSpeakerAdaptation());

        config.setProperty(WebApiConfig.Property.KRYPTON_DISCARD_SPEAKER_ADAPTATION.key, false);
        params.setDiscardSpeakerAdaptation(recognitionFlagsBuilder);
        assertFalse(recognitionFlagsBuilder.getDiscardSpeakerAdaptation());

        config.setProperty(WebApiConfig.Property.KRYPTON_DISCARD_SPEAKER_ADAPTATION.key, true);
        params.setDiscardSpeakerAdaptation(recognitionFlagsBuilder);
        assertTrue(recognitionFlagsBuilder.getDiscardSpeakerAdaptation());
    }

    @Test
    public void setSpeakerProfileTimeout() throws CobraException {

        config.setProperty(WebApiConfig.Property.ENABLE_SPEECH_RECOGNITION_SPEAKER_PROFILE.key, true);

        // resource loaded with default Krypton GRPC resource load timeout (no value present in config)
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        RecognitionInitMessage.Builder initMessage = RecognitionInitMessage.newBuilder();
        params.setSpeakerProfile(initMessage);

        assertEquals(EnumResourceType.SPEAKER_PROFILE, initMessage.getResources(0).getExternalReference().getType());
        assertEquals(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.getDefaultValue(),
                initMessage.getResources(0).getExternalReference().getRequestTimeoutMs());

        // resource loaded with general Krypton GRPC resource load  timeout (applies to all resources)
        final int RESOURCE_TIMEOUT_MS = 18000;
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.key, RESOURCE_TIMEOUT_MS);

        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        initMessage = RecognitionInitMessage.newBuilder();
        params.setSpeakerProfile(initMessage);
        assertEquals(EnumResourceType.SPEAKER_PROFILE, initMessage.getResources(0).getExternalReference().getType());
        assertEquals(RESOURCE_TIMEOUT_MS, initMessage.getResources(0).getExternalReference().getRequestTimeoutMs());

        // resource loaded with specific Krypton GRPC resource load  timeout (applies only to speaker profile)
        final int SPREAKER_PROFILE_RESOURCE_TIMEOUT_MS = 12500;
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_SPEAKER_PROFILE_RESOURCE_REQUEST_TIMEOUT_MS.key, SPREAKER_PROFILE_RESOURCE_TIMEOUT_MS);

        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));
        initMessage = RecognitionInitMessage.newBuilder();
        params.setSpeakerProfile(initMessage);
        assertEquals(EnumResourceType.SPEAKER_PROFILE, initMessage.getResources(0).getExternalReference().getType());
        assertEquals(SPREAKER_PROFILE_RESOURCE_TIMEOUT_MS, initMessage.getResources(0).getExternalReference().getRequestTimeoutMs());
    }

    @Test
    public void addWordsetsTimeouts() throws CobraException {

        // build mock dynamic vocabularies list
        DynamicVocabulary dynamicVocabulary = mock(DynamicVocabulary.class);
        ArrayList<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
        dynamicVocabularies.add(dynamicVocabulary);
        final String WORDSET_URI = "urn:blah";
        when(dynamicVocabulary.getType()).thenReturn(DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET_PKG);
        when(dynamicVocabulary.getUri()).thenReturn(WORDSET_URI);

        // resource loaded with default GRPC timeout (not present in config)
        RecognitionInitMessage.Builder initMessage = RecognitionInitMessage.newBuilder();
        params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
        params.addWordsets(initMessage);
        assertEquals(WORDSET_URI, initMessage.getResources(0).getExternalReference().getUri());
        assertEquals(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.getDefaultValue(),
                initMessage.getResources(0).getExternalReference().getRequestTimeoutMs());

        // resource loaded with a specific resource GRPC timeout defined in configuration.
        initMessage = RecognitionInitMessage.newBuilder();
        final int RESOURCE_TIMEOUT_MS = 18000;
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.key, RESOURCE_TIMEOUT_MS);
        params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
        params.addWordsets(initMessage);
        assertEquals(WORDSET_URI, initMessage.getResources(0).getExternalReference().getUri());
        assertEquals(RESOURCE_TIMEOUT_MS, initMessage.getResources(0).getExternalReference().getRequestTimeoutMs());

        // resource loaded with a specific wordset GRPC timeout (has precedence over the generic resource one.)
        initMessage = RecognitionInitMessage.newBuilder();
        final int WORDSET_TIMEOUT_MS = 23000;
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.key, WORDSET_TIMEOUT_MS);

        params = new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class));
        params.addWordsets(initMessage);
        assertEquals(WORDSET_URI, initMessage.getResources(0).getExternalReference().getUri());
        assertEquals(WORDSET_TIMEOUT_MS, initMessage.getResources(0).getExternalReference().getRequestTimeoutMs());
    }


    @Test
    public void loadPersistedWordsets()
    {
        RecognitionInitMessage.Builder initMessage = RecognitionInitMessage.newBuilder();
        params = new KryptonGrpcRequestParams(configs, mock(Logger.class));

        params.loadPersistedWordsets(initMessage);

        config.setProperty(WebApiConfig.Property.LOAD_PERSISTED_WORDSETS.key, true);
        params.loadPersistedWordsets(initMessage);

        PowerMockito.mockStatic(DynamicVocabularyDb.class);
        PowerMockito.when(DynamicVocabularyDb.getPersistedWordsets(any())).thenReturn(Set.of("place-name", "hockey-team"));
        params.loadPersistedWordsets(initMessage);

        DynamicVocabulary dynamicVocabularyInline = DynamicVocabulary.fromJsonMap(DynamicVocabularyTest.getInlineJson().toMap(), false, configs);
        PowerMockito.when(DynamicVocabularyDb.getWordset(anyString(), any())).thenReturn(dynamicVocabularyInline);
        params.loadPersistedWordsets(initMessage);
        assertTrue(initMessage.getResourcesCount() > 0);
    }
}

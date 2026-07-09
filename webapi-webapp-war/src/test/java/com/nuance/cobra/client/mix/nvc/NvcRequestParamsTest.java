package com.nuance.cobra.client.mix.nvc;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.UrlUtils;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.rpc.tts.v1.SynthesizerProto;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.net.URL;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static org.junit.Assert.*;

public class NvcRequestParamsTest {
    private CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration config = configs.getConfiguration(0);
    private NvcRequestParams params;

    @Before
    public void runsBeforeEachTest() {
        // tenant config
        config.setProperty(WebApiConfig.Property.NVC_GRPC_SERVER_URL.key, "https://host:443");

        config.setProperty("vocsMapping.Evan.language", "en-us");
        config.setProperty("vocsMapping.Evan.name", "Evan");

        config.setProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.setProperty("vocsMapping.Evan.opus_nb.voice_model", "enhanced");
        config.setProperty("vocsMapping.Evan.opus_nb.vbr", "off");
        config.setProperty("vocsMapping.Evan.opus_nb.maxFrameDuration", "60");
        config.setProperty("vocsMapping.Evan.opus_wb.voice_model", "enhanced");
        config.setProperty("vocsMapping.Evan.opus_wb.vbr", "off");
        config.setProperty("vocsMapping.Evan.opus_wb.maxFrameDuration", "60");

        // start session command
        config.setProperty("speechSynthesisCodec", "pcm_16_16k");

        // speech synthesis command
        config.setProperty("type", "text");
        config.setProperty("data", "hi");
        config.setProperty("voice", "Evan");

        params = new NvcRequestParams(configs);
    }

    @Test
    public void testGetUri() {
        config.setProperty(WebApiConfig.Property.NVC_GRPC_SERVER_URL.key, "http://host:443");
        params = new NvcRequestParams(configs);

        URL url = UrlUtils.getServerUrl(params.getUrl());
        assertEquals("host", UrlUtils.getServerHost(url));

        //invalid url test
        config.setProperty(WebApiConfig.Property.NVC_GRPC_SERVER_URL.key, "host:443");
        params = new NvcRequestParams(configs);

        assertNull(UrlUtils.getServerUrl(params.getUrl()));
    }

    @Test
    public void getVoiceName() throws InvalidKeySpecException
    {
        assertEquals("Evan", params.getVoiceName());
    }

    @Test
    public void getVoiceModel() throws InvalidKeySpecException {
        assertEquals("enhanced", params.getVoiceModel());
    }

    @Test
    public void getVoiceLanguage() throws InvalidKeySpecException {
        assertEquals("en-us", params.getVoiceLanguage());
    }

    @Test
    public void getInputType_text() {
        assertEquals("text", params.getInputType());
    }

    @Test
    public void getInputType_ssml() {
        config.setProperty("type", "ssml");
        params = new NvcRequestParams(configs);

        assertEquals("ssml", params.getInputType());
    }

    @Test
    public void getInputType_other() {
        config.setProperty("type", "other");
        params = new NvcRequestParams(configs);

        assertEquals("other", params.getInputType());
    }

    @Test
    public void getInputType_null() {
        configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        config = configs.getConfiguration(0);
        params = new NvcRequestParams(configs);

        assertNull(params.getInputType());
    }

    @Test
    public void getInputBody() {
        assertEquals("hi", params.getInputBody());
    }

    @Test
    public void getAudioFormat_pcm_16_8k() {
        config.setProperty("speechSynthesisCodec", "pcm_16_8k");
        params = new NvcRequestParams(configs);

        SynthesizerProto.AudioFormat expectedFormat = SynthesizerProto.AudioFormat.newBuilder()
                                                                                  .setPcm(SynthesizerProto.PCM.newBuilder().setSampleRateHz(8000))
                                                                                  .build();

        assertEquals(expectedFormat, params.getAudioFormat());
    }

    @Test
    public void getAudioFormat_pcm_16_16k() {
        SynthesizerProto.AudioFormat expectedFormat = SynthesizerProto.AudioFormat.newBuilder()
                .setPcm(SynthesizerProto.PCM.newBuilder()
                        .setSampleRateHz(16000))
                .build();

        assertEquals(expectedFormat, params.getAudioFormat());
    }

    @Test
    public void getAudioFormat_pcm_16_22k() {
        config.setProperty("speechSynthesisCodec", "pcm_16_22k");
        params = new NvcRequestParams(configs);

        SynthesizerProto.AudioFormat expectedFormat = SynthesizerProto.AudioFormat.newBuilder()
                .setPcm(SynthesizerProto.PCM.newBuilder()
                        .setSampleRateHz(22050)).build();

        assertEquals(expectedFormat, params.getAudioFormat());
    }

    @Test
    public void getVbr()
    {
        config.setProperty("speechSynthesisCodec", "opus_wb");
        params = new NvcRequestParams(configs);

        config.clearProperty("vocsMapping.Evan.opus_wb.vbr");
        assertEquals(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF, params.getVbr());

        config.setProperty("vocsMapping.Evan.opus_wb.vbr", "on");
        assertEquals(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_ON, params.getVbr());

        config.setProperty("vocsMapping.Evan.opus_wb.vbr", "constrained");
        assertEquals(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_CONSTRAINED, params.getVbr());

        config.setProperty("vocsMapping.Evan.opus_wb.vbr", "off");
        assertEquals(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF, params.getVbr());

        config.setProperty("vocsMapping.Evan.opus_wb.vbr", "exceptional");
        assertEquals(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF, params.getVbr());
    }

    @Test
    public void getMaxFrameDurationMs()
    {
        config.setProperty("speechSynthesisCodec", "opus_wb");
        params = new NvcRequestParams(configs);

        config.setProperty("vocsMapping.Evan.opus_wb.maxFrameDuration", "120");
        assertEquals(120, params.getMaxFrameDurationMs());

        config.setProperty("vocsMapping.Evan.opus_wb.maxFrameDuration", "abc");
        assertEquals(60, params.getMaxFrameDurationMs());
    }


    @Test
    public void getAudioFormat_pure_opus_nb() {
        config.setProperty("speechSynthesisCodec", "opus_nb");
        params = new NvcRequestParams(configs);

        SynthesizerProto.AudioFormat expectedFormat = SynthesizerProto.AudioFormat.newBuilder()
                                                                    .setOpus(SynthesizerProto.Opus.newBuilder()
                        .setSampleRateHz(8000).setVbr(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF).setMaxFrameDurationMs(60)).build();

        assertEquals(expectedFormat, params.getAudioFormat());
    }

    @Test
    public void getAudioFormat_pure_opus_wb() {
        config.setProperty("speechSynthesisCodec", "opus_wb");
        params = new NvcRequestParams(configs);

        SynthesizerProto.AudioFormat expectedFormat = SynthesizerProto.AudioFormat.newBuilder()
                .setOpus(SynthesizerProto.Opus.newBuilder()
                        .setSampleRateHz(16000).setVbr(SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF).setMaxFrameDurationMs(60)).build();

        assertEquals(expectedFormat, params.getAudioFormat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAudioFormat_unknown() {
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key, "unknown");
        params = new NvcRequestParams(configs);

        params.getAudioFormat();
    }

    @Test
    public void testHasSpeakingRateFactor() {
        assertFalse(params.hasSpeakingRateFactor());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "1");
        params = new NvcRequestParams(configs);

        assertTrue(params.hasSpeakingRateFactor());
    }

    @Test
    public void testGetSpeakingRateFactor() throws ExceptionWithExternalResponseErrorCode {
//        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "slow");
//        params = new NvcRequestParams(configs);
//
//        assertEquals(0.5, params.getSpeakingRateFactor(), 0);
//
//        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "normal");
//        params = new NvcRequestParams(configs);
//
//        assertEquals(1.0, params.getSpeakingRateFactor(), 0);
//
//        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "fast");
//        params = new NvcRequestParams(configs);
//
//        assertEquals(2.0, params.getSpeakingRateFactor(), 0);

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "0.75");
        params = new NvcRequestParams(configs);

        assertEquals(0.75, params.getSpeakingRateFactor(), 0);
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testGetSpeakingRateFactor_invalid() throws ExceptionWithExternalResponseErrorCode {
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "woupelaye");
        params = new NvcRequestParams(configs);

        params.getSpeakingRateFactor();
    }

    @Test
    public void testAssertValidity_valid() throws ExceptionWithExternalResponseErrorCode {
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "0");
        params = new NvcRequestParams(configs);

        params.assertValidity();

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "2");
        params = new NvcRequestParams(configs);

        params.assertValidity();
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAssertValidity_invalid_1() throws ExceptionWithExternalResponseErrorCode {
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "-1");
        params = new NvcRequestParams(configs);

        params.assertValidity();
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAssertValidity_invalid_2() throws ExceptionWithExternalResponseErrorCode {
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "2.01");
        params = new NvcRequestParams(configs);

        params.assertValidity();
    }

    @Test
    public void testSpeechSynthesisTuningBundles_textRuleSet() throws ConfigurationException, ExceptionWithExternalResponseErrorCode {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "    \"speechSynthesisTuningBundle\": \"pizza\",\n" +
                "    \"speechSynthesisTuningBundles\":\n" +
                "    {\n" +
                "        \"pizza\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"content_type\": \"application/x-vocalizer-rettt+text\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/TEXT_RULE_SET/mix.tts?=language=en-US\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"sports\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/DICTIONARY/mix.tts?=language=en-US\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"vocsMapping\":\n" +
                "    {\n" +
                "        \"Evan\":\n" +
                "        {}\n" +
                "    },\n" +
                "    \"voice\": \"Evan\"\n" +
                "}"));

        params = new NvcRequestParams(configs);

        List<SynthesizerProto.SynthesisResource.Builder> speechSynthesisTuningBundleResources = params.getSynthesisResources();

        assertEquals(1, speechSynthesisTuningBundleResources.size());
        assertEquals(SynthesizerProto.EnumResourceType.TEXT_USER_RULESET, speechSynthesisTuningBundleResources.get(0).getType());
        assertEquals("urn:nuance-mix:tag:model/TEXT_RULE_SET/mix.tts?=language=en-US", speechSynthesisTuningBundleResources.get(0).getUri());
    }

    @Test
    public void testSpeechSynthesisTuningBundles_dictionary() throws ConfigurationException, ExceptionWithExternalResponseErrorCode {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "    \"speechSynthesisTuningBundle\": \"sports\",\n" +
                "    \"speechSynthesisTuningBundles\":\n" +
                "    {\n" +
                "        \"pizza\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"content_type\": \"application/x-vocalizer-rettt+text\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/TEXT_RULE_SET/mix.tts?=language=en-US\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"sports\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/DICTIONARY/mix.tts?=language=en-US\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"content_type\": \"application/x-vocalizer-activeprompt-db\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/ACTIVEPROMPT_DB/mix.tts?=language=en-US\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"content_type\": \"application/x-vocalizer-activeprompt-db;mode=automatic\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/ACTIVEPROMPT_DB_AUTO/mix.tts?=language=en-US\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"content_type\": \"application/sdct-bin-dictionary\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/SYSTEM_DICTIONARY/mix.tts?=language=en-US\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"vocsMapping\":\n" +
                "    {\n" +
                "        \"Evan\":\n" +
                "        {}\n" +
                "    },\n" +
                "    \"voice\": \"Evan\"\n" +
                "}"));

        params = new NvcRequestParams(configs);

        List<SynthesizerProto.SynthesisResource.Builder> speechSynthesisTuningBundleResources = params.getSynthesisResources();

        assertEquals(4, speechSynthesisTuningBundleResources.size());
        assertEquals(SynthesizerProto.EnumResourceType.USER_DICTIONARY, speechSynthesisTuningBundleResources.get(0).getType());
        assertEquals("urn:nuance-mix:tag:model/DICTIONARY/mix.tts?=language=en-US", speechSynthesisTuningBundleResources.get(0).getUri());
        assertEquals(SynthesizerProto.EnumResourceType.ACTIVEPROMPT_DB, speechSynthesisTuningBundleResources.get(1).getType());
        assertEquals("urn:nuance-mix:tag:model/ACTIVEPROMPT_DB/mix.tts?=language=en-US", speechSynthesisTuningBundleResources.get(1).getUri());
        assertEquals(SynthesizerProto.EnumResourceType.ACTIVEPROMPT_DB_AUTO, speechSynthesisTuningBundleResources.get(2).getType());
        assertEquals("urn:nuance-mix:tag:model/ACTIVEPROMPT_DB_AUTO/mix.tts?=language=en-US", speechSynthesisTuningBundleResources.get(2).getUri());
        assertEquals(SynthesizerProto.EnumResourceType.SYSTEM_DICTIONARY, speechSynthesisTuningBundleResources.get(3).getType());
        assertEquals("urn:nuance-mix:tag:model/SYSTEM_DICTIONARY/mix.tts?=language=en-US", speechSynthesisTuningBundleResources.get(3).getUri());
    }

    @Test
    public void testSpeechSynthesisTuningBundles_noSpeechSynthesisTuningBundles() throws ConfigurationException {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "    \"speechSynthesisTuningBundle\": \"sports\",\n" +
                "    \"vocsMapping\":\n" +
                "    {\n" +
                "        \"Evan\":\n" +
                "        {}\n" +
                "    },\n" +
                "    \"voice\": \"Evan\"\n" +
                "}"));

        params = new NvcRequestParams(configs);

        try {
            params.getSynthesisResources();
        } catch (ExceptionWithExternalResponseErrorCode e) {
            assertEquals(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_TUNINGBUNDLES_ERROR, e.getErrorCode());
        }
    }

    @Test
    public void testSpeechSynthesisTuningBundles_noSpeechSynthesisTuningBundle() throws ConfigurationException {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "    \"speechSynthesisTuningBundle\": \"pizza\",\n" +
                "    \"speechSynthesisTuningBundles\":\n" +
                "    {\n" +
                "        \"sports\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "                \"uri\": \"urn:nuance-mix:tag:model/DICTIONARY/mix.tts?=language=en-US\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"vocsMapping\":\n" +
                "    {\n" +
                "        \"Evan\":\n" +
                "        {}\n" +
                "    },\n" +
                "    \"voice\": \"Evan\"\n" +
                "}"));

        params = new NvcRequestParams(configs);

        try {
            params.getSynthesisResources();
        } catch (ExceptionWithExternalResponseErrorCode e) {
            assertEquals(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_TUNINGBUNDLE_ERROR, e.getErrorCode());
        }
    }

    @Test
    public void testSpeechSynthesisTuningBundles_noSpeechSynthesisTuningBundle2() throws ConfigurationException {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "    \"speechSynthesisTuningBundle\": \"sports\",\n" +
                "    \"speechSynthesisTuningBundles\":\n" +
                "    {\n" +
                "        \"sports\":\n" +
                "        {\n" +
                "            \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "            \"uri\": \"urn:nuance-mix:tag:model/DICTIONARY/mix.tts?=language=en-US\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"vocsMapping\":\n" +
                "    {\n" +
                "        \"Evan\":\n" +
                "        {}\n" +
                "    },\n" +
                "    \"voice\": \"Evan\"\n" +
                "}"));

        params = new NvcRequestParams(configs);

        try {
            params.getSynthesisResources();
        } catch (ExceptionWithExternalResponseErrorCode e) {
            assertEquals(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_TUNINGBUNDLE_ERROR, e.getErrorCode());
        }
    }

    @Test
    public void testSpeechSynthesisTuningBundles_speechSynthesisSyntaxError() throws ConfigurationException {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "    \"speechSynthesisTuningBundle\": \"sports\",\n" +
                "    \"speechSynthesisTuningBundles\":\n" +
                "    {\n" +
                "        \"sports\": \"urn:nuance-mix:tag:model/DICTIONARY/mix.tts?=language=en-US\"\n" +
                "    },\n" +
                "    \"vocsMapping\":\n" +
                "    {\n" +
                "        \"Evan\":\n" +
                "        {}\n" +
                "    },\n" +
                "    \"voice\": \"Evan\"\n" +
                "}"));

        params = new NvcRequestParams(configs);

        try {
            params.getSynthesisResources();
        } catch (ExceptionWithExternalResponseErrorCode e) {
            assertEquals(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_TUNINGBUNDLE_SYNTAX_ERROR, e.getErrorCode());
        }
    }

    @Test
    public void testHasAudioChunkDurationMs() {
        assertFalse(params.hasAudioChunkDurationMs());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS.key, 1);
        params = new NvcRequestParams(configs);

        assertTrue(params.hasAudioChunkDurationMs());
    }

    @Test
    public void testHasAudioChunkDurationMs_negative() {
        assertFalse(params.hasAudioChunkDurationMs());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS.key, -11);
        params = new NvcRequestParams(configs);

        assertFalse(params.hasAudioChunkDurationMs());
    }

    @Test
    public void testHasAudioChunkDurationMs_outOfBound() {
        assertFalse(params.hasAudioChunkDurationMs());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS.key, 100000);
        params = new NvcRequestParams(configs);

        assertFalse(params.hasAudioChunkDurationMs());
    }

    @Test
    public void testHasTargetAudioLengthMs() {
        assertFalse(params.hasTargetAudioLengthMs());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS.key, 1);
        params = new NvcRequestParams(configs);

        assertTrue(params.hasTargetAudioLengthMs());
    }

    @Test
    public void testHasTargetAudioLengthMs_negative() {
        assertFalse(params.hasTargetAudioLengthMs());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS.key, -1);
        params = new NvcRequestParams(configs);

        assertFalse(params.hasTargetAudioLengthMs());
    }

    @Test
    public void testHasTargetAudioLengthMs_high() {
        assertFalse(params.hasTargetAudioLengthMs());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS.key, 100000);
        params = new NvcRequestParams(configs);

        assertTrue(params.hasTargetAudioLengthMs());
    }

    @Test
    public void test_getAudioParameters() throws ExceptionWithExternalResponseErrorCode {
        SynthesizerProto.AudioParameters audioParams = params.getAudioParameters();

        assertEquals(0, audioParams.getAudioChunkDurationMs());
        assertEquals(0, audioParams.getTargetAudioLengthMs());
        assertEquals(0, audioParams.getSpeakingRateFactor(), 0.01);
        assertEquals(false, audioParams.getDisableEarlyEmission());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS.key, 15);
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS.key, 100);
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, 1.5);
        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_DISABLE_EARLY_EMISSIONS.key, true);
        params = new NvcRequestParams(configs);
        audioParams = params.getAudioParameters();

        assertEquals(15, audioParams.getAudioChunkDurationMs());
        assertEquals(100, audioParams.getTargetAudioLengthMs());
        assertEquals(1.5, audioParams.getSpeakingRateFactor(), 0.01);
        assertEquals(true, audioParams.getDisableEarlyEmission());

        config.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_DISABLE_EARLY_EMISSIONS.key, false);
        params = new NvcRequestParams(configs);
        audioParams = params.getAudioParameters();

        assertEquals(false, audioParams.getDisableEarlyEmission());
    }

    @Test
    public void getNvcVersion()
    {
        config.setProperty("vocsMapping.Evan.nvcVersion", "nvc3");
        assertEquals("nvc3", params.getVoiceNvcVersion());
    }
}

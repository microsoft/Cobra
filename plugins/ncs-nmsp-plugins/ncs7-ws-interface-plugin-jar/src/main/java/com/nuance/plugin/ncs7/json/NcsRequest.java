package com.nuance.plugin.ncs7.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.plugin.ncs.utils.HashType;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.ncs.utils.StringObfuscator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@java.lang.SuppressWarnings("java:S116")
public class NcsRequest {

    @JsonProperty("message")
    public String message;

    @JsonProperty("device_id")
    public String device_id;

    @JsonProperty("deviceModel")
    public String deviceModel;

    @JsonProperty("user_id")
    public String user_id;

    @JsonProperty("app_id")
    public String app_id;

    @JsonProperty("app_key")
    public String app_key;

    @JsonProperty("transaction_id")
    public String transaction_id;

    @JsonProperty("command")
    public String command;

    @JsonProperty("parameter_name")
    public String parameter_name;

    @JsonProperty("parameter_type")
    public String parameter_type;

    @JsonProperty("language")
    public String language;

    @JsonProperty("dictation_language")
    public String dictation_language;

    @JsonProperty("dictation_type")
    public String dictation_type;

    @JsonProperty("phone_model")
    public String phone_model;

    @JsonProperty("phoneModel")
    public String phoneModel;

    @JsonProperty("phone_number")
    public String phone_number;

    @JsonProperty("tts_voice")
    public String tts_voice;

    @JsonProperty("tts_text_genre")
    public String tts_text_genre;

    @JsonProperty("tts_genre")
    public String tts_genre;

    @JsonProperty("tts_rate")
    public String tts_rate;

    @JsonProperty("subscriber_id")
    public String subscriber_id;

    @JsonProperty("concept_id")
    public String concept_id;

    @JsonProperty("application_session_id")
    public String application_session_id;

    @JsonProperty("context_tag")
    public String context_tag;

    @JsonProperty("locale")
    public String locale;

    @JsonProperty("application_name")
    public String application_name;

    @JsonProperty("application")
    public String application;

    @JsonProperty("field_id")
    public String field_id;

    @JsonProperty("audio_source")
    public String audio_source;

    @JsonProperty("location")
    public String location;

    @JsonProperty("client_hardware_model")
    public String client_hardware_model;

    @JsonProperty("client_handware_model")
    public String client_handware_model;

    @JsonProperty("client_hardware_submodel")
    public String client_hardware_submodel;

    @JsonProperty("nmt_device_model")
    public String nmt_device_model;

    @JsonProperty("nmt_connection_type")
    public String nmt_connection_type;

    @JsonProperty("nmt_app_version_build")
    public String nmt_app_version_build;

    @JsonProperty("phone_submodel")
    public String phone_submodel;

    @JsonProperty("phone_network")
    public String phone_network;

    @JsonProperty("uid")
    public String uid;

    @JsonProperty("application_state_id")
    public String application_state_id;

    @JsonProperty("buffer_id")
    public Integer buffer_id;

    @JsonProperty("type")
    public Integer type;

    @JsonProperty("_IS_LEGACY_CLIENT_")
    public String _IS_LEGACY_CLIENT_;

    @JsonProperty("lcd_height")
    public Integer lcd_height;

    @JsonProperty("lcd_width")
    public Integer lcd_width;

    @JsonProperty("pdx_version")
    public String pdx_version;

    @JsonProperty("client_os_version")
    public String client_os_version;

    @JsonProperty("original_session_id")
    public String original_session_id;

    @JsonProperty("carrier")
    public String carrier;

    @JsonProperty("sdk_version")
    public String sdk_version;

    @JsonProperty("script_version")
    public String script_version;

    @JsonProperty("client_version")
    public String client_version;

    @JsonProperty("region")
    public String region;

    @JsonProperty("new_session_id")
    public String new_session_id;

    @JsonProperty("codec")
//    @JsonDeserialize(using = CodecEnumDeserializer.class)
//    public AudioCodec codec;
    public String codec;

    @JsonProperty("utterance_detection_silence_duration")
    public String utterance_detection_silence_duration;

    @JsonProperty("codec_id")
    public Integer codec_id;

    @JsonProperty("device_codec")
    public Integer device_codec;

    @JsonProperty("audio_id")
    public Integer audio_id;

    @JsonProperty("recognition_type")
    public String recognition_type;

    @JsonProperty("dictionary")
    public Map<String, Object> dictionary = new HashMap<>();

    @JsonProperty("phone_OS")
    public String phone_OS;

    @JsonProperty("utterance_number")
    public String utterance_number;

    @JsonProperty("ui_language")
    public String ui_language;

    @JsonProperty("tts_language")
    public String tts_language;

    @JsonProperty("nmaid")
    public String nmaid;

    @JsonProperty("productImprovements")
    public List<Object> productImprovements;

    @JsonProperty("data_session_id")
    public String data_session_id;

    @JsonProperty("datacollection_prompt")
    public String datacollection_prompt;

    @JsonProperty("datacollection_prompt_type")
    public String datacollection_prompt_type;

    @JsonProperty("datacollection_prompt_index")
    public Integer datacollection_prompt_index;

    @JsonProperty("datacollection_name")
    public String datacollection_name;

    @JsonProperty("datacollection_participant_id")
    public String datacollection_participant_id;

    static final Map<String, Integer> UTTERANCE_DETECTION_SILENCE_DURATIONS = ImmutableMap.of(
            "VeryAggressive", 300,
            "Aggressive", 500,
            "Average", 750,
            "Conservative", 1000
    );

    public static NcsRequest fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return mapper.readValue(json, NcsRequest.class);
    }

    public static NcsRequest fromMap(Map<String, Object> json) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(json, NcsRequest.class);
    }

    public String getCodecName() throws IOException {
        if (codec != null) {
            return codec;
        }

        if (codec_id != null) {
            return ICodecResource.Codec.fromNMAINCodecId(codec_id).getWebAPIString();
        }

        if (device_codec != null) {
            return ICodecResource.Codec.fromNMAINCodecId(device_codec).getWebAPIString();
        }
        return null;
    }

    public Integer getCodecId() {
        if (codec != null) {
            return (int) ICodecResource.Codec.fromWebApiString(getWebAPICodecName(codec)).getNmaidCode();
        }

        if (codec_id != null) {
            return codec_id;
        }

        if (device_codec != null) {
            return device_codec;
        }
        return null;
    }

    public String getASRLanguage(){
        if (dictation_language != null) {
            return dictation_language;
        }
        return language;
    }

    public String getTTSLanguage(){
        if (tts_language != null) {
            return tts_language;
        }
        return language;
    }

    public String getWebAPICodecName(String name){
        if (name != null) {
            return AudioCodec.fromString(name).getWebapiString().toLowerCase();
        }

        return null;
    }

    public Integer getEndOfSpeechVoicedFrames() {
        return UTTERANCE_DETECTION_SILENCE_DURATIONS.get(utterance_detection_silence_duration) / 10; // Convert time to frames history
    }

    public boolean getResultDelivery() {
        if (dictionary != null && dictionary.containsKey("result_delivery")) {
            return ((String)dictionary.get("result_delivery")).equalsIgnoreCase("progressive");
        }
        return false;
    }

    public String getUserId(HashType hashType, String salt) throws IOException {
        if(StringUtils.isEmpty(user_id)) {
            return getDeviceId(hashType, salt);
        }
        return StringObfuscator.getObfuscatedString(hashType, salt, user_id, true);
    }

    public String getDeviceId(HashType hashType, String salt) throws IOException {
        return StringObfuscator.getObfuscatedString(hashType, salt, device_id, StringUtils.isEmpty(user_id));
    }

    public String getSubscriberId(HashType hashType, String salt) throws IOException {
        return StringObfuscator.getObfuscatedString(hashType, salt, subscriber_id, false);
    }

    public String getDeviceModel() {
        if(!StringUtils.isEmpty(deviceModel)) {
            return deviceModel;
        }
        return getPhoneModel();
    }

    public String getPhoneModel() {
        if(!StringUtils.isEmpty(phone_model)) {
            return phone_model;
        }
        return phoneModel;
    }
}

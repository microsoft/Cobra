package com.nuance.plugin.ncs7.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NcsResponse {
    @JsonProperty("message")
    public String message;

    @JsonProperty("session_id")
    public String session_id;

    @JsonProperty("code")
    public Integer code;

    @JsonProperty("codec")
    public String codec;

    @JsonProperty("codec_id")
    public Integer codec_id;

    @JsonProperty("audio_id")
    public Integer audio_id;

    @JsonProperty("reason")
    public String reason;

    @JsonProperty("TTSStatus")
    public String TTSStatus;

    @JsonProperty("transaction_id")
    public Integer transaction_id;

    @JsonProperty("result_type")
    public String result_type;

    @JsonProperty("NMAS_PRFX_SESSION_ID")
    public String NMAS_PRFX_SESSION_ID;

    @JsonProperty("NMAS_PRFX_TRANSACTION_ID")
    public String NMAS_PRFX_TRANSACTION_ID;

    @JsonProperty("transcriptions")
    public List<String> transcriptions;

    @JsonProperty("confidences")
    public List<Integer> confidences;

    @JsonProperty("final_response")
    public Integer final_response;

    @JsonProperty("status_code")
    public Integer status_code;

    @JsonProperty("result_format")
    public String result_format;

    @JsonProperty("prompt")
    public String prompt;

    @JsonProperty("cadence_regulatable_result")
    public String cadence_regulatable_result;

    @JsonProperty("nlu_interpretation_results")
    public Map<String, Object> nlu_interpretation_results;

    @JsonProperty("words")
    public List<List<Object>> words;

    @JsonProperty("audio_transfer_info")
    public Map<String, Object> audio_transfer_info;

    @JsonProperty("parameter")
    public String parameter;

    public static NcsResponse fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, NcsResponse.class);
    }

    public static NcsResponse fromMap(Map<String, Object> json) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(json, NcsResponse.class);
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writer().writeValueAsString(this);
    }
}

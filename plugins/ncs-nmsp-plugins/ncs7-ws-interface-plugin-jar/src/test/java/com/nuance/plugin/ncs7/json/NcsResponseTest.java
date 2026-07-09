package com.nuance.plugin.ncs7.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class NcsResponseTest {

    @Test
    public void fromJson() throws JsonProcessingException {
        NcsResponse resp = NcsResponse.fromJson("{\n" +
                "  \"message\" : \"query_response\",\n" +
                "  \"result_type\" : \"NMDP_ASR_CMD\",\n" +
                "  \"NMAS_PRFX_SESSION_ID\" : \"6cfc863a-8f4e-4cf6-a0f5-dca1b2101f48\",\n" +
                "  \"NMAS_PRFX_TRANSACTION_ID\" : \"4\",\n" +
                "  \"transcriptions\" : [ \"Monday Tuesday Wednesday\" ],\n" +
                "  \"confidences\" : [ 946 ],\n" +
                "  \"final_response\" : 1,\n" +
                "  \"status_code\" : 0,\n" +
                "  \"result_format\" : \"rec_text_results\",\n" +
                "  \"cadence_regulatable_result\" : \"completeRecognition\",\n" +
                "  \"words\" : [ [ {\n" +
                "    \"confidence\" : 974.0,\n" +
                "    \"word\" : \"Monday\"\n" +
                "  }, {\n" +
                "    \"confidence\" : 978.0,\n" +
                "    \"word\" : \"Tuesday\"\n" +
                "  }, {\n" +
                "    \"confidence\" : 983.0,\n" +
                "    \"word\" : \"Wednesday\"\n" +
                "  } ] ]\n" +
                "}");

        assertEquals("query_response", resp.message);
    }

    @Test
    public void toJson() throws JsonProcessingException {
        NcsResponse resp = new NcsResponse();
        resp.result_format = "test";
        resp.status_code = 1;
        resp.final_response = 1;

        String respStr = resp.toJson();

        assertTrue(respStr.contains("test"));
    }
}
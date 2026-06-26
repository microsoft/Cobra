package com.nuance.cobra.utils.nle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.WebApiResponseTest;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NLEConverterTest {

    private static final String webapiInterpret = "{\n" +
            "  \"type\": \"result\",\n" +
            "  \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
            "  \"command\": {\n" +
            "    \"id\": \"27\",\n" +
            "    \"name\": \"interpretation\"\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"final\": true,\n" +
            "    \"status\": \"success\",\n" +
            "    \"interpretations\": [\n" +
            "      {\n" +
            "        \"concepts\": [\n" +
            "          {\n" +
            "            \"concept\": \"FLOWER\",\n" +
            "            \"value\": \"Tulips\",\n" +
            "            \"literal\": \"tulips\",\n" +
            "            \"begin\": 10,\n" +
            "            \"end\": 16\n" +
            "          },\n" +
            "          {\n" +
            "            \"concept\": \"FLOWER\",\n" +
            "            \"value\": \"ROSES\",\n" +
            "            \"literal\": \"roses\",\n" +
            "            \"begin\": 21,\n" +
            "            \"end\": 26\n" +
            "          }\n" +
            "        ],\n" +
            "        \"confidence\": 100,\n" +
            "        \"literal\": \"Can I buy tulips and roses?\",\n" +
            "        \"intent\": \"BUY_FLOWERS\",\n" +
            "        \"origin\": \"SSM\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"bestInterpretation\": {\n" +
            "      \"concepts\": [\n" +
            "        {\n" +
            "          \"concept\": \"FLOWER\",\n" +
            "          \"value\": \"Tulips\",\n" +
            "          \"literal\": \"tulips\",\n" +
            "          \"begin\": 10,\n" +
            "          \"end\": 16\n" +
            "        },\n" +
            "        {\n" +
            "          \"concept\": \"FLOWER\",\n" +
            "          \"value\": \"ROSES\",\n" +
            "          \"literal\": \"roses\",\n" +
            "          \"begin\": 21,\n" +
            "          \"end\": 26\n" +
            "        }\n" +
            "      ],\n" +
            "      \"confidence\": 100,\n" +
            "      \"literal\": \"Can I buy tulips and roses?\",\n" +
            "      \"intent\": \"BUY_FLOWERS\",\n" +
            "      \"origin\": \"SSM\"\n" +
            "    },\n" +
            "    \"statistics\": {\n" +
            "      \"startTime\": \"2017/11/17 21:33:09.540\",\n" +
            "      \"endTime\": \"2017/11/17 21:33:10.062\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String webapiInterpretWithoutValue = "{\n" +
            "  \"type\": \"result\",\n" +
            "  \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
            "  \"command\": {\n" +
            "    \"id\": \"27\",\n" +
            "    \"name\": \"interpretation\"\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"final\": true,\n" +
            "    \"status\": \"success\",\n" +
            "    \"interpretations\": [\n" +
            "      {\n" +
            "        \"concepts\": [\n" +
            "          {\n" +
            "            \"concept\": \"FLOWER\",\n" +
            "            \"value\": \"Tulips\",\n" +
            "            \"literal\": \"tulips\",\n" +
            "            \"begin\": 10,\n" +
            "            \"end\": 16\n" +
            "          },\n" +
            "          {\n" +
            "            \"concept\": \"FLOWER\",\n" +
            "            \"value\": \"ROSES\",\n" +
            "            \"literal\": \"roses\",\n" +
            "            \"begin\": 21,\n" +
            "            \"end\": 26\n" +
            "          }\n" +
            "        ],\n" +
            "        \"confidence\": 100,\n" +
            "        \"literal\": \"Can I buy tulips and roses?\",\n" +
            "        \"intent\": \"BUY_FLOWERS\",\n" +
            "        \"origin\": \"SSM\"\n" +
            "      },\n" +
            "      {\n" + // New interpretation object without 'value' attribute
            "        \"concepts\": [\n" +
            "          {\n" +
            "            \"concept\": \"COLOR\",\n" +
            "            \"literal\": \"red\",\n" +
            "            \"begin\": 30,\n" +
            "            \"end\": 33\n" +
            "          }\n" +
            "        ],\n" +
            "        \"confidence\": 90,\n" +
            "        \"literal\": \"I love red color\",\n" +
            "        \"intent\": \"PREFER_COLOR\",\n" +
            "        \"origin\": \"SSM\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"bestInterpretation\": {\n" +
            "      \"concepts\": [\n" +
            "        {\n" +
            "          \"concept\": \"FLOWER\",\n" +
            "          \"value\": \"Tulips\",\n" +
            "          \"literal\": \"tulips\",\n" +
            "          \"begin\": 10,\n" +
            "          \"end\": 16\n" +
            "        },\n" +
            "        {\n" +
            "          \"concept\": \"FLOWER\",\n" +
            "          \"value\": \"ROSES\",\n" +
            "          \"literal\": \"roses\",\n" +
            "          \"begin\": 21,\n" +
            "          \"end\": 26\n" +
            "        }\n" +
            "      ],\n" +
            "      \"confidence\": 100,\n" +
            "      \"literal\": \"Can I buy tulips and roses?\",\n" +
            "      \"intent\": \"BUY_FLOWERS\",\n" +
            "      \"origin\": \"SSM\"\n" +
            "    },\n" +
            "    \"statistics\": {\n" +
            "      \"startTime\": \"2017/11/17 21:33:09.540\",\n" +
            "      \"endTime\": \"2017/11/17 21:33:10.062\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String webapiInterpret_boolean = "{\n" +
            "    \"type\": \"result\",\n" +
            "    \"sessionId\": \"43b18829-7424-4965-a26b-bbfc51d0d354\",\n" +
            "    \"command\":\n" +
            "    {\n" +
            "        \"name\": \"interpretation\",\n" +
            "        \"id\": \"3\"\n" +
            "    },\n" +
            "    \"result\":\n" +
            "    {\n" +
            "        \"final\": true,\n" +
            "        \"status\": \"success\",\n" +
            "        \"interpretations\":\n" +
            "        [\n" +
            "            {\n" +
            "                \"confidence\": 99,\n" +
            "                \"literal\": \"Yes\",\n" +
            "                \"intent\": \"DO_STUFF\",\n" +
            "                \"origin\": \"GRAMMAR\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"concepts\":\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"concepts\":\n" +
            "                        [\n" +
            "                            {\n" +
            "                                \"concept\": \"nuance_BOOLEAN\",\n" +
            "                                \"value\": \"true\",\n" +
            "                                \"literal\": \"Yes\",\n" +
            "                                \"begin\": 0,\n" +
            "                                \"end\": 3\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"concept\": \"YES_NO\",\n" +
            "                        \"literal\": \"Yes\",\n" +
            "                        \"begin\": 0,\n" +
            "                        \"end\": 3\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"confidence\": 99,\n" +
            "                \"literal\": \"Yes\",\n" +
            "                \"intent\": \"NO_INTENT\",\n" +
            "                \"origin\": \"GRAMMAR\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"confidence\": 0,\n" +
            "                \"literal\": \"Yes\",\n" +
            "                \"intent\": \"GREETINGS\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"bestInterpretation\":\n" +
            "        {\n" +
            "            \"confidence\": 99,\n" +
            "            \"literal\": \"Yes\",\n" +
            "            \"intent\": \"DO_STUFF\",\n" +
            "            \"origin\": \"GRAMMAR\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    @Test
    public void convertWebapiInterpretationToNle_basic() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(webapiInterpret), false).getJSONArray("interpretations").getJSONObject(0);
        assertTrue(convertedToNle.has("intention"));
        assertTrue(convertedToNle.has("mentions"));
    }

    @Test
    public void convertWebapiInterpretationToNle_basic_without_value() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(webapiInterpretWithoutValue), false).getJSONArray("interpretations").getJSONObject(0);
        assertTrue(convertedToNle.has("intention"));
        assertTrue(convertedToNle.has("mentions"));
    }

    @Test
    public void convertWebapiInterpretationToNle_noverbose_noasr() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(webapiInterpret_boolean), false).getJSONArray("interpretations").getJSONObject(1);
        assertEquals("{\"confidence\":99,\"literal\":\"Yes\",\"origin\":\"GRAMMAR\",\"mentions\":[{\"end\":3,\"begin\":0,\"value\":\"{\\\"nuance_BOOLEAN\\\":\\\"true\\\"}\",\"concept\":\"YES_NO\",\"literal\":\"Yes\"}],\"intention\":\"NO_INTENT\"}", convertedToNle.toString());
    }

    @Test
    public void convertWebapiInterpretationToNle_noverbose_withasr() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(webapiInterpret_boolean), true).getJSONArray("interpretations").getJSONObject(1);
        assertEquals("{\"confidence\":99,\"literal\":\"Yes\",\"origin\":\"GRAMMAR\",\"formattedLiteral\":\"Yes\",\"mentions\":[{\"formattedEnd\":3,\"concept\":\"YES_NO\",\"formattedLiteral\":\"Yes\",\"end\":3,\"begin\":0,\"value\":\"{\\\"nuance_BOOLEAN\\\":\\\"true\\\"}\",\"formattedBegin\":0,\"literal\":\"Yes\"}],\"intention\":\"NO_INTENT\"}", convertedToNle.toString());
    }

    @Test
    public void testConvertWebapiInterpretationToNle_verbose_noasr() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(WebApiResponseTest.TEST_RESPONSE_WITH_VERBOSE), true);
        assertEquals("{\"interpretations\":[{\"confidence\":99,\"literal\":\"What is my account balance?\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"What is my account balance?\",\"mentions\":[{\"formattedEnd\":26,\"concept\":\"CALL_PURPOSE\",\"formattedLiteral\":\"account balance\",\"end\":26,\"value\":\"account balance\",\"begin\":11,\"formattedBegin\":11,\"literal\":\"account balance\"}],\"intention\":\"CHECK_BALANCE\"},{\"confidence\":0,\"literal\":\"What is my account balance?\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"What is my account balance?\",\"mentions\":[{\"formattedEnd\":26,\"concept\":\"CALL_PURPOSE\",\"formattedLiteral\":\"account balance\",\"end\":26,\"value\":\"account balance\",\"begin\":11,\"formattedBegin\":11,\"literal\":\"account balance\"}],\"intention\":\"PAY_BILL\"}]}", convertedToNle.toString());
    }

    @Test
    public void testConvertWebapiInterpretationToNle_verbose_withasr() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(WebApiResponseTest.TEST_RESPONSE_WITH_VERBOSE), true);
        assertEquals("{\"interpretations\":[{\"confidence\":99,\"literal\":\"What is my account balance?\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"What is my account balance?\",\"mentions\":[{\"formattedEnd\":26,\"concept\":\"CALL_PURPOSE\",\"formattedLiteral\":\"account balance\",\"end\":26,\"value\":\"account balance\",\"begin\":11,\"formattedBegin\":11,\"literal\":\"account balance\"}],\"intention\":\"CHECK_BALANCE\"},{\"confidence\":0,\"literal\":\"What is my account balance?\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"What is my account balance?\",\"mentions\":[{\"formattedEnd\":26,\"concept\":\"CALL_PURPOSE\",\"formattedLiteral\":\"account balance\",\"end\":26,\"value\":\"account balance\",\"begin\":11,\"formattedBegin\":11,\"literal\":\"account balance\"}],\"intention\":\"PAY_BILL\"}]}", convertedToNle.toString());
    }

    @Test
    public void testConvertWebapiInterpretationToNle_verbose_withasr_complex() throws JsonProcessingException {
        JSONObject convertedToNle = NLEConverter.convertWebapiInterpretationToNle(WebApiResponse.fromJson(WebApiResponseTest.TEST_RESPONSE_WITH_VERBOSE_COMPLEX), true);
        assertEquals("{\"interpretations\":[{\"confidence\":96,\"literal\":\"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\"mentions\":[{\"formattedEnd\":30,\"concept\":\"ACCOUNT\",\"formattedLiteral\":\"checking\",\"end\":30,\"begin\":22,\"value\":\"Checking\",\"formattedBegin\":22,\"literal\":\"checking\"},{\"formattedEnd\":60,\"concept\":\"ACCOUNT\",\"formattedLiteral\":\"savings account\",\"end\":60,\"begin\":45,\"formattedBegin\":45,\"literal\":\"savings account\"},{\"formattedEnd\":75,\"concept\":\"nuance_CALENDARX\",\"formattedLiteral\":\"tomorrow at 1 \",\"end\":75,\"begin\":61,\"value\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":1}}}},\"formattedBegin\":61,\"literal\":\"tomorrow at 1 \"},{\"formattedEnd\":20,\"concept\":\"nuance_AMOUNT\",\"formattedLiteral\":\"one hundred\",\"end\":20,\"begin\":9,\"formattedBegin\":9,\"literal\":\"one hundred\"}],\"intention\":\"transfer-funds\"},{\"confidence\":1,\"literal\":\"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\"intention\":\"vague-account_checking\"},{\"confidence\":0,\"literal\":\"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\"origin\":\"STATISTICAL\",\"formattedLiteral\":\"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\"intention\":\"request-contact\"}]}", convertedToNle.toString());
    }
}
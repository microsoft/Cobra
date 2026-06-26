package com.nuance.cobra.utils.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebApiResponseTest
{
    @Test
    public void isResult()
    {
        WebApiResponse testResponse = new WebApiResponse();
        testResponse.result = new HashedMap();
        testResponse.result.put("final", true);

        assertTrue(testResponse.isFinal());
    }

    @Test
    public void isForCommand() throws JsonProcessingException {
        WebApiResponse testResponse = WebApiResponse.fromJson("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechRecognition\",\n" +
                "        \"id\": \"2\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"noInput\",\n" +
                "        \"noiseLevel\": {\n" +
                "            \"mean\": 67.82,\n" +
                "            \"standardDeviation\": 24.48\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertTrue(testResponse.isForCommand("speechRecognition"));
        assertFalse(testResponse.isForCommand("speechSynthesis"));
        assertTrue(testResponse.isFinal());
    }

    @Test
    public void testGetResult() throws JsonProcessingException {
        WebApiResponse webApiResponse= WebApiResponse.fromJson(TEST_RESPONSE_WITH_VERBOSE);
        JSONObject result = webApiResponse.getResult();
        assertNotNull(result);
    }

    @Test
    public void testHasVerboseResultTrue() throws JsonProcessingException {
        WebApiResponse webApiResponse= WebApiResponse.fromJson(TEST_RESPONSE_WITH_VERBOSE);
        boolean hasVerboseResult = webApiResponse.hasVerboseResult();
        assertTrue(hasVerboseResult);
    }

    @Test
    public void testHasVerboseResultFalse() throws JsonProcessingException {
        WebApiResponse webApiResponse= WebApiResponse.fromJson(TEST_RESPONSE_NO_VERBOSE);
        boolean hasVerboseResult = webApiResponse.hasVerboseResult();
        assertFalse(hasVerboseResult);
    }

    @Test
    public void testGetVerboseResult() throws JsonProcessingException {
        WebApiResponse webApiResponse= WebApiResponse.fromJson(TEST_RESPONSE_WITH_VERBOSE);
        JSONObject verboseResult = webApiResponse.getVerboseResult();
        assertNotNull(verboseResult);
    }

    @Test
    public void testGetInterpretations() throws JsonProcessingException {
        WebApiResponse webApiResponse= WebApiResponse.fromJson(TEST_RESPONSE_WITH_VERBOSE);
        JSONArray interpretations = webApiResponse.getInterpretations();
        assertNotNull(interpretations);
        assertFalse(interpretations.isEmpty());
    }

    private static final String TEST_RESPONSE_NO_VERBOSE = "{\n" +
            "    \"type\": \"result\",\n" +
            "    \"sessionId\": \"b2770b34-571d-4428-1337-f34e9bb1cef3\",\n" +
            "    \"command\":\n" +
            "    {\n" +
            "        \"name\": \"interpretation\",\n" +
            "        \"id\": \"2\"\n" +
            "    },\n" +
            "    \"result\":\n" +
            "    {\n" +
            "        \"final\": true,\n" +
            "        \"status\": \"success\",\n" +
            "        \"statistics\":\n" +
            "        {\n" +
            "            \"hostInfo\":\n" +
            "            {\n" +
            "                \"region\": \"ma\",\n" +
            "                \"environment\": \"Prod\",\n" +
            "                \"version\": \"2.33.11-SNAPSHOT\"\n" +
            "            },\n" +
            "            \"startTime\": \"2022/08/25 00:01:47.082\",\n" +
            "            \"endTime\": \"2022/08/25 00:01:47.158\",\n" +
            "            \"runTimeMs\": 76,\n" +
            "            \"modelVersion\": \"urn:nuance-mix:tag:model/Tier5Tests/mix.nlu?=language=eng-USA&nlptype\",\n" +
            "            \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
            "            \"engineRequests\":\n" +
            "            {\n" +
            "                \"engine\": \"nlu-grpc\",\n" +
            "                \"model\": \"urn:nuance-mix:tag:model/Tier5Tests/mix.nlu?=language=eng-USA&nlptype\",\n" +
            "                \"language\": \"eng-USA\"\n" +
            "            },\n" +
            "            \"engineResponses\":\n" +
            "            {\n" +
            "                \"grpcHeaders\":\n" +
            "                {\n" +
            "                    \"x-request-id\": \"b197cd56-1037-9dbf-1337-5cb0ff7b10c7\",\n" +
            "                    \"date\": \"Thu, 25 Aug 2022 00:01:46 GMT\",\n" +
            "                    \"server\": \"envoy\",\n" +
            "                    \"x-envoy-upstream-service-time\": \"4\",\n" +
            "                    \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
            "                    \"uber-trace-id\": \"e30744fc5f21237680c3d9f63fb779be:5e2e133731693383:fd1adab9c1d9cf8a:1\",\n" +
            "                    \"x-client-request-id\": \"03e16b3b-1ff7-4445-1337-dfb455355be8\",\n" +
            "                    \"content-type\": \"application/grpc\",\n" +
            "                    \"accept-encoding\": \"identity,gzip\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        \"interpretations\":\n" +
            "        [\n" +
            "            {\n" +
            "                \"concepts\":\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"concept\": \"CALL_PURPOSE\",\n" +
            "                        \"value\": \"account balance\",\n" +
            "                        \"literal\": \"account balance\",\n" +
            "                        \"begin\": 11,\n" +
            "                        \"end\": 26\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"confidence\": 99,\n" +
            "                \"literal\": \"What is my account balance?\",\n" +
            "                \"intent\": \"CHECK_BALANCE\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"concepts\":\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"concept\": \"CALL_PURPOSE\",\n" +
            "                        \"value\": \"account balance\",\n" +
            "                        \"literal\": \"account balance\",\n" +
            "                        \"begin\": 11,\n" +
            "                        \"end\": 26\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"confidence\": 0,\n" +
            "                \"literal\": \"What is my account balance?\",\n" +
            "                \"intent\": \"PAY_BILL\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"bestInterpretation\":\n" +
            "        {\n" +
            "            \"concepts\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"concept\": \"CALL_PURPOSE\",\n" +
            "                    \"value\": \"account balance\",\n" +
            "                    \"literal\": \"account balance\",\n" +
            "                    \"begin\": 11,\n" +
            "                    \"end\": 26\n" +
            "                }\n" +
            "            ],\n" +
            "            \"confidence\": 99,\n" +
            "            \"literal\": \"What is my account balance?\",\n" +
            "            \"intent\": \"CHECK_BALANCE\",\n" +
            "            \"origin\": \"STATISTICAL\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public static final String TEST_RESPONSE_WITH_VERBOSE = "{\n" +
            "    \"type\": \"result\",\n" +
            "    \"sessionId\": \"b2770b34-571d-4428-1337-f34e9bb1cef3\",\n" +
            "    \"command\":\n" +
            "    {\n" +
            "        \"name\": \"interpretation\",\n" +
            "        \"id\": \"2\"\n" +
            "    },\n" +
            "    \"result\":\n" +
            "    {\n" +
            "        \"final\": true,\n" +
            "        \"status\": \"success\",\n" +
            "        \"statistics\":\n" +
            "        {\n" +
            "            \"hostInfo\":\n" +
            "            {\n" +
            "                \"region\": \"ma\",\n" +
            "                \"environment\": \"Prod\",\n" +
            "                \"version\": \"2.33.11-SNAPSHOT\"\n" +
            "            },\n" +
            "            \"startTime\": \"2022/08/25 00:01:47.082\",\n" +
            "            \"endTime\": \"2022/08/25 00:01:47.158\",\n" +
            "            \"runTimeMs\": 76,\n" +
            "            \"modelVersion\": \"urn:nuance-mix:tag:model/Tier5Tests/mix.nlu?=language=eng-USA&nlptype\",\n" +
            "            \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
            "            \"engineRequests\":\n" +
            "            {\n" +
            "                \"engine\": \"nlu-grpc\",\n" +
            "                \"model\": \"urn:nuance-mix:tag:model/Tier5Tests/mix.nlu?=language=eng-USA&nlptype\",\n" +
            "                \"language\": \"eng-USA\"\n" +
            "            },\n" +
            "            \"engineResponses\":\n" +
            "            {\n" +
            "                \"grpcHeaders\":\n" +
            "                {\n" +
            "                    \"x-request-id\": \"b197cd56-1037-9dbf-1337-5cb0ff7b10c7\",\n" +
            "                    \"date\": \"Thu, 25 Aug 2022 00:01:46 GMT\",\n" +
            "                    \"server\": \"envoy\",\n" +
            "                    \"x-envoy-upstream-service-time\": \"4\",\n" +
            "                    \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
            "                    \"uber-trace-id\": \"e30744fc5f21237680c3d9f63fb779be:5e2e133731693383:fd1adab9c1d9cf8a:1\",\n" +
            "                    \"x-client-request-id\": \"03e16b3b-1ff7-4445-1337-dfb455355be8\",\n" +
            "                    \"content-type\": \"application/grpc\",\n" +
            "                    \"accept-encoding\": \"identity,gzip\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        \"interpretations\":\n" +
            "        [\n" +
            "            {\n" +
            "                \"concepts\":\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"concept\": \"CALL_PURPOSE\",\n" +
            "                        \"value\": \"account balance\",\n" +
            "                        \"literal\": \"account balance\",\n" +
            "                        \"begin\": 11,\n" +
            "                        \"end\": 26\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"confidence\": 99,\n" +
            "                \"literal\": \"What is my account balance?\",\n" +
            "                \"intent\": \"CHECK_BALANCE\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"concepts\":\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"concept\": \"CALL_PURPOSE\",\n" +
            "                        \"value\": \"account balance\",\n" +
            "                        \"literal\": \"account balance\",\n" +
            "                        \"begin\": 11,\n" +
            "                        \"end\": 26\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"confidence\": 0,\n" +
            "                \"literal\": \"What is my account balance?\",\n" +
            "                \"intent\": \"PAY_BILL\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"bestInterpretation\":\n" +
            "        {\n" +
            "            \"concepts\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"concept\": \"CALL_PURPOSE\",\n" +
            "                    \"value\": \"account balance\",\n" +
            "                    \"literal\": \"account balance\",\n" +
            "                    \"begin\": 11,\n" +
            "                    \"end\": 26\n" +
            "                }\n" +
            "            ],\n" +
            "            \"confidence\": 99,\n" +
            "            \"literal\": \"What is my account balance?\",\n" +
            "            \"intent\": \"CHECK_BALANCE\",\n" +
            "            \"origin\": \"STATISTICAL\"\n" +
            "        },\n" +
            "        \"verboseResult\":\n" +
            "        {\n" +
            "            \"literal\": \"What is my account balance?\",\n" +
            "            \"interpretations\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"singleIntentInterpretation\":\n" +
            "                    {\n" +
            "                        \"intent\": \"CHECK_BALANCE\",\n" +
            "                        \"confidence\": 0.99999726,\n" +
            "                        \"origin\": \"STATISTICAL\",\n" +
            "                        \"entities\":\n" +
            "                        {\n" +
            "                            \"CALL_PURPOSE\":\n" +
            "                            {\n" +
            "                                \"entities\":\n" +
            "                                [\n" +
            "                                    {\n" +
            "                                        \"textRange\":\n" +
            "                                        {\n" +
            "                                            \"startIndex\": 11.0,\n" +
            "                                            \"endIndex\": 26.0\n" +
            "                                        },\n" +
            "                                        \"confidence\": 0.40816373,\n" +
            "                                        \"origin\": \"STATISTICAL\",\n" +
            "                                        \"stringValue\": \"account balance\",\n" +
            "                                        \"literal\": \"account balance\",\n" +
            "                                        \"sensitive\": false,\n" +
            "                                        \"formattedLiteral\": \"account balance\",\n" +
            "                                        \"formattedTextRange\":\n" +
            "                                        {\n" +
            "                                            \"startIndex\": 11.0,\n" +
            "                                            \"endIndex\": 26.0\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"singleIntentInterpretation\":\n" +
            "                    {\n" +
            "                        \"intent\": \"PAY_BILL\",\n" +
            "                        \"confidence\": 0.0000019,\n" +
            "                        \"origin\": \"STATISTICAL\",\n" +
            "                        \"entities\":\n" +
            "                        {\n" +
            "                            \"CALL_PURPOSE\":\n" +
            "                            {\n" +
            "                                \"entities\":\n" +
            "                                [\n" +
            "                                    {\n" +
            "                                        \"textRange\":\n" +
            "                                        {\n" +
            "                                            \"startIndex\": 11.0,\n" +
            "                                            \"endIndex\": 26.0\n" +
            "                                        },\n" +
            "                                        \"confidence\": 0.40816373,\n" +
            "                                        \"origin\": \"STATISTICAL\",\n" +
            "                                        \"stringValue\": \"account balance\",\n" +
            "                                        \"literal\": \"account balance\",\n" +
            "                                        \"sensitive\": false,\n" +
            "                                        \"formattedLiteral\": \"account balance\",\n" +
            "                                        \"formattedTextRange\":\n" +
            "                                        {\n" +
            "                                            \"startIndex\": 11.0,\n" +
            "                                            \"endIndex\": 26.0\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            ],\n" +
            "            \"sensitive\": false,\n" +
            "            \"formattedLiteral\": \"What is my account balance?\",\n" +
            "            \"metadata\":\n" +
            "            {\n" +
            "                \"x-nuance-supplemental-metrics-result\":\n" +
            "                {\n" +
            "                    \"modelLanguage\": \"eng-USA\",\n" +
            "                    \"versions\":\n" +
            "                    {\n" +
            "                        \"mrec\": \"1.42.100.19403\",\n" +
            "                        \"qnlu\": \"2.22.9\",\n" +
            "                        \"textProc\": \"15.07.100.02109\"\n" +
            "                    },\n" +
            "                    \"tokens\":\n" +
            "                    [\n" +
            "                        \"What\",\n" +
            "                        \"is\",\n" +
            "                        \"my\",\n" +
            "                        \"account\",\n" +
            "                        \"balance\",\n" +
            "                        \"?\"\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public static final String TEST_RESPONSE_WITH_VERBOSE_COMPLEX = "{\n" +
            "    \"command\":\n" +
            "    {\n" +
            "        \"id\": \"3\",\n" +
            "        \"name\": \"interpretation\"\n" +
            "    },\n" +
            "    \"result\":\n" +
            "    {\n" +
            "        \"bestInterpretation\":\n" +
            "        {\n" +
            "            \"concepts\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"begin\": 22,\n" +
            "                    \"concept\": \"ACCOUNT\",\n" +
            "                    \"end\": 30,\n" +
            "                    \"literal\": \"checking\",\n" +
            "                    \"value\": \"Checking\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"begin\": 45,\n" +
            "                    \"concept\": \"ACCOUNT\",\n" +
            "                    \"end\": 60,\n" +
            "                    \"literal\": \"savings account\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"begin\": 61,\n" +
            "                    \"concept\": \"nuance_CALENDARX\",\n" +
            "                    \"end\": 75,\n" +
            "                    \"literal\": \"tomorrow at 1 \",\n" +
            "                    \"value\":\n" +
            "                    {\n" +
            "                        \"nuance_CALENDAR\":\n" +
            "                        {\n" +
            "                            \"nuance_DATE\":\n" +
            "                            {\n" +
            "                                \"nuance_DATE_REL\":\n" +
            "                                {\n" +
            "                                    \"nuance_INCREMENT\": 1.0,\n" +
            "                                    \"nuance_STEP\": \"day\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            \"nuance_TIME\":\n" +
            "                            {\n" +
            "                                \"nuance_TIME_ABS\":\n" +
            "                                {\n" +
            "                                    \"nuance_HOUR\": 1.0\n" +
            "                                }\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"begin\": 9,\n" +
            "                    \"concept\": \"nuance_AMOUNT\",\n" +
            "                    \"end\": 20,\n" +
            "                    \"literal\": \"one hundred\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"confidence\": 96,\n" +
            "            \"intent\": \"transfer-funds\",\n" +
            "            \"literal\": \"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\n" +
            "            \"origin\": \"STATISTICAL\"\n" +
            "        },\n" +
            "        \"final\": true,\n" +
            "        \"interpretations\":\n" +
            "        [\n" +
            "            {\n" +
            "                \"concepts\":\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"begin\": 22,\n" +
            "                        \"concept\": \"ACCOUNT\",\n" +
            "                        \"end\": 30,\n" +
            "                        \"literal\": \"checking\",\n" +
            "                        \"value\": \"Checking\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"begin\": 45,\n" +
            "                        \"concept\": \"ACCOUNT\",\n" +
            "                        \"end\": 60,\n" +
            "                        \"literal\": \"savings account\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"begin\": 61,\n" +
            "                        \"concept\": \"nuance_CALENDARX\",\n" +
            "                        \"end\": 75,\n" +
            "                        \"literal\": \"tomorrow at 1 \",\n" +
            "                        \"value\":\n" +
            "                        {\n" +
            "                            \"nuance_CALENDAR\":\n" +
            "                            {\n" +
            "                                \"nuance_DATE\":\n" +
            "                                {\n" +
            "                                    \"nuance_DATE_REL\":\n" +
            "                                    {\n" +
            "                                        \"nuance_INCREMENT\": 1.0,\n" +
            "                                        \"nuance_STEP\": \"day\"\n" +
            "                                    }\n" +
            "                                },\n" +
            "                                \"nuance_TIME\":\n" +
            "                                {\n" +
            "                                    \"nuance_TIME_ABS\":\n" +
            "                                    {\n" +
            "                                        \"nuance_HOUR\": 1.0\n" +
            "                                    }\n" +
            "                                }\n" +
            "                            }\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"begin\": 9,\n" +
            "                        \"concept\": \"nuance_AMOUNT\",\n" +
            "                        \"end\": 20,\n" +
            "                        \"literal\": \"one hundred\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"confidence\": 96,\n" +
            "                \"intent\": \"transfer-funds\",\n" +
            "                \"literal\": \"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"confidence\": 1,\n" +
            "                \"intent\": \"vague-account_checking\",\n" +
            "                \"literal\": \"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"confidence\": 0,\n" +
            "                \"intent\": \"request-contact\",\n" +
            "                \"literal\": \"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\n" +
            "                \"origin\": \"STATISTICAL\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"statistics\":\n" +
            "        {\n" +
            "            \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
            "            \"endTime\": \"2022/08/29 15:14:31.753\",\n" +
            "            \"engineRequests\":\n" +
            "            {\n" +
            "                \"engine\": \"nlu-grpc\",\n" +
            "                \"language\": \"en-US\",\n" +
            "                \"model\": \"urn:nuance-mix:tag:model/test_dtv/mix.nlu?=language=eng-USA&nlptype\"\n" +
            "            },\n" +
            "            \"engineResponses\":\n" +
            "            {\n" +
            "                \"grpcHeaders\":\n" +
            "                {\n" +
            "                    \"accept-encoding\": \"identity,gzip\",\n" +
            "                    \"content-type\": \"application/grpc\",\n" +
            "                    \"date\": \"Mon, 29 Aug 2022 15:14:31 GMT\",\n" +
            "                    \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
            "                    \"server\": \"envoy\",\n" +
            "                    \"uber-trace-id\": \"1d0b7949732b11337207a52d91a9aaf8:11c7a3dc37b1c932:8af7c267f456838d:1\",\n" +
            "                    \"x-client-request-id\": \"43ec1012-9ab6-4c86-a8ef-fe7d0586d753\",\n" +
            "                    \"x-envoy-upstream-service-time\": \"4\",\n" +
            "                    \"x-request-id\": \"4a5b70b4-685c-1337-9597-a20162748df8\"\n" +
            "                }\n" +
            "            },\n" +
            "            \"hostInfo\":\n" +
            "            {\n" +
            "                \"environment\": \"Prod\",\n" +
            "                \"region\": \"ma\",\n" +
            "                \"version\": \"2.33.11-SNAPSHOT\"\n" +
            "            },\n" +
            "            \"modelVersion\": \"urn:nuance-mix:tag:model/test_dtv/mix.nlu?=language=eng-USA&nlptype\",\n" +
            "            \"runTimeMs\": 245,\n" +
            "            \"startTime\": \"2022/08/29 15:14:31.508\"\n" +
            "        },\n" +
            "        \"status\": \"success\",\n" +
            "        \"verboseResult\":\n" +
            "        {\n" +
            "            \"formattedLiteral\": \"Transfer $100 from my checking account to my savings account tomorrow at 1 PM\",\n" +
            "            \"interpretations\":\n" +
            "            [\n" +
            "                {\n" +
            "                    \"singleIntentInterpretation\":\n" +
            "                    {\n" +
            "                        \"confidence\": 0.9620449,\n" +
            "                        \"entities\":\n" +
            "                        {\n" +
            "                            \"ACCOUNT\":\n" +
            "                            {\n" +
            "                                \"entities\":\n" +
            "                                [\n" +
            "                                    {\n" +
            "                                        \"audioRange\":\n" +
            "                                        {\n" +
            "                                            \"endTimeMs\": 3680.0,\n" +
            "                                            \"startTimeMs\": 3200.0\n" +
            "                                        },\n" +
            "                                        \"confidence\": 0.7546738,\n" +
            "                                        \"formattedLiteral\": \"checking\",\n" +
            "                                        \"formattedTextRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 30.0,\n" +
            "                                            \"startIndex\": 22.0\n" +
            "                                        },\n" +
            "                                        \"literal\": \"checking\",\n" +
            "                                        \"origin\": \"STATISTICAL\",\n" +
            "                                        \"sensitive\": false,\n" +
            "                                        \"stringValue\": \"Checking\",\n" +
            "                                        \"textRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 45.0,\n" +
            "                                            \"startIndex\": 37.0\n" +
            "                                        }\n" +
            "                                    },\n" +
            "                                    {\n" +
            "                                        \"audioRange\":\n" +
            "                                        {\n" +
            "                                            \"endTimeMs\": 5840.0,\n" +
            "                                            \"startTimeMs\": 4800.0\n" +
            "                                        },\n" +
            "                                        \"confidence\": 0.4230887,\n" +
            "                                        \"formattedLiteral\": \"savings account\",\n" +
            "                                        \"formattedTextRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 60.0,\n" +
            "                                            \"startIndex\": 45.0\n" +
            "                                        },\n" +
            "                                        \"literal\": \"savings account\",\n" +
            "                                        \"origin\": \"STATISTICAL\",\n" +
            "                                        \"sensitive\": false,\n" +
            "                                        \"textRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 75.0,\n" +
            "                                            \"startIndex\": 60.0\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            \"nuance_AMOUNT\":\n" +
            "                            {\n" +
            "                                \"entities\":\n" +
            "                                [\n" +
            "                                    {\n" +
            "                                        \"audioRange\":\n" +
            "                                        {\n" +
            "                                            \"endTimeMs\": 2320.0,\n" +
            "                                            \"startTimeMs\": 1800.0\n" +
            "                                        },\n" +
            "                                        \"confidence\": 0.13398778,\n" +
            "                                        \"literal\": \"one hundred\",\n" +
            "                                        \"origin\": \"STATISTICAL\",\n" +
            "                                        \"sensitive\": false,\n" +
            "                                        \"textRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 20.0,\n" +
            "                                            \"startIndex\": 9.0\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            \"nuance_CALENDARX\":\n" +
            "                            {\n" +
            "                                \"entities\":\n" +
            "                                [\n" +
            "                                    {\n" +
            "                                        \"audioRange\":\n" +
            "                                        {\n" +
            "                                            \"endTimeMs\": 7240.0,\n" +
            "                                            \"startTimeMs\": 6120.0\n" +
            "                                        },\n" +
            "                                        \"confidence\": 0.37517348,\n" +
            "                                        \"formattedLiteral\": \"tomorrow at 1 \",\n" +
            "                                        \"formattedTextRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 75.0,\n" +
            "                                            \"startIndex\": 61.0\n" +
            "                                        },\n" +
            "                                        \"literal\": \"tomorrow at one\",\n" +
            "                                        \"origin\": \"STATISTICAL\",\n" +
            "                                        \"sensitive\": false,\n" +
            "                                        \"structValue\":\n" +
            "                                        {\n" +
            "                                            \"nuance_CALENDAR\":\n" +
            "                                            {\n" +
            "                                                \"nuance_DATE\":\n" +
            "                                                {\n" +
            "                                                    \"nuance_DATE_REL\":\n" +
            "                                                    {\n" +
            "                                                        \"nuance_INCREMENT\": 1.0,\n" +
            "                                                        \"nuance_STEP\": \"day\"\n" +
            "                                                    }\n" +
            "                                                },\n" +
            "                                                \"nuance_TIME\":\n" +
            "                                                {\n" +
            "                                                    \"nuance_TIME_ABS\":\n" +
            "                                                    {\n" +
            "                                                        \"nuance_HOUR\": 1.0\n" +
            "                                                    }\n" +
            "                                                }\n" +
            "                                            }\n" +
            "                                        },\n" +
            "                                        \"textRange\":\n" +
            "                                        {\n" +
            "                                            \"endIndex\": 91.0,\n" +
            "                                            \"startIndex\": 76.0\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"intent\": \"transfer-funds\",\n" +
            "                        \"origin\": \"STATISTICAL\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"singleIntentInterpretation\":\n" +
            "                    {\n" +
            "                        \"confidence\": 0.019773426,\n" +
            "                        \"intent\": \"vague-account_checking\",\n" +
            "                        \"origin\": \"STATISTICAL\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"singleIntentInterpretation\":\n" +
            "                    {\n" +
            "                        \"confidence\": 0.0015450721,\n" +
            "                        \"intent\": \"request-contact\",\n" +
            "                        \"origin\": \"STATISTICAL\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ],\n" +
            "            \"literal\": \"Transfer one hundred dollars from my checking account to my savings account tomorrow at one p.m.\",\n" +
            "            \"metadata\":\n" +
            "            {\n" +
            "                \"x-nuance-supplemental-metrics-result\":\n" +
            "                {\n" +
            "                    \"modelLanguage\": \"eng-USA\",\n" +
            "                    \"tokens\":\n" +
            "                    [\n" +
            "                        \"Transfer\",\n" +
            "                        \"one\",\n" +
            "                        \"hundred\",\n" +
            "                        \"dollars\",\n" +
            "                        \"from\",\n" +
            "                        \"my\",\n" +
            "                        \"checking\",\n" +
            "                        \"account\",\n" +
            "                        \"to\",\n" +
            "                        \"my\",\n" +
            "                        \"savings\",\n" +
            "                        \"account\",\n" +
            "                        \"tomorrow\",\n" +
            "                        \"at\",\n" +
            "                        \"one\",\n" +
            "                        \"p.m.\"\n" +
            "                    ],\n" +
            "                    \"versions\":\n" +
            "                    {\n" +
            "                        \"mrec\": \"1.42.100.19403\",\n" +
            "                        \"qnlu\": \"2.22.9\",\n" +
            "                        \"textProc\": \"15.07.100.02109\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"sensitive\": false\n" +
            "        }\n" +
            "    },\n" +
            "    \"sessionId\": \"43ec1012-9ab6-4c86-1337-fe7d0586d753\",\n" +
            "    \"type\": \"result\"\n" +
            "}";
}
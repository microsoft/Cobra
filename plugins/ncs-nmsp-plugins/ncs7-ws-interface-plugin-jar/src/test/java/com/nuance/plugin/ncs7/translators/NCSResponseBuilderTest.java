package com.nuance.plugin.ncs7.translators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class NCSResponseBuilderTest
{

    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new NCSResponseBuilder();
    }

    @Test
    public void nluFinalResponseBuilder() throws JsonProcessingException {
        Orchestrator mockOrchestrator = mock (Orchestrator.class);
        WebApiResponse webapiResponse = WebApiResponse.fromJson("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"id\":\"45\",\n" +
                "      \"name\":\"endSession\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"error\",\n" +
                "      \"errors\":[\n" +
                "         {\n" +
                "            \"code\":1402,\n" +
                "            \"message\":\"EndSession service failed.\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}");
        NcsResponse response = NCSResponseBuilder.nluFinalResponseBuilder(mockOrchestrator, webapiResponse, "test");
        assertEquals("query_error", response.message);
    }

    @Test
    public void asrFinalResponseBuilder() throws JsonProcessingException {
        Orchestrator mockOrchestrator = mock (Orchestrator.class);
        WebApiResponse webapiResponse = WebApiResponse.fromJson("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "    \"command\": {\n" +
                "        \"id\": \"2\",\n" +
                "        \"name\": \"speechRecognition\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "         \"final\": true,\n" +
                "         \"status\": \"error\",\n" +
                "         \"errors\": [\n" +
                "             {\n" +
                "                 \"code\": 1600,\n" +
                "                 \"message\": \"speechRecognition failed.\"\n" +
                "             },\n" +
                "             {\n" +
                "                 \"code\": 1601,\n" +
                "                 \"message\": \"speechRecognition command couldn't be executed.\"\n" +
                "             }\n" +
                "         ]\n" +
                "    }\n" +
                "}");
        NcsRequest ncsRequest = mock(NcsRequest.class);
        when(ncsRequest.getASRLanguage()).thenReturn("eng-USA");
        when(ncsRequest.getResultDelivery()).thenReturn(false);
        NcsResponse response = NCSResponseBuilder.asrFinalResponseBuilder(mockOrchestrator, webapiResponse, "test", ncsRequest);
        assertEquals("query_error", response.message);
    }

    @Test
    public void asrFinalResponseBuilder_noMatch() throws JsonProcessingException {
        Orchestrator mockOrchestrator = mock (Orchestrator.class);
        WebApiResponse webapiResponse = WebApiResponse.fromJson("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "    \"command\": {\n" +
                "        \"id\": \"2\",\n" +
                "        \"name\": \"speechRecognition\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "         \"final\": true,\n" +
                "         \"status\": \"noMatch\"\n" +
                "    }\n" +
                "}");
        NcsRequest ncsRequest = mock(NcsRequest.class);
        when(ncsRequest.getASRLanguage()).thenReturn("eng-USA");
        when(ncsRequest.getResultDelivery()).thenReturn(true);
        NcsResponse response = NCSResponseBuilder.asrFinalResponseBuilder(mockOrchestrator, webapiResponse, "test", ncsRequest);
        assertEquals("query_response", response.message);
    }

    @Test
    public void nluFinalResponseBuilderLargeResponse_newFormat() throws IOException {
        CombinedConfiguration config = new CombinedConfiguration();
        config.setProperty("ncs7-ws-plugin.useNewConceptFormat", true);
        Orchestrator mockOrchestrator = mock (Orchestrator.class);
        when(mockOrchestrator.getWebApiFeatures()).thenReturn(mock(IWebApiFeatures.class));
        when(mockOrchestrator.getWebApiFeatures().getConfigs(any())).thenReturn(config);

        WebApiResponse webapiResponse = WebApiResponse.fromJson("{\"type\":\"result\",\"sessionId\":\"b81e93e1-b2b7-4baf-86da-cbc6afc0742a\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"success\",\"interpretations\":[{\"concepts\":[{\"concept\":\"tv_air_date\",\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}},\"literal\":\"demain matin\",\"begin\":30,\"end\":42},{\"concept\":\"tv_air_date\",\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"begin\":51,\"end\":57},{\"concept\":\"tv_query\",\"value\":\"Desperado\",\"literal\":\"Desperado\",\"begin\":20,\"end\":29},{\"concept\":\"tv_station\",\"value\":\"tf1\",\"literal\":\"TF1\",\"begin\":47,\"end\":50},{\"concept\":\"tv_service\",\"value\":\"record\",\"literal\":\"enregistrer\",\"begin\":8,\"end\":19}],\"confidence\":94,\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"intent\":\"tv_search\",\"origin\":\"STATISTICAL\"}],\"bestInterpretation\":{\"concepts\":[{\"concept\":\"tv_air_date\",\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}},\"literal\":\"demain matin\",\"begin\":30,\"end\":42},{\"concept\":\"tv_air_date\",\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"begin\":51,\"end\":57},{\"concept\":\"tv_query\",\"value\":\"Desperado\",\"literal\":\"Desperado\",\"begin\":20,\"end\":29},{\"concept\":\"tv_station\",\"value\":\"tf1\",\"literal\":\"TF1\",\"begin\":47,\"end\":50},{\"concept\":\"tv_service\",\"value\":\"record\",\"literal\":\"enregistrer\",\"begin\":8,\"end\":19}],\"confidence\":94,\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"intent\":\"tv_search\",\"origin\":\"STATISTICAL\"},\"verboseResult\":{\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"interpretations\":[{\"singleIntentInterpretation\":{\"intent\":\"tv_search\",\"confidence\":0.9449202,\"origin\":\"STATISTICAL\",\"entities\":{\"tv_air_date\":{\"entities\":[{\"textRange\":{\"startIndex\":30.0,\"endIndex\":42.0},\"confidence\":0.82713646,\"origin\":\"STATISTICAL\",\"structValue\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":5.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\"}}}},\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":11.0,\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\"}}}}}}},\"literal\":\"demain matin\",\"sensitive\":false,\"formattedLiteral\":\"demain matin\",\"formattedTextRange\":{\"startIndex\":30.0,\"endIndex\":42.0}},{\"textRange\":{\"startIndex\":51.0,\"endIndex\":57.0},\"confidence\":0.82601124,\"origin\":\"STATISTICAL\",\"stringValue\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"sensitive\":false,\"formattedLiteral\":\"à 9h30\",\"formattedTextRange\":{\"startIndex\":51.0,\"endIndex\":57.0}}]},\"tv_query\":{\"entities\":[{\"textRange\":{\"startIndex\":20.0,\"endIndex\":29.0},\"confidence\":0.8014085,\"origin\":\"STATISTICAL\",\"stringValue\":\"Desperado\",\"literal\":\"Desperado\",\"sensitive\":false,\"formattedLiteral\":\"Desperado\",\"formattedTextRange\":{\"startIndex\":20.0,\"endIndex\":29.0}}]},\"tv_station\":{\"entities\":[{\"textRange\":{\"startIndex\":47.0,\"endIndex\":50.0},\"confidence\":0.5830483,\"origin\":\"STATISTICAL\",\"stringValue\":\"tf1\",\"literal\":\"TF1\",\"sensitive\":false,\"formattedLiteral\":\"TF1\",\"formattedTextRange\":{\"startIndex\":47.0,\"endIndex\":50.0}}]},\"tv_service\":{\"entities\":[{\"textRange\":{\"startIndex\":8.0,\"endIndex\":19.0},\"confidence\":0.60814315,\"origin\":\"STATISTICAL\",\"stringValue\":\"record\",\"literal\":\"enregistrer\",\"sensitive\":false,\"formattedLiteral\":\"enregistrer\",\"formattedTextRange\":{\"startIndex\":8.0,\"endIndex\":19.0}}]}}}}],\"sensitive\":false,\"formattedLiteral\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"metadata\":{\"x-nuance-supplemental-metrics-result\":{\"modelLanguage\":\"fra-FRA\",\"versions\":{\"mrec\":\"1.42.100.19403\",\"qnlu\":\"2.22.7\",\"textProc\":\"15.07.100.02109\"},\"tokens\":[\"Peux-tu\",\"enregistrer\",\"Desperado\",\"demain\",\"matin\",\"sur\",\"TF1\",\"à\",\"9h30\"]}}}}}");
        NcsResponse response = NCSResponseBuilder.nluFinalResponseBuilder(mockOrchestrator, webapiResponse, "test");
        assertEquals("{\"message\":\"query_response\",\"transaction_id\":1,\"result_type\":\"test\",\"NMAS_PRFX_TRANSACTION_ID\":\"1\",\"final_response\":1,\"result_format\":\"nlu_interpretation_results\",\"nlu_interpretation_results\":{\"payload_version\":\"1.0\",\"payload_format\":\"nlu-base\",\"payload\":{\"type\":\"nlu-1.0\",\"interpretations\":[{\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"action\":{\"intent\":{\"value\":\"tv_search\",\"confidence\":0.94}},\"concepts\":{\"tv_air_date\":[{\"concepts\":{\"nuance_CALENDARX\":[{\"literal\":\"demain matin\",\"ranges\":[[30,42]],\"value\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}}]}},{\"literal\":\"à 9h30\",\"ranges\":[[51,57]],\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\"}],\"tv_query\":[{\"literal\":\"Desperado\",\"ranges\":[[20,29]],\"value\":\"Desperado\"}],\"tv_station\":[{\"literal\":\"TF1\",\"ranges\":[[47,50]],\"value\":\"tf1\"}],\"tv_service\":[{\"literal\":\"enregistrer\",\"ranges\":[[8,19]],\"value\":\"record\"}]}}]},\"final_response\":1,\"status\":\"success\"}}", response.toJson());
    }

    @Test
    public void nluFinalResponseBuilderLargeResponse_oldFormat() throws IOException {
        CombinedConfiguration config = new CombinedConfiguration();
        Orchestrator mockOrchestrator = mock (Orchestrator.class);
        when(mockOrchestrator.getWebApiFeatures()).thenReturn(mock(IWebApiFeatures.class));
        when(mockOrchestrator.getWebApiFeatures().getConfigs(any())).thenReturn(config);

        WebApiResponse webapiResponse = WebApiResponse.fromJson("{\"type\":\"result\",\"sessionId\":\"b81e93e1-b2b7-4baf-86da-cbc6afc0742a\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"success\",\"interpretations\":[{\"concepts\":[{\"concept\":\"tv_air_date\",\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}},\"literal\":\"demain matin\",\"begin\":30,\"end\":42},{\"concept\":\"tv_air_date\",\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"begin\":51,\"end\":57},{\"concept\":\"tv_query\",\"value\":\"Desperado\",\"literal\":\"Desperado\",\"begin\":20,\"end\":29},{\"concept\":\"tv_station\",\"value\":\"tf1\",\"literal\":\"TF1\",\"begin\":47,\"end\":50},{\"concept\":\"tv_service\",\"value\":\"record\",\"literal\":\"enregistrer\",\"begin\":8,\"end\":19}],\"confidence\":94,\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"intent\":\"tv_search\",\"origin\":\"STATISTICAL\"}],\"bestInterpretation\":{\"concepts\":[{\"concept\":\"tv_air_date\",\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}},\"literal\":\"demain matin\",\"begin\":30,\"end\":42},{\"concept\":\"tv_air_date\",\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"begin\":51,\"end\":57},{\"concept\":\"tv_query\",\"value\":\"Desperado\",\"literal\":\"Desperado\",\"begin\":20,\"end\":29},{\"concept\":\"tv_station\",\"value\":\"tf1\",\"literal\":\"TF1\",\"begin\":47,\"end\":50},{\"concept\":\"tv_service\",\"value\":\"record\",\"literal\":\"enregistrer\",\"begin\":8,\"end\":19}],\"confidence\":94,\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"intent\":\"tv_search\",\"origin\":\"STATISTICAL\"},\"verboseResult\":{\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"interpretations\":[{\"singleIntentInterpretation\":{\"intent\":\"tv_search\",\"confidence\":0.9449202,\"origin\":\"STATISTICAL\",\"entities\":{\"tv_air_date\":{\"entities\":[{\"textRange\":{\"startIndex\":30.0,\"endIndex\":42.0},\"confidence\":0.82713646,\"origin\":\"STATISTICAL\",\"structValue\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":5.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\"}}}},\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":11.0,\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\"}}}}}}},\"literal\":\"demain matin\",\"sensitive\":false,\"formattedLiteral\":\"demain matin\",\"formattedTextRange\":{\"startIndex\":30.0,\"endIndex\":42.0}},{\"textRange\":{\"startIndex\":51.0,\"endIndex\":57.0},\"confidence\":0.82601124,\"origin\":\"STATISTICAL\",\"stringValue\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"sensitive\":false,\"formattedLiteral\":\"à 9h30\",\"formattedTextRange\":{\"startIndex\":51.0,\"endIndex\":57.0}}]},\"tv_query\":{\"entities\":[{\"textRange\":{\"startIndex\":20.0,\"endIndex\":29.0},\"confidence\":0.8014085,\"origin\":\"STATISTICAL\",\"stringValue\":\"Desperado\",\"literal\":\"Desperado\",\"sensitive\":false,\"formattedLiteral\":\"Desperado\",\"formattedTextRange\":{\"startIndex\":20.0,\"endIndex\":29.0}}]},\"tv_station\":{\"entities\":[{\"textRange\":{\"startIndex\":47.0,\"endIndex\":50.0},\"confidence\":0.5830483,\"origin\":\"STATISTICAL\",\"stringValue\":\"tf1\",\"literal\":\"TF1\",\"sensitive\":false,\"formattedLiteral\":\"TF1\",\"formattedTextRange\":{\"startIndex\":47.0,\"endIndex\":50.0}}]},\"tv_service\":{\"entities\":[{\"textRange\":{\"startIndex\":8.0,\"endIndex\":19.0},\"confidence\":0.60814315,\"origin\":\"STATISTICAL\",\"stringValue\":\"record\",\"literal\":\"enregistrer\",\"sensitive\":false,\"formattedLiteral\":\"enregistrer\",\"formattedTextRange\":{\"startIndex\":8.0,\"endIndex\":19.0}}]}}}}],\"sensitive\":false,\"formattedLiteral\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"metadata\":{\"x-nuance-supplemental-metrics-result\":{\"modelLanguage\":\"fra-FRA\",\"versions\":{\"mrec\":\"1.42.100.19403\",\"qnlu\":\"2.22.7\",\"textProc\":\"15.07.100.02109\"},\"tokens\":[\"Peux-tu\",\"enregistrer\",\"Desperado\",\"demain\",\"matin\",\"sur\",\"TF1\",\"à\",\"9h30\"]}}}}}");
        NcsResponse response = NCSResponseBuilder.nluFinalResponseBuilder(mockOrchestrator, webapiResponse, "test");
        assertEquals("{\"message\":\"query_response\",\"transaction_id\":1,\"result_type\":\"test\",\"NMAS_PRFX_TRANSACTION_ID\":\"1\",\"final_response\":1,\"result_format\":\"nlu_interpretation_results\",\"nlu_interpretation_results\":{\"payload_version\":\"1.0\",\"payload_format\":\"nlu-base\",\"payload\":{\"type\":\"nlu-1.0\",\"interpretations\":[{\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"action\":{\"intent\":{\"value\":\"tv_search\",\"confidence\":0.94}},\"concepts\":{\"tv_air_date\":[{\"literal\":\"demain matin\",\"ranges\":[[30,42]],\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}}},{\"literal\":\"à 9h30\",\"ranges\":[[51,57]],\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\"}],\"tv_query\":[{\"literal\":\"Desperado\",\"ranges\":[[20,29]],\"value\":\"Desperado\"}],\"tv_station\":[{\"literal\":\"TF1\",\"ranges\":[[47,50]],\"value\":\"tf1\"}],\"tv_service\":[{\"literal\":\"enregistrer\",\"ranges\":[[8,19]],\"value\":\"record\"}]}}]},\"final_response\":1,\"status\":\"success\"}}", response.toJson());
    }
}
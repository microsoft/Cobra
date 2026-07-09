package com.nuance.plugin.backend.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.plugin.backend.v1.json.RequestJson;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TtsResponseSenderTest {
    private static final String SPEECH_SYNTHESIS_CODEC = "speechSynthesisCodec";
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    CombinedConfiguration config = mock(CombinedConfiguration.class);
    RequestJson queryParam;
    String requestJson = "{\n" +
            "  \"generalConfig\": {\n" +
            "    \"sensitivity\": \"masked\",\n" +
            "    \"statistics\": false\n" +
            "  },\n" +
            "  \"input\": {\n" +
            "    \"text\": \"string\"\n" +
            "  },\n" +
            "  \"voice\": {\n" +
            "    \"name\": \"string\",\n" +
            "    \"gender\": \"Female\",\n" +
            "    \"locale\": \"string\"\n" +
            "  },\n" +
            "  \"audioConfig\": {\n" +
            "    \"audioEncoding\": \"pcm_16_16k\"\n" +
            "  },\n" +
            "  \"clientMetaDataConfig\": {\n" +
            "    \"clientUserId\": \"string\",\n" +
            "    \"clientDeviceId\": \"string\",\n" +
            "    \"clientSessionId\": \"string\",\n" +
            "    \"clientOsName\": \"string\",\n" +
            "    \"clientGUID\": \"string\",\n" +
            "    \"clientOversion\": \"string\"\n" +
            "  }\n" +
            "}";

    @Before
    public void initTest() throws JsonProcessingException {
        when(config.getInt(anyString(), anyInt())).thenReturn(10);
        when(config.getString(SPEECH_SYNTHESIS_CODEC)).thenReturn("pcm_16_16k");
        queryParam = RequestJson.fromJson(requestJson);
    }

    @Test
    public void closeConnection() throws IOException {
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);
        (new Thread() {
            public void run() {
                try {
                    responseSender.waitForResponse(config, queryParam);
                } catch (IOException e) {
                    fail();
                }
            }
        }).start();
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\"}");

        responseSender.closeConnection(null);
        verify(servletResponse, times(0)).sendError(anyInt(), anyString());
    }

    @Test
    public void waitTimeout() throws IOException {
        when(config.getInt(anyString(), anyInt())).thenReturn(1);
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);
        responseSender.waitForResponse(config, queryParam);
        verify(servletResponse, times(1)).sendError(anyInt(), anyString());
    }

    @Test
    public void waitForResponseNoBinary() throws IOException {
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\", \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":true, \"result\":{\"status\":\"success\"}}");
        StringWriter sr = new StringWriter();
        PrintWriter testWriter = new PrintWriter(sr);

        when(servletResponse.getWriter()).thenReturn(testWriter);

        responseSender.waitForResponse(config, queryParam);

        verify(servletResponse, times(1)).getWriter();
        verify(servletResponse, times(0)).sendError(anyInt(), anyString());

        assertEquals("{\"clientSessionId\":\"string\",\"requestId\":\"1\"}", sr.toString().trim());
    }

    @Test
    public void waitForResponseBinaryPCM() throws IOException {
        when(config.getString(SPEECH_SYNTHESIS_CODEC)).thenReturn("pcm_16_16k");
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\", \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"result\":{\"final\":true,\"status\":\"success\"},\"sessionId\":\"7519a2ae-c594-411d-9ea6-0ec5d53467f2\",\"type\":\"result\",\"command\":{\"name\":\"speechSynthesis\",\"id\":\"tts\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":true, \"result\":{\"status\":\"success\"}}");
        StringWriter sr = new StringWriter();
        PrintWriter testWriter = new PrintWriter(sr);

        responseSender.sendBinary("test".getBytes());

        when(servletResponse.getWriter()).thenReturn(testWriter);

        responseSender.waitForResponse(config, queryParam);

        verify(servletResponse, times(1)).getWriter();
        verify(servletResponse, times(0)).sendError(anyInt(), anyString());

        assertEquals("{\"result\":{\"audioEncoding\":\"pcm_16_16k\",\"audioContent\":\"UklGRiwAAABXQVZFZm10IBAAAAABAAEAgD4AAAB9AAACABAAZGF0YQgAAAB0ZXN0\"},\"clientSessionId\":\"string\",\"requestId\":\"1\"}", sr.toString().trim());
    }

    @Test
    public void waitForResponseBinaryOpus() throws IOException {
        when(config.getString(SPEECH_SYNTHESIS_CODEC)).thenReturn("opus_wb");
        queryParam.audioConfig.put("audioEncoding", "opus_wb");
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);

        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\", \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":false, \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":true, \"result\":{\"status\":\"success\"}}");
        StringWriter sr = new StringWriter();
        PrintWriter testWriter = new PrintWriter(sr);

        responseSender.sendBinary("test".getBytes());

        when(servletResponse.getWriter()).thenReturn(testWriter);

        responseSender.waitForResponse(config, queryParam);

        verify(servletResponse, times(1)).getWriter();
        verify(servletResponse, times(0)).sendError(anyInt(), anyString());

        JSONObject received = new JSONObject(sr.toString());

        assertEquals(AudioCodec.OPUS_16K.getWebapiString(), received.getJSONObject("result").getString("audioEncoding"));
        assertEquals("1", received.getString("requestId"));
    }


    @Test (expected = IOException.class)
    public void waitForResponseWrongCodec() throws IOException {
        final String codecName = "ulaw";
        when(config.getString(SPEECH_SYNTHESIS_CODEC)).thenReturn(codecName);
        queryParam.audioConfig.put("audioEncoding", codecName);
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);

        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\", \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":false, \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":true, \"result\":{\"status\":\"success\"}}");
        StringWriter sr = new StringWriter();
        PrintWriter testWriter = new PrintWriter(sr);

        responseSender.sendBinary("test".getBytes());

        when(servletResponse.getWriter()).thenReturn(testWriter);

        responseSender.waitForResponse(config, queryParam);
    }

    @Test
    public void waitForResponseReturnStatistics() throws IOException {
        when(config.getString(SPEECH_SYNTHESIS_CODEC)).thenReturn("pcm_16_16k");
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\", \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\"result\":{\"final\":true,\"status\":\"success\",\"statistics\":{\"hostInfo\":{\"version\":\"2.31.2-SNAPSHOT\"}}},\"sessionId\":\"7519a2ae-c594-411d-9ea6-0ec5d53467f2\",\"type\":\"result\",\"command\":{\"name\":\"speechSynthesis\",\"id\":\"tts\"}}");
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":true, \"result\":{\"status\":\"success\"}}");
        StringWriter sr = new StringWriter();
        PrintWriter testWriter = new PrintWriter(sr);

        responseSender.sendBinary("test".getBytes());

        when(servletResponse.getWriter()).thenReturn(testWriter);

        responseSender.waitForResponse(config, queryParam);

        verify(servletResponse, times(1)).getWriter();
        verify(servletResponse, times(0)).sendError(anyInt(), anyString());

        assertEquals("{\"result\":{\"audioEncoding\":\"pcm_16_16k\",\"audioContent\":\"UklGRiwAAABXQVZFZm10IBAAAAABAAEAgD4AAAB9AAACABAAZGF0YQgAAAB0ZXN0\"},\"clientSessionId\":\"string\",\"requestId\":\"1\",\"statistics\":{\"hostInfo\":{\"version\":\"2.31.2-SNAPSHOT\"}}}", sr.toString().trim());
    }

    @Test
    public void waitForgetResourcesResponse() throws IOException {
        when(config.getString(SPEECH_SYNTHESIS_CODEC)).thenReturn("pcm_16_16k");
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"result\", \"result\":{\"status\":\"success\"}}");
        responseSender.sendResponse("{\n" +
                "  \"type\":\"result\",\n" +
                "  \"sessionId\":\"29760dc3-7bde-4e7a-83f9-0f16969defec\",\n" +
                "  \"command\":{\n" +
                "    \"name\":\"getResources\",\n" +
                "    \"id\":\"2\"\n" +
                "  },\n" +
                "  \"result\":{\n" +
                "    \"final\":true,\n" +
                "    \"status\":\"success\",\n" +
                "    \"resources\":{\n" +
                "      \"speechRecognition\":{\n" +
                "        \"speechRecognitionCodec\":\"pcm_16_16k\",\n" +
                "        \"speechRecognitionEngine\":\"krypton\",\n" +
                "        \"languagePack\":\"eng-USA\"\n" +
                "      },\n" +
                "      \"speechSynthesis\":{\n" +
                "        \"vocalizer\":\"vocs\",\n" +
                "        \"voices\":[\n" +
                "          {\n" +
                "            \"name\":\"Evan\",\n" +
                "            \"voice\":[\n" +
                "              {\n" +
                "                \"codec\":\"pcm_16_16k\",\n" +
                "                \"voice_model\":\"xpremium-high\"\n" +
                "              },\n" +
                "              {\n" +
                "                \"codec\":\"pcm_16_22k\",\n" +
                "                \"voice_model\":\"full_vssq5f22\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}".trim().replace("\n", ""));
        responseSender.sendResponse("{\"sessionId\":\"1\", \"type\":\"commandChainResult\", \"final\":true, \"result\":{\"status\":\"success\"}}");
        StringWriter sr = new StringWriter();
        PrintWriter testWriter = new PrintWriter(sr);

        when(servletResponse.getWriter()).thenReturn(testWriter);


        responseSender.waitForResponse(config, queryParam);

        verify(servletResponse, times(1)).getWriter();
        verify(servletResponse, times(0)).sendError(anyInt(), anyString());

        JSONObject responseJSON = new JSONObject(sr.toString().trim());

        assertEquals("Evan", new JSONObject(responseJSON.getJSONArray("voices").get(0).toString()).get("name"));
    }

    @Test
    public void testProcessNonSuccess()
    {
        TtsResponseSender responseSender = new TtsResponseSender(servletResponse);

        JSONObject result = new JSONObject();
        JSONArray errors = new JSONArray();
        JSONObject error = new JSONObject();
        error.put("code", 1105);
        error.put("message", "Tenant rate limit exceeded");
        errors.put(error);
        result.put("errors", errors);
        JSONObject responseJson = new JSONObject();

        responseSender.processNonSuccess(result, responseJson);
        verify(servletResponse, times(1)).setStatus(429);
        assertTrue(responseJson.get("errors") != null);
    }
}

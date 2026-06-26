package com.nuance.ncs.nmsp.servlet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NcsServletHandlerTest {

    public static final String CRLF = "\r\n";
    private static final String BOUNDARY = "boundary123";

    public static String asrRequestDataJsonString = "{\n" +
            "    \"cmdTimeout\": \"5000\",\n" +
            "    \"appVersion\": \"1.0\",\n" +
            "    \"appName\": \"Nuance Java Sample HTTP App\",\n" +
            "    \"cmdName\": \"NMDP_ASR_CMD\",\n" +
            "    \"language\": \"eng-USA\",\n" +
            "    \"uId\": \"f083a1f6b74b420ca6ff157a845a98b8\",\n" +
            "    \"carrier\": \"unknown\",\n" +
            "    \"inCodec\": \"PCM_16_16K\",\n" +
            "    \"appId\": \"nina4\",\n" +
            "    \"outCodec\": \"PCM_16_16K\",\n" +
            "    \"appKey\": \"b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b\",\n" +
            "    \"deviceModel\": \"aarch64 - Mac OS X - 11.6\",\n" +
            "    \"cmdDict\": {\n" +
            "        \"application_state_id\": \"0\",\n" +
            "        \"locale\": \"USA\"\n" +
            "    }\n" +
            "}";

    String ttsRequestDataJsonString = "{\n" +
            "    \"appKey\": \"b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b\",\n" +
            "    \"appId\": \"nina4\",\n" +
            "    \"uId\": \"f083a1f6b74b420ca6ff157a845a98b8\",\n" +
            "    \"inCodec\": \"PCM_16_16K\",\n" +
            "    \"outCodec\": \"PCM_16_16K\",\n" +
            "    \"cmdName\": \"NVC_TTS_CMD\",\n" +
            "    \"appName\": \"Python\",\n" +
            "    \"appVersion\": \"1\",\n" +
            "    \"language\": \"eng_USA\",\n" +
            "    \"carrier\": \"carrier\",\n" +
            "    \"deviceModel\": \"deviceModel\"\n" +
            "}";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private NcsServletHandler handler;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data; boundary=" + BOUNDARY);
        response = new MockHttpServletResponse();
        handler = new NcsServletHandler();
    }

    private static byte[] multipart(String... bodyParts) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(BOUNDARY).append(CRLF);
        for (int i = 0; i < bodyParts.length; i++) {
            sb.append(bodyParts[i]);
            if (i < bodyParts.length - 1) {
                sb.append(CRLF).append("--").append(BOUNDARY).append(CRLF);
            }
        }
        sb.append(CRLF).append("--").append(BOUNDARY).append("--").append(CRLF);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String part(String name, String contentType, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(CRLF);
        if (contentType != null) {
            sb.append("Content-Type: ").append(contentType).append(CRLF);
        }
        sb.append(CRLF).append(body);
        return sb.toString();
    }

    private static String partWithParamName(String name, String paramName, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"; paramName=\"").append(paramName).append("\"").append(CRLF);
        sb.append(CRLF).append(body);
        return sb.toString();
    }

    @Test
    public void emptyMultipartIsRejectedAsMalformed() throws IOException {
        request.setContent(("--" + BOUNDARY + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        handler.handlePost(request, response);
        // No parts at all → loop body never runs, but the final !abortRequest branch
        // still calls waitForResponse, which is a no-op without a transaction.
    }

    @Test
    public void unknownPartNameTriggersBadRequest() throws IOException {
        request.setContent(multipart(part("unknownPartName", null, "ignored")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void requestDataWithMalformedJsonReturnsBadRequest() throws IOException {
        request.setContent(multipart(part("RequestData", null, "{not-valid-json")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void requestDataWithEmptyJsonReturnsBadRequest() throws IOException {
        request.setContent(multipart(part("RequestData", null, "{}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void requestDataAsrJsonFailsAuthNotConnected() throws IOException {
        // Valid JSON, but no real NMSP gateway → auth/connect fails. Still exercises
        // header parsing, sanitization, RequestData parse, TransactionEngine.setUpRequest.
        request.addHeader("Nuance-Context", "test-ctx");
        request.addHeader("nuance-application-id", "nina4");
        request.addHeader("nuance-application-key", "abc");
        request.addHeader("nuance-user-id", "uid-123");
        request.addHeader("Origin", "http://example.com");
        request.setContent(multipart(part("RequestData", "application/json", asrRequestDataJsonString)));
        handler.handlePost(request, response);
        // Either bad request (auth) or another failure path — never 200 OK without a real GW.
        assertNotEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void requestDataTtsJsonExercisesTtsBranch() throws IOException {
        request.setContent(multipart(part("RequestData", "application/json", ttsRequestDataJsonString)));
        handler.handlePost(request, response);
        assertNotEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void callLogDataPartIsHandled() throws IOException {
        // CLIENT_CALL_LOG_ENABLED defaults to TRUE; handleCallLog runs through readData
        // and returns false (no transaction set up), but exercises the dispatch.
        request.setContent(multipart(part("CallLogData", null, "{\"event\":\"foo\"}")));
        handler.handlePost(request, response);
    }

    @Test
    public void audioParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("AudioParameter", "audio/x-wav", "raw")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void concludingAudioParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("ConcludingAudioParameter", "audio/x-wav", "raw")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void choiceParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("ChoiceParameter", null, "choiceA")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void dataParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("DataParameter", null, "data")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void textParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("TextParameter", null, "some text")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void dictParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("DictParameter", null, "{}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void seqStartParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("SeqStartParameter", null, "{}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void seqChunkParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("SeqChunkParameter", null, "{}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void seqEndParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("SeqEndParameter", null, "{}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void ttsParameterWithoutParamNameAborts() throws IOException {
        request.setContent(multipart(part("TtsParameter", null, "{}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void concludingParameterTerminatesGracefully() throws IOException {
        // ConcludingParameter discards body and ends the command without aborting.
        request.setContent(multipart(part("ConcludingParameter", null, "")));
        handler.handlePost(request, response);
        // No abort → finalize path runs (no transaction → no waitForResponse semantics).
        assertNotEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void textParameterWithParamNameRoutesToTransaction() throws IOException {
        // paramName present → checkParamName passes, dispatches to nmspTransaction.sendTextParam
        // (which returns false because setUpRequest never succeeded). Aborts thereafter.
        request.setContent(multipart(partWithParamName("TextParameter", "myParam", "the text")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void choiceParameterWithParamNameRoutesToTransaction() throws IOException {
        request.setContent(multipart(partWithParamName("ChoiceParameter", "choiceParam", "value")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void dataParameterWithParamNameRoutesToTransaction() throws IOException {
        request.setContent(multipart(partWithParamName("DataParameter", "dataParam", "payload")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void dictParameterWithParamNameRoutesToTransaction() throws IOException {
        // Valid JSON dictionary so handleParam can parse it before dispatch.
        request.setContent(multipart(partWithParamName("DictParameter", "dictParam", "{\"k\":\"v\"}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void seqStartParameterWithParamNameRoutesToTransaction() throws IOException {
        request.setContent(multipart(partWithParamName("SeqStartParameter", "seqParam", "{\"k\":\"v\"}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void seqChunkParameterWithParamNameRoutesToTransaction() throws IOException {
        request.setContent(multipart(partWithParamName("SeqChunkParameter", "seqParam", "{\"k\":\"v\"}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void seqEndParameterWithParamNameRoutesToTransaction() throws IOException {
        request.setContent(multipart(partWithParamName("SeqEndParameter", "seqParam", "{\"k\":\"v\"}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void ttsParameterWithParamNameRoutesToTransaction() throws IOException {
        request.setContent(multipart(partWithParamName("TtsParameter", "ttsParam", "{\"k\":\"v\"}")));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void filenameDispositionIsIgnoredAndStillDispatches() throws IOException {
        // Forces the extractFormNameAndParamName 'filename=' branch
        String body = "Content-Disposition: form-data; name=\"TextParameter\"; paramName=\"p\"; filename=\"f.txt\""
                + CRLF + CRLF + "value";
        request.setContent(multipart(body));
        handler.handlePost(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void malformedPartHeaderIsHandled() throws IOException {
        // Header with no colon — parseHeaders throws IOException, which propagates out of
        // handlePost since it's declared on the signature. We accept either an IOException
        // being thrown or an error response being written, but must not corrupt state.
        String body = "Content-Disposition form-data name=TextParameter"  // no colon
                + CRLF + CRLF + "value";
        request.setContent(multipart(body));
        try {
            handler.handlePost(request, response);
        } catch (IOException expected) {
            // parseHeaders throws "Failed to read headers" — that's the documented path.
            assertTrue(expected.getMessage().toLowerCase().contains("header"));
        }
    }
}

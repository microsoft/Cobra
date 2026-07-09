package com.nuance.ncs.nmsp.servlet.response;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;

public class ResponseManagerTest {

    @Test
    public void constructorWithContext() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx-123");
        assertNotNull(rm);
    }

    @Test
    public void constructorWithNullContextGeneratesUUID() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, null);
        assertNotNull(rm);
    }

    @Test
    public void setSessionId() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess-abc");
        // No exception expected
    }

    @Test
    public void sendJsonPartFinalResponse() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess-123");
        JSONObject json = new JSONObject();
        json.put("result", "ok");
        rm.sendJsonPart("nlu_results", json, true, "http://example.com");
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
    }

    @Test
    public void sendJsonPartNonFinal() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess-xyz");
        JSONObject json = new JSONObject();
        json.put("partial", "data");
        rm.sendJsonPart("partial_result", json, false, null);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
    }

    @Test
    public void sendAudioPart() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess-audio");
        rm.sendAudioPart("audio", new byte[]{1, 2, 3}, "audioParam", "audio/x-wav", null);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
    }

    @Test
    public void sendErrorReplyWithoutSessionId() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.sendErrorReply(HttpServletResponse.SC_FORBIDDEN, "Not authorized");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatus());
    }

    @Test
    public void sendErrorReplyWithSessionId() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess-err");
        rm.sendErrorReply(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service down");
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, resp.getStatus());
    }

    @Test
    public void quietSendError() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager.quietSendError(resp, HttpServletResponse.SC_BAD_GATEWAY, "Bad gateway");
        assertEquals(HttpServletResponse.SC_BAD_GATEWAY, resp.getStatus());
    }

    @Test
    public void sendMultipleJsonParts() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess");
        JSONObject j1 = new JSONObject(); j1.put("partial", true);
        JSONObject j2 = new JSONObject(); j2.put("final", true);
        rm.sendJsonPart("partial", j1, false, null);
        rm.sendJsonPart("final", j2, true, null);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
    }

    @Test
    public void sendAudioThenJson() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ResponseManager rm = new ResponseManager(resp, "ctx");
        rm.setSessionId("sess");
        rm.sendAudioPart("audio", new byte[16], "tts_audio", "audio/x-wav", null);
        JSONObject json = new JSONObject(); json.put("done", true);
        rm.sendJsonPart("result", json, true, null);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
    }
}

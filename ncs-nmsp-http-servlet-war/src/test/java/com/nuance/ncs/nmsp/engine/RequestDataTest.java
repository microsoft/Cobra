package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.pdx.NmspParamType;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestDataTest
{
    @Test
    public void getParmType()
    {
        RequestData requestData = new RequestData("{}", null, null, null, null);
        assertEquals(NmspParamType.SDK, requestData.getParmType("SDK"));
        assertEquals(NmspParamType.NMSP, requestData.getParmType("NMSP"));
        assertEquals(NmspParamType.APP, requestData.getParmType("APP"));
        assertEquals(NmspParamType.NSS, requestData.getParmType("NSS"));
        assertEquals(NmspParamType.SLOG, requestData.getParmType("SLOG"));
        assertEquals(NmspParamType.NSSLOG, requestData.getParmType("NSSLOG"));
        assertEquals(NmspParamType.GWLOG, requestData.getParmType("GWLOG"));
        assertEquals(NmspParamType.SVSP, requestData.getParmType("SVSP"));
        assertEquals(NmspParamType.SIP, requestData.getParmType("SIP"));
        assertEquals(NmspParamType.SDP, requestData.getParmType("SDP"));
        assertNull(requestData.getParmType("TOTO"));
    }

    @Test
    public void various()
    {
        String jsonString = "{]";
        RequestData requestData = new RequestData(jsonString, null, null, null, null);
        assertFalse(requestData.succeeded());

        jsonString = "{" +
                     "\"srvrIP\": \"127.0.0.2\"," +
                     "\"srvrPort\": \"8912\"," +
                     "\"inCodec\": \"SPEEX_16K\"," +
                     "\"outCodec\": \"SPEEX_16K\"," +
                     "\"appVersion\": \"1.0\"," +
                     "\"carrier\": \"unknown\"," +
                     "\"deviceModel\": \"aarch64 - Mac OS X - 11.6\"," +
                     "\"appName\": \"Nuance Java Sample HTTP App\"," +
                     "\"cmdName\": \"NMDP_ASR_CMD\"," +
                     "\"language\": \"eng-USA\"," +
                     "\"mgerPrms\": [{\"name\": \"testName\", \"value\":\"testValue\", \"type\":\"SDK\"}]" +
                     "}";
        requestData = new RequestData(jsonString, "nina4", "abc123", "xyz987", null);
        assertTrue(requestData.succeeded());
        requestData = new RequestData(jsonString, "nina4", "abc12", "xyz987", null);
        assertFalse(requestData.succeeded());
    }

    @Test
    public void getInOutCodecs()
    {
        JSONObject jsonObject = new JSONObject("{}");
        String[] inOutCodecs = RequestData.getInOutCodecs(jsonObject, "");
        assertArrayEquals(new String[] {"PCM_16_16K", "PCM_16_16K"}, inOutCodecs);

        jsonObject = new JSONObject("{\"inCodec\":\"PCM_16_16K\"}");
        inOutCodecs = RequestData.getInOutCodecs(jsonObject, "");
        assertArrayEquals(new String[] {"PCM_16_16K", "PCM_16_16K"}, inOutCodecs);

        jsonObject = new JSONObject("{\"outCodec\":\"PCM_16_16K\"}");
        inOutCodecs = RequestData.getInOutCodecs(jsonObject, "");
        assertArrayEquals(new String[] {"PCM_16_16K", "PCM_16_16K"}, inOutCodecs);

        jsonObject = new JSONObject("{\"inCodec\":\"PCM_16_16K\", \"outCodec\":\"PCM_16_8K\"}");
        inOutCodecs = RequestData.getInOutCodecs(jsonObject, "NVC_TTS_CMD");
        assertArrayEquals(new String[] {"PCM_16_8K", "PCM_16_8K"}, inOutCodecs);

        inOutCodecs = RequestData.getInOutCodecs(jsonObject, "NVC_ASR_CMD");
        assertArrayEquals(new String[] {"PCM_16_16K", "PCM_16_8K"}, inOutCodecs);
    }

}
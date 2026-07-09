package com.nuance.plugin.ncs7.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.plugin.ncs.utils.HashType;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NcsRequestTest {

    @Test
    public void fromJson() throws JsonProcessingException {
        NcsRequest request = NcsRequest.fromJson("{\n" +
                "            \"message\" : \"message\"\n" +
                "        }");

        assertEquals("message", request.message);
    }

    @Test
    public void codecParser() throws IOException {
        NcsRequest request = NcsRequest.fromJson("{\n" +
                "            \"message\" : \"message\",\n" +
                "            \"codec\" : \"audio/x-raw;format=s16le;rate=16000\"\n" +
                "        }");

        assertEquals("pcm_16_16k", request.getWebAPICodecName(request.getCodecName()));
        assertEquals(5, (int)request.getCodecId());

        assertFalse(request.getResultDelivery());

        assertFalse(request.getResultDelivery());

        request = NcsRequest.fromJson("{\n" +
                "            \"message\" : \"message\",\n" +
                "            \"codec_id\" : \"5\"\n" +
                "        }");

        assertEquals("pcm_16_16k", request.getWebAPICodecName(request.getCodecName()));

        request = NcsRequest.fromJson("{\n" +
                "            \"message\" : \"message\",\n" +
                "            \"device_codec\" : \"5\"\n" +
                "        }");

        assertEquals("pcm_16_16k", request.getWebAPICodecName(request.getCodecName()));

        request = NcsRequest.fromJson("{\n" +
                "            \"message\" : \"message\"\n" +
                "        }");

        assertNull(request.getWebAPICodecName(request.getCodecName()));
    }

    @Test
    public void utteranceDetectionSilenceDurationsParser() throws JsonProcessingException {
        NcsRequest request = NcsRequest.fromJson("{\n" +
                "            \"message\" : \"message\",\n" +
                "            \"utterance_detection_silence_duration\" : \"VeryAggressive\"\n" +
                "        }");

        assertEquals(30, (int) request.getEndOfSpeechVoicedFrames());
    }

    @Test
    public void getUserID() throws JsonProcessingException, IOException {
        NcsRequest request = NcsRequest.fromJson("{\n" +
                "            \"user_id\" : \"test\"\n" +
                "        }");
        assertEquals("bd876db5-8db6-3941-827f-7addf493ac67", request.getUserId(HashType.HASH_TYPE_NCS, "fakeSalt"));
    }

    @Test
    public void getUserID_empty() throws JsonProcessingException, IOException {
        NcsRequest request = NcsRequest.fromJson("{\n" +
                "            \"user_id\" : \"\"\n" +
                "        }");
        assertEquals(null, request.getUserId(HashType.HASH_TYPE_NCS, "fakeSalt"));
    }

    @Test
    public void getUserID_emptyDefaultsToDeviceId() throws JsonProcessingException, IOException {
        NcsRequest request = NcsRequest.fromJson("{\n" +
                "            \"user_id\" : \"\",\n" +
                "            \"device_id\" : \"hi mike\"\n" +
                "        }");
        assertEquals("83e5c7c2-6d2f-3522-a118-ebe5f4f403e5", request.getUserId(HashType.HASH_TYPE_NCS, "fakeSalt"));
    }

    @Test
    public void testFromMap()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", "meow");
        map.put("device_id", "myDeviceId");
        List<Object> products = new ArrayList<>();
        products.add(Map.of("feature", "model-improvements", "enable", false));
        map.put("productImprovements", products);
        NcsRequest request = NcsRequest.fromMap(map);
        assertEquals("myDeviceId", request.device_id);
    }

    @Test
    public void testGetTTsLanguage() {
        Map<String, Object> map = new HashMap<>();
        map.put("language", "en-us");
        NcsRequest request = NcsRequest.fromMap(map);
        assertEquals("en-us", request.getTTSLanguage());

        map.put("tts_language", "eng-usa");
        request = NcsRequest.fromMap(map);
        assertEquals("eng-usa", request.getTTSLanguage());
    }

    @Test
    public void testGetDeviceModel() {
        Map<String, Object> map = new HashMap<>();
        String key = "phoneModel";
        map.put(key, key);
        NcsRequest request = NcsRequest.fromMap(map);
        assertEquals(key, request.getDeviceModel());

        key = "phone_model";
        map.put(key, key);
        request = NcsRequest.fromMap(map);
        assertEquals(key, request.getDeviceModel());

        key = "deviceModel";
        map.put(key, key);
        request = NcsRequest.fromMap(map);
        assertEquals(key, request.getDeviceModel());
    }
}
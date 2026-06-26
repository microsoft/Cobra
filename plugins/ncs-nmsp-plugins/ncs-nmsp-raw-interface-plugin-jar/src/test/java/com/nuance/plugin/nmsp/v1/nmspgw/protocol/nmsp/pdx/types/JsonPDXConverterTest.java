package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonPDXConverterTest {

    @Test
    public void test_pdxDictionaryToJsonObject() {
        Dictionary dictionary = new PDXDictionary();
        dictionary.addInteger("intKey", 42);
        dictionary.addUTF8String("stringKey", "value");
        dictionary.addAsciiString("asciiKey", "value2");
        dictionary.addByteString("bytesKey", new byte[]{1, 2, 3});

        Dictionary nestedDict = new PDXDictionary();
        nestedDict.addInteger("nestedIntKey", 24);
        dictionary.addDictionary("dictKey", nestedDict);

        Sequence sequence = new PDXSequence();
        sequence.addInteger(100);
        sequence.addUTF8String("seqValue");
        dictionary.addSequence("seqKey", sequence);

        JSONObject jsonObject = JsonPDXConverter.pdxDictionaryToJsonObject(dictionary);

        assertEquals(42, jsonObject.getInt("intKey"));
        assertEquals("value", jsonObject.getString("stringKey"));
        assertEquals("value2", jsonObject.getString("asciiKey"));
        JSONArray bytesArray = jsonObject.getJSONArray("bytesKey");
        assertEquals(1, bytesArray.getInt(0));
        assertEquals(2, bytesArray.getInt(1));
        assertEquals(3, bytesArray.getInt(2));

        JSONObject nestedJsonObject = jsonObject.getJSONObject("dictKey");
        assertEquals(24, nestedJsonObject.getInt("nestedIntKey"));

        JSONArray seqArray = jsonObject.getJSONArray("seqKey");
        assertEquals(100, seqArray.getInt(0));
        assertEquals("seqValue", seqArray.getString(1));
    }

    @Test
    public void test_jsonObjectToPdxDictionary() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("intKey", 42);
        jsonObject.put("stringKey", "value");

        JSONObject nestedJsonObject = new JSONObject();
        nestedJsonObject.put("nestedIntKey", 24);
        jsonObject.put("dictKey", nestedJsonObject);

        JSONArray seqArray = new JSONArray();
        seqArray.put(100);
        seqArray.put("seqValue");
        jsonObject.put("seqKey", seqArray);

        Dictionary dictionary = JsonPDXConverter.jsonObjectToPdxDictionary(jsonObject);

        assertEquals(42, dictionary.getInteger("intKey"));
        assertEquals("value", dictionary.getUTF8String("stringKey"));

        Dictionary nestedDict = dictionary.getDictionary("dictKey");
        assertEquals(24, nestedDict.getInteger("nestedIntKey"));

        Sequence sequence = dictionary.getSequence("seqKey");
        assertEquals(100, sequence.getInteger(0));
        assertEquals("seqValue", sequence.getUTF8String(1));
    }

    @Test
    public void test_jsonArrayToPdxSequence() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(42);
        jsonArray.put("value");

        JSONObject nestedJsonObject = new JSONObject();
        nestedJsonObject.put("nestedIntKey", 24);
        jsonArray.put(nestedJsonObject);

        Sequence sequence = JsonPDXConverter.jsonArrayToPdxSequence(jsonArray);

        assertEquals(42, sequence.getInteger(0));
        assertEquals("value", sequence.getUTF8String(1));

        Dictionary nestedDict = sequence.getDictionary(2);
        assertEquals(24, nestedDict.getInteger("nestedIntKey"));
    }

    @Test
    public void test_jsonArrayToByteArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(1);
        jsonArray.put(2);
        jsonArray.put(3);

        byte[] byteArray = JsonPDXConverter.jsonArrayToByteArray(jsonArray);

        assertEquals(3, byteArray.length);
        assertEquals(1, byteArray[0]);
        assertEquals(2, byteArray[1]);
        assertEquals(3, byteArray[2]);
    }

    @Test
    public void test_jsonArrayToByteArray_withInvalidData() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(1);
        jsonArray.put("invalid");

        byte[] byteArray = JsonPDXConverter.jsonArrayToByteArray(jsonArray);

        assertNull(byteArray);
    }

    @Test
    public void test_pdxSequenceToJsonArray() {
        Sequence sequence = new PDXSequence();
        sequence.addInteger(42);
        sequence.addUTF8String("value");
        sequence.addAsciiString("value2");
        sequence.addByteString(new byte[]{1, 2, 3});

        Dictionary nestedDict = new PDXDictionary();
        nestedDict.addInteger("nestedIntKey", 24);
        sequence.addDictionary(nestedDict);

        JSONArray jsonArray = JsonPDXConverter.pdxSequenceToJsonArray(sequence);

        assertEquals(42, jsonArray.getInt(0));
        assertEquals("value", jsonArray.getString(1));
        assertEquals("value2", jsonArray.getString(2));

        JSONArray bytesArray = jsonArray.getJSONArray(3);
        assertEquals(1, bytesArray.getInt(0));
        assertEquals(2, bytesArray.getInt(1));
        assertEquals(3, bytesArray.getInt(2));

        JSONObject nestedJsonObject = jsonArray.getJSONObject(4);
        assertEquals(24, nestedJsonObject.getInt("nestedIntKey"));
    }

    @Test
    public void test_byteArrayToJsonArray() {
        byte[] byteArray = new byte[]{1, 2, 3};

        JSONArray jsonArray = JsonPDXConverter.byteArrayToJsonArray(byteArray);

        assertEquals(3, jsonArray.length());
        assertEquals(1, jsonArray.getInt(0));
        assertEquals(2, jsonArray.getInt(1));
        assertEquals(3, jsonArray.getInt(2));
    }
}

package com.nuance.ncs.nmsp.servlet.utils;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.nmsp.servlet.nmas.MyQueryError;
import com.nuance.nmsp.servlet.nmas.MyQueryRetry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class JsonPdxConverterTest
{

    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new JsonPdxConverter();
    }

    @Test
    public void jsonObjectToPdxDictionary()
    {
        String jsonString = "{ \"key\": \"value\", \"object\": {\"a\": \"b\"}, \"array1\": [5], \"array2\": [\"a\"], \"int\": 5, \"null\": null }";
        JSONObject jsonObject = new JSONObject(jsonString);
        NmspDictionary dictionary = JsonPdxConverter.jsonObjectToPdxDictionary(jsonObject);
        jsonObject = JsonPdxConverter.pdxDictionaryToJsonObject(dictionary);
        Assert.assertEquals("value", dictionary.getUTF8String("key"));
    }

    @Test
    public void jsonArrayToPdxSequence()
    {
        String jsonString = "{ \"array\": [\"value0\", {\"key1\": \"value1\"}, [\"value2\"], [5], 5 ] }";
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("array");
        NmspSequence sequence = JsonPdxConverter.jsonArrayToPdxSequence(jsonArray);
        jsonArray = JsonPdxConverter.pdxSequenceToJsonArray(sequence);
        Assert.assertEquals("value0", sequence.getUTF8String(0));
    }

    @Test
    public void queryErrorToJsonObject()
    {
        MyQueryError myQueryError = new MyQueryError(550, "550");
        JSONObject jsonObject = JsonPdxConverter.queryErrorToJsonObject(myQueryError);
        Assert.assertEquals(550, jsonObject.getInt("Error"));
    }

    @Test
    public void queryRetryToJsonObject()
    {
        MyQueryRetry myQueryRetry = new MyQueryRetry("Bad input", 4, "Try again");
        JSONObject jsonObject = JsonPdxConverter.queryRetryToJsonObject(myQueryRetry);
        Assert.assertEquals(4, jsonObject.getInt("Cause"));
    }
}

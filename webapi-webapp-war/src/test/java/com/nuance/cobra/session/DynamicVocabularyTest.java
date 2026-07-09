package com.nuance.cobra.session;

//import com.sun.org.apache.xpath.internal.operations.String;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DynamicVocabularyTest
{
    static final String inlineJsonString = "            {\n" +
                              "               \"id\":\"some_id1\",\n" +
                              "               \"type\":\"application/x-nuance-wordset\",\n" +
                              "               \"body\":{\n" +
                              "                  \"place-name\":[\n" +
                              "                     {\n" +
                              "                        \"literal\":\"Los Angeles\",\n" +
                              "                        \"spoken\":[\n" +
                              "                           \"l a\",\n" +
                              "                           \"los angeles\"\n" +
                              "                        ]\n" +
                              "                     },\n" +
                              "                     {\n" +
                              "                        \"literal\":\"San Francisco\"\n" +
                              "                     }\n" +
                              "                  ],\n" +
                              "                  \"hockey-team\":[\n" +
                              "                     {\n" +
                              "                        \"literal\":\"Montreal Canadiens\",\n" +
                              "                        \"spoken\":[\n" +
                              "                           \"montreal canadiens\",\n" +
                              "                           \"habs\"\n" +
                              "                        ]\n" +
                              "                     },\n" +
                              "                     {\n" +
                              "                        \"literal\":\"Boston Bruins\"\n" +
                              "                     }\n" +
                              "                  ]\n" +
                              "               }\n" +
                              "            }";
    static JSONObject inlineJson = new JSONObject(inlineJsonString);

    public static JSONObject getInlineJson()
    {
        return inlineJson;
    }

    @Test
    public void getId()
            throws Exception
    {
    }

    @Test
    public void getType()
            throws Exception
    {
    }

    @Test
    public void setId()
            throws Exception
    {
    }

    @Test
    public void getSpokenList()
            throws Exception
    {
    }

    @Test
    public void populateSpokenList()
            throws Exception
    {
    }

    @Test
    public void fromJsonMap()
    {
        DynamicVocabulary dynamicVocabularyInline = DynamicVocabulary.fromJsonMap(inlineJson.toMap(), false, null);
        assertEquals("some_id1", dynamicVocabularyInline.getId());

        String urn = "urn:nuance-mix:tag:wordset:lang/A100_C1/places-compiled-ws/eng-USA/mix.asr";
        Map<String, Object> vocabMapUrn = Map.of("id", "abc", "type", "application/x-nuance-wordset-pkg", "uri", urn);
        DynamicVocabulary dynamicVocabularyUrn = DynamicVocabulary.fromJsonMap(vocabMapUrn, false, null);
        assertEquals(urn, dynamicVocabularyUrn.getUri());
    }

    @Test
    public void fromJsonMapBuiltin()
    {
        var builtinJson = new JSONObject();
        builtinJson.put("id", "builtInId");
        builtinJson.put("builtin", "TEMPERATURE");
        builtinJson.put("type", "application/x-nuance-builtin");
        builtinJson.put("weight", "0.05");
        DynamicVocabulary dynamicVocabularyBuiltin = DynamicVocabulary.fromJsonMap(builtinJson.toMap(), false, null);
        assertEquals("builtInId", dynamicVocabularyBuiltin.getId());
        assertEquals("TEMPERATURE", dynamicVocabularyBuiltin.getBuiltin());
        assertEquals("0.05", dynamicVocabularyBuiltin.getWeight());
    }

    @Test
    public void fromJsonMapDomainLM()
    {
        var domainLmJson = inlineJson;
        var uri = "urn:nuance-mix:tag:model/context-tag/mix.asr?=language=eng-USA";
        domainLmJson.put("uri", uri);
        domainLmJson.put("type", "application/x-nuance-domainlm");
        domainLmJson.put("weight", "0.05");
        DynamicVocabulary dynamicVocabularyDomainLm = DynamicVocabulary.fromJsonMap(domainLmJson.toMap(), false, null);
        assertEquals("some_id1", dynamicVocabularyDomainLm.getId());
        assertEquals(uri, dynamicVocabularyDomainLm.getUri());
        assertEquals("0.05", dynamicVocabularyDomainLm.getWeight());
    }

    @Test
    public void validateStringNotNullOrEmpty()
            throws Exception
    {
        Object data = new String();
        data = "test";
        java.lang.String name = "testKey";

        assertTrue(DynamicVocabulary.validateStringNotNullOrEmpty(data, name).equals("test"));

        data = "";
        try {
            DynamicVocabulary.validateStringNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty"));
        }

        data = null;
        try {
            DynamicVocabulary.validateStringNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty"));
        }

        data = new ArrayList<String>();
        try {
            DynamicVocabulary.validateStringNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should be a String"));
        }
    }

    @Test
    public void validateMap()
            throws Exception
    {
        Object data = new HashMap<String, Object>();
        java.lang.String name = "testKey";

        assertTrue(DynamicVocabulary.validateMap(data, name) == data);

        data = "";
        try {
            DynamicVocabulary.validateMap(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should be a Map"));
        }

        data = null;
        try {
            DynamicVocabulary.validateMap(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should be a Map"));
        }
    }

    @Test
    public void validateMapNotNullOrEmpty()
            throws Exception
    {
        Object data = new HashMap<String, Object>();
        java.lang.String name = "testKey";

        ((HashMap)data).put(name, "test");

        assertTrue(DynamicVocabulary.validateMapNotNullOrEmpty(data, name) == data);

        data = "";
        try {
            DynamicVocabulary.validateMapNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("Exception strings do not match", ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should be a Map"));
        }

        data = null;
        try {
            DynamicVocabulary.validateMapNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("Exception strings do not match", ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty"));
        }

        data = new HashMap<String, Object>();
        try {
            DynamicVocabulary.validateMapNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("Exception strings do not match", ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty"));
        }
    }

    @Test
    public void validateList()
            throws Exception
    {
        Object data = new ArrayList<String>();
        java.lang.String name = "testKey";

        assertTrue(DynamicVocabulary.validateList(data, name) == data);

        data = "";
        try {
            DynamicVocabulary.validateList(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should have a List"));
        }

        data = null;
        try {
            DynamicVocabulary.validateList(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should have a List"));
        }
    }

    @Test
    public void validateListNotNullOrEmpty()
            throws Exception
    {
        Object data = new ArrayList<String>();
        java.lang.String name = "testKey";

        ((ArrayList)data).add("test");

        assertTrue(DynamicVocabulary.validateListNotNullOrEmpty(data, name) == data);

        data = "";
        try {
            DynamicVocabulary.validateListNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("Exception strings do not match", ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should have a List"));
        }

        data = null;
        try {
            DynamicVocabulary.validateListNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("Exception strings do not match", ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty"));
        }

        data = new ArrayList<String>();
        try {
            DynamicVocabulary.validateListNotNullOrEmpty(data, name);
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("Exception strings do not match", ex.getMessage().equalsIgnoreCase("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty"));
        }
    }

    @Test(expected = Test.None.class)
    public void dynamicVocabConstructPersist()
    {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

        new DynamicVocabulary("abc", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, Map.of(), false, "", configs);

        new DynamicVocabulary("abc", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, Map.of(), true, "", configs);

        new DynamicVocabulary("abc", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, Map.of(), true, "add", configs);

        new DynamicVocabulary("abc", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, Map.of(), true, "remove", configs);
    }

}
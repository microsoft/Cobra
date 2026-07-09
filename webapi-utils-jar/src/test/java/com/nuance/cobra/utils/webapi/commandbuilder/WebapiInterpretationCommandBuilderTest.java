package com.nuance.cobra.utils.webapi.commandbuilder;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebapiInterpretationCommandBuilderTest
{
    @Test
    public void testBuildRequest()
    {
        WebapiInterpretationCommandBuilder builder = getWebapiInterpretationCommandBuilder();

        String expected = "{\n" +
                "  \"command\": {\n" +
                "    \"id\": \"requestId\",\n" +
                "    \"name\": \"interpretation\",\n" +
                "    \"parameters\": {\n" +
                "      \"interpretationVerboseResult\": false,\n" +
                "      \"activeDynamicVocabularySets\": [\n" +
                "        \"one\",\n" +
                "        \"two\"\n" +
                "      ],\n" +
                "      \"data\": \"data\",\n" +
                "      \"dialogModelApplicationVersion\": \"dialogModelApplicationVersion\",\n" +
                "      \"language\": \"language\",\n" +
                "      \"sensitivity\": \"sensitivity\",\n" +
                "      \"statistics\": true,\n" +
                "      \"type\": \"type\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"sessionId\": \"sessionId\",\n" +
                "  \"type\": \"command\"\n" +
                "}";

        assertEquals(new JSONObject(expected).toMap(), builder.buildRequest().toMap());
    }

    protected static WebapiInterpretationCommandBuilder getWebapiInterpretationCommandBuilder() {
        var builder = new WebapiInterpretationCommandBuilder("sessionId", "requestId")
                .data("data")
                .type("type")
                .dialogModelApplicationVersion("dialogModelApplicationVersion")
                .sensitivity("sensitivity")
                .activeDynamicVocabularySet(new String[]{"one", "two"})
                .statistics(true)
                .language("language")
                .interpretationVerboseResult(false);
        return builder;
    }
}

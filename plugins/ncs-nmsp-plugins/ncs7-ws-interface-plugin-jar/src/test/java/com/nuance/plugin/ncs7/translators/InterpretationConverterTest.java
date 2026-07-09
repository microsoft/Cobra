package com.nuance.plugin.ncs7.translators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InterpretationConverterTest {

    private String testResourcesDir = "src/test/resources/";

    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    private void testConvertToMixInterpretations(String cobraInterpretationsFileName, String mixInterpretationsFileName) throws IOException {
        String cobraInterpretationsJson = Files.readString(Path.of(testResourcesDir + cobraInterpretationsFileName));
        List<InterpretationConverter.CobraInterpretation> cobraInterpretations = objectMapper.readValue(cobraInterpretationsJson, new TypeReference<>() {});
        List<InterpretationConverter.MixInterpretation> mixInterpretations = InterpretationConverter.convertToMixInterpretations(true, cobraInterpretations);
        String mixInterpretationsJson = objectMapper.writeValueAsString(mixInterpretations);

        String expectedMixInterpretationsJson = Files.readString(Path.of(testResourcesDir + mixInterpretationsFileName));

        Object result = objectMapper.readValue(mixInterpretationsJson, Object.class);
        Object expected = objectMapper.readValue(expectedMixInterpretationsJson, Object.class);

        String resultJson = objectMapper.writeValueAsString(result);
        String expectedJson = objectMapper.writeValueAsString(expected);

        assertEquals(expectedJson, resultJson);
    }

    @Test
    public void testConvertToMixInterpretations_530() throws IOException {
        testConvertToMixInterpretations("530.cobra226.json", "530.mixv2.json");
    }

//    @Test
//    public void testConvertToMixInterpretations_August5thmorning() throws IOException {
//        testConvertToMixInterpretations("August 5th morning.cobra226.json", "August 5th morning.mixv2.json");
//    }

//    @Test
//    public void testConvertToMixInterpretations_August30th2021() throws IOException {
//        testConvertToMixInterpretations("August 30th 2021.cobra226.json", "August 30th 2021.mixv2.json");
//    }

    @Test
    public void testConvertToMixInterpretations_channelonehundredandfortyfive() throws IOException {
        testConvertToMixInterpretations("channel one hundred and forty five.cobra226.json", "channel one hundred and forty five.mixv2.json");
    }

    @Test
    public void testConvertToMixInterpretations_Channel456() throws IOException {
        testConvertToMixInterpretations("Channel 456.cobra226.json", "Channel 456.mixv2.json");
    }

    @Test
    public void testConvertToMixInterpretations_cnn() throws IOException {
        testConvertToMixInterpretations("cnn.cobra226.json", "cnn.mixv2.json");
    }

//    @Test
//    public void testConvertToMixInterpretations_NuanceVersion() throws IOException {
//        testConvertToMixInterpretations("Nuance Version.cobra226.json", "Nuance Version.mixv2.json");
//    }

    @Test
    public void testConvertToMixInterpretations_Onehundredandforty() throws IOException {
        testConvertToMixInterpretations("One hundred and forty.cobra226.json", "One hundred and forty.mixv2.json");
    }

//    @Test
//    public void testConvertToMixInterpretations_onehundredandtwentyseconds() throws IOException {
//        testConvertToMixInterpretations("one hundred and twenty seconds.cobra226.json", "one hundred and twenty seconds.mixv2.json");
//    }

    @Test
    public void testConvertToMixInterpretations_openYouTube() throws IOException {
        testConvertToMixInterpretations("open YouTube.cobra226.json", "open YouTube.mixv2.json");
    }

//    @Test
//    public void testConvertToMixInterpretations_rewindby1hour20minutes35seconds() throws IOException {
//        testConvertToMixInterpretations("rewind by 1 hour 20 minutes 35 seconds.cobra226.json", "rewind by 1 hour 20 minutes 35 seconds.mixv2.json");
//    }

//    @Test
//    public void testConvertToMixInterpretations_rewindonehundredandtwentyseconds() throws IOException {
//        testConvertToMixInterpretations("rewind one hundred and twenty seconds.cobra226.json", "rewind one hundred and twenty seconds.mixv2.json");
//    }

//    @Test
//    public void testConvertToMixInterpretations_showmoviesbyjohnWayneonBBC() throws IOException {
//        testConvertToMixInterpretations("show movies by john Wayne on BBC.cobra226.json", "show movies by john Wayne on BBC.mixv2.json");
//    }

//    @Test
//    public void testConvertToMixInterpretations_showTomHanksmovieson12thofseptember() throws IOException {
//        testConvertToMixInterpretations("show Tom Hanks movies on 12th of september.cobra226.json", "show Tom Hanks movies on 12th of september.mixv2.json");
//    }

//    @Test
//    public void testConvertToMixInterpretations_skipthreehundredandtwentyminutes() throws IOException {
//        testConvertToMixInterpretations("skip three hundred and twenty minutes.cobra226.json", "skip three hundred and twenty minutes.mixv2.json");
//    }
}

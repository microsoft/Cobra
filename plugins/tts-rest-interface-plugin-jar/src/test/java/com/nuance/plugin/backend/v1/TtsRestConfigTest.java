package com.nuance.plugin.backend.v1;

import org.junit.Test;

import static org.junit.Assert.*;

public class TtsRestConfigTest
{

    @Test
    public void generateMarkdownDocs() {
        assertFalse((Boolean) TtsRestConfig.Property.TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getDefaultValue());
        assertEquals("Boolean indicating if simple text should be encapsulated in ssml.",
                TtsRestConfig.Property.TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getDescription());
        assertEquals("TTS_REST_ENCAPSULATE_SIMPLE_TEXT", TtsRestConfig.Property.TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getConstantName());
        assertEquals("ttsRestEncapsulateSimpleText", TtsRestConfig.Property.TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getKey());
    }

}
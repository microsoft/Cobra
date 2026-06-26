package com.nuance.ncs.nmsp.servlet;

import com.nuance.plugin.ncs.utils.ICodecResource;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NcsServletDefinesTest {

    @Test
    public void getCodecMapExposesRegisteredCodecs() {
        Map<String, NcsServletDefines.Pair<ICodecResource.Codec, String>> map = NcsServletDefines.getCodecMap();
        assertNotNull(map);
        assertTrue(map.containsKey("PCM_16_16K"));
        assertEquals(ICodecResource.Codec.PCM16_16K, map.get("PCM_16_16K").codec);
        assertEquals("audio/x-wav;codec=pcm;bit=16;rate=16000", map.get("PCM_16_16K").mimeType);
    }

    @Test
    public void getBodyPartMapResolvesKnownParts() {
        Map<String, NcsServletDefines.BodyPart> map = NcsServletDefines.getBodyPartMap();
        assertNotNull(map);
        assertEquals(NcsServletDefines.BodyPart.PART_NCS_REQUEST_DATA, map.get("requestdata"));
        assertEquals(NcsServletDefines.BodyPart.PART_NCS_AUDIO_PARAMETER, map.get("audioparameter"));
        assertEquals(NcsServletDefines.BodyPart.PART_NCS_TTS_PARAMETER, map.get("ttsparameter"));
    }

    @Test
    public void pairHoldsCodecAndMimeType() {
        NcsServletDefines.Pair<ICodecResource.Codec, String> pair =
                new NcsServletDefines.Pair<>(ICodecResource.Codec.PCM16_8K, "audio/x-wav");
        assertEquals(ICodecResource.Codec.PCM16_8K, pair.codec);
        assertEquals("audio/x-wav", pair.mimeType);
    }
}

package com.nuance.cobra.utils.audio;

import com.nuance.cobra.client.UserResetClients;
import com.nuance.cobra.test.CobraTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpeechDetectorTypesTest extends CobraTest {
    @Test
    public void test_createType() {
        SpeechDetectorTypes type = SpeechDetectorTypes.fromString("engine");
        assertNotNull(type);
        assertEquals(SpeechDetectorTypes.ENGINE.getValue(), type.toString());
        assertTrue(SpeechDetectorTypes.isSpeechDetectorTypes("engine"));
        assertFalse(SpeechDetectorTypes.isSpeechDetectorTypes("nginx"));
    }

    @Test(expected = NullPointerException.class)
    public void test_createType_empty() {
        SpeechDetectorTypes type = SpeechDetectorTypes.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_createType_invalid() {
        SpeechDetectorTypes type = SpeechDetectorTypes.fromString("yahoo");
    }
}
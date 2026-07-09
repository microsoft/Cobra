package com.nuance.cobra.client;

import com.nuance.cobra.utils.audio.AudioChunk;
import com.nuance.cobra.utils.audio.AudioNode;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpeechRecoClientTest {

    @Test
    public void testGetLanguage() {

        SpeechRecoClient testRecoClient = new SpeechRecoClient("sessionID") {
            @Override
            public void startSpeechRecognition() {

            }

            @Override
            public void stopSpeechRecognition() {

            }

            @Override
            public void cancelSpeechRecognition(boolean isFinal) {

            }

            @Override
            public void startRecognitionTimers()
            {

            }

            @Override
            public boolean setRecResultType(RecResultType recResultType) {
                return false;
            }

            @Override
            public float getMaxConfidence() {
                return 0;
            }

            @Override
            public void onAudio(AudioChunk chunk) {

            }

            @Override
            public void finish() {

            }

            @Override
            public void onEvent(AudioNode eventOwner, NodeEvent event) {

            }
        };

        assertNull(testRecoClient.getLanguage());
        testRecoClient.setObservers(null); // For coverage
    }
}
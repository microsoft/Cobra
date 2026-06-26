package com.nuance.cobra.utils.audio;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class ImaAdpcmChunkerNodeTest {

    @Test
    public void onAudio() throws IOException {
        ImaAdpcmChunkerNode node = new ImaAdpcmChunkerNode(16000);

        AudioDestination audioDestination = mock(AudioDestination.class);
        node.connect(audioDestination);

        byte[] actualImaAdpcmAudio = Files.readAllBytes(Paths.get("./src/test/resources/audio/life.imaadpcm"));

        node.onAudio(actualImaAdpcmAudio);

        ArgumentCaptor<AudioChunk> captor = ArgumentCaptor.forClass(AudioChunk.class);
        verify(audioDestination, times(61)).onAudio(captor.capture());
    }
}

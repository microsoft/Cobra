package com.nuance.ncs.nmsp.engine.streamers;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.apache.commons.fileupload.MultipartStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ToGwAudioStreamerTest {

    private List<byte[]> capturedBuffers = new ArrayList<>();

    private PdxAudioSink capturingAudioSink() throws IOException {
        PdxAudioSink sink = mock(PdxAudioSink.class);
        doAnswer(inv -> {
            byte[] buf = inv.getArgument(0);
            if (buf != null) capturedBuffers.add(buf.clone());
            return null;
        }).when(sink).addAudioBuf(any(), anyInt(), anyInt(), anyBoolean());
        return sink;
    }

    @Test
    public void streamWithNullAudioSink() throws IOException {
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(null, ICodecResource.Codec.PCM16_16K);
        streamer.stream(); // should log error and return without NPE
    }

    @Test
    public void initialize() throws IOException {
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        streamer.initialize(null, "audio/ogg");
        assertTrue(streamer.isEncapsulatedOgg());
    }

    @Test
    public void initializeNonOgg() throws IOException {
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        streamer.initialize(null, "audio/x-wav");
        assertFalse(streamer.isEncapsulatedOgg());
    }

    @Test
    public void initializeNullContentType() throws IOException {
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        streamer.initialize(null, null);
        assertFalse(streamer.isEncapsulatedOgg());
    }

    @Test
    public void setAndGetEncapsulatedOgg() throws IOException {
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        streamer.setEncapsulatedOgg(true);
        assertTrue(streamer.isEncapsulatedOgg());
        streamer.setEncapsulatedOgg(false);
        assertFalse(streamer.isEncapsulatedOgg());
    }

    @Test
    public void terminateSendsLastBuffer() throws IOException {
        PdxAudioSink sink = mock(PdxAudioSink.class);
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        streamer.terminate();
        verify(sink).addAudioBuf(null, 0, 0, true);
    }

    @Test
    public void terminateHandlesIOException() throws IOException {
        PdxAudioSink sink = mock(PdxAudioSink.class);
        doThrow(new IOException("test")).when(sink).addAudioBuf(null, 0, 0, true);
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        streamer.terminate(); // should not throw
    }

    @Test
    public void streamPcmSmallAudio() throws IOException {
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        MultipartStream multiStream = mockMultipartWithData(new byte[]{1, 2, 3, 4});
        streamer.initialize(multiStream, "audio/x-wav");
        streamer.stream();
        assertFalse(capturedBuffers.isEmpty());
        assertTrue(streamer.finished);
    }

    @Test
    public void streamPcmLargeAudio() throws IOException {
        // Larger than MAX_BUFFERING_LENGTH = 4*24*1024 = 98304 bytes
        byte[] bigAudio = new byte[200000];
        for (int i = 0; i < bigAudio.length; i++) bigAudio[i] = (byte) (i % 127);

        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.PCM16_16K);
        MultipartStream multiStream = mockMultipartWithData(bigAudio);
        streamer.initialize(multiStream, "audio/x-wav");
        streamer.stream();
        assertTrue(capturedBuffers.size() > 1);
    }

    @Test
    public void streamMpegAudio() throws IOException {
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.MP3_32KBPS);
        MultipartStream multiStream = mockMultipartWithData(new byte[]{0x01, 0x02});
        streamer.initialize(multiStream, "audio/mpeg");
        streamer.stream();
        assertTrue(streamer.finished);
    }

    @Test
    public void streamNonPcmNonMpegAudio() throws IOException {
        // Any codec that is neither PCM nor MPEG — reads all, pushes in one part
        PdxAudioSink sink = capturingAudioSink();
        ToGwAudioStreamer streamer = new ToGwAudioStreamer(sink, ICodecResource.Codec.SPEEX_NB_NR0);
        MultipartStream multiStream = mockMultipartWithData(new byte[]{10, 20, 30});
        streamer.initialize(multiStream, "audio/x-speex");
        streamer.stream();
        assertEquals(1, capturedBuffers.size());
        assertEquals(3, capturedBuffers.get(0).length);
        assertTrue(streamer.finished);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Creates a mock MultipartStream that returns data once and then returns 0.
     */
    private MultipartStream mockMultipartWithData(byte[] data) throws IOException {
        MultipartStream multiStream = mock(MultipartStream.class);
        // First call returns data.length, subsequent calls return 0
        when(multiStream.readBodyData(any())).thenAnswer(inv -> {
            java.io.OutputStream out = inv.getArgument(0);
            if (data.length > 0) {
                out.write(data);
                return data.length;
            }
            return 0;
        }).thenReturn(0);
        return multiStream;
    }
}

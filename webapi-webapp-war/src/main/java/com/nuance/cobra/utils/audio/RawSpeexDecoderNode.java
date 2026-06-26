package com.nuance.cobra.utils.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xiph.speex.NbCodec;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.SbCodec;
import org.xiph.speex.SbEncoder;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

public class RawSpeexDecoderNode
        extends PcmChunkerNode {
    private static final Logger log = LogManager.getLogger(RawSpeexDecoderNode.class);

    /** Defines whether or not the perceptual enhancement is used. */
    private boolean enhanced = true;
    private boolean vbr = false;
    /** If input is raw, defines th number of channels (1=mono, 2=stereo). */
    private int channels = 1;

    /**
     *
     * A speex frame is generally 20ms. We have to call processData() once for each speex frame we pass to it.
     *
     * A Speex frame could actually take 69.5 bytes of space (ex. wideband 16k mono quality 8). In that case, you'd have:
     *      packet with one frame  = 69.5 rounded up     = 70 bytes
     *      packet with two frames = 69.5 * 2            = 139 bytes
     *      packet with 3 frames   = 69.5 * 3 = 139 + 70 = 209 bytes
     *      packet with 4 frames   = 69.5 * 4            = 278 bytes
     *      packet with 5 frames   = 69.5 * 5 = 347.5 rounded up = 348 bytes
     *      etc.
     *
     *
     * 'frameSizeBytes' the number of bytes needed to encapsulate only one frame.
     */
    private int frameSizeBytes = 0;

    private int packetNo = 0;

    private int mode = 0;

    private final org.xiph.speex.SpeexDecoder speexDecoder = new org.xiph.speex.SpeexDecoder();

    public RawSpeexDecoderNode(int mode,
                               int sampleRate,
                               int quality,
                               int nbAudioChannels,
                               boolean enablePerceptualEnhancement) {
        super(sampleRate);

        this.mode = mode;
        channels = nbAudioChannels;
        enhanced = enablePerceptualEnhancement;

        boolean isInitSuccessful = speexDecoder.init(mode, sampleRate, channels, enhanced);

        if (!isInitSuccessful) {
            log.error("Unable to initialize SpeexDecoder correctly. Mode={}, SampleRate={}, NumberOfAudioChannels={}, EnablePerceptualEnhancement={}", mode, sampleRate, channels, enhanced);
            return;
        }

        if (!vbr) {
            frameSizeBytes = getFrameSizeNonVbr(mode, quality);
        } else {
            // We have to read the stream to find out more
            frameSizeBytes = 0;
        }
    }

    public static int getFrameSizeNonVbr(int mode, int quality)
    {
        if(quality < 0)
        {
            return 0;
        }

        int frameSize;
        switch (mode) {
            case 0:
                frameSize = NbCodec.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[quality]];
                break;
            //Wideband
            case 1:
                frameSize = NbCodec.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                frameSize += SbCodec.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                break;
            case 2:
                frameSize = NbCodec.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                frameSize += SbCodec.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                frameSize += SbCodec.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[quality]];
                break;
            //*/
            default:
                log.error("Unable to initialize SpeexDecoder correctly. Illegal mode encountered: {}", mode);
                return -1;
        }
        frameSize = (frameSize + 7) >> 3;
        return frameSize;
    }

    /**
     * 'payload' MUST be ONLY ONE complete Speex packet containing any number of Speex frames.
     */
    @Override
    public void onAudio(byte[] payload) {
        packetNo++;
        log.trace("Decoding Speex packet. Number={}, Length={}", packetNo, payload.length);

        decodeAndWriteFrames(payload);
    }

    private void decodeAndWriteFrames(byte[] payload)
    {
        int nframes = getNFrames(payload.length);
        log.trace("Speex chunk has {} frame(s)", nframes);

        for (int i = 0; i < nframes; i++) {
            try {
                if (i == 0)
                {
                    log.trace("Calling processData with {} bytes", payload.length);
                    speexDecoder.processData(payload, 0, payload.length);
                } else
                {
                    log.trace("Calling processData(false)");
                    speexDecoder.processData(false);
                }
            } catch (Exception e) {
                log.warn("Dropping invalid speex packet! Dropped bytes: {}.", payload.length, e);
            } finally {
                getAndWritePcmData();
            }
        }
    }

    private void getAndWritePcmData()
    {
        int nbBytesProcessedAndReadyToBeRead = speexDecoder.getProcessedDataByteSize();
        log.trace("Extracted PCM bytes {}.", nbBytesProcessedAndReadyToBeRead);

        if (nbBytesProcessedAndReadyToBeRead > 0)
        {
            byte[] decodedPcmData = new byte[nbBytesProcessedAndReadyToBeRead];
            int processedBytes = speexDecoder.getProcessedData(decodedPcmData, 0);
            log.debug("Writing {} PCM bytes", processedBytes);
            super.onAudio(decodedPcmData);
        }
    }

    //Reverses #addNmspNmtSizeHeader, which is done by the NMSP SDK or NMT SDK.
    public static byte[] removeNmspNmtSizeHeaders(byte[] payload)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ByteBuffer bytes = ByteBuffer.wrap(payload);
        while (bytes.hasRemaining())
        {
            int size = 0;
            int partialSize;

            do
            {
                byte sizeByte = bytes.get();
                partialSize = Byte.toUnsignedInt(sizeByte);
                size = size << 7 | (sizeByte & 127);
            } while (partialSize >= 128 && size < 268435456);

            log.trace("Extracting speex frame of size {} from packet", size);
            byte[] speexFrame = new byte[size];
            bytes.get(speexFrame);
            byteArrayOutputStream.writeBytes(speexFrame);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @SuppressWarnings("all")
    int getNFrames(int payloadLength)
    {
        //Frame size based on fixed quality
        if(frameSizeBytes > 0)
        {
            return new BigDecimal(payloadLength).divide(new BigDecimal(frameSizeBytes), RoundingMode.FLOOR).intValue();
        }

        //Try to auto-detect the frame size

        //Quality 10
        float quality10FrameSize = getFrameSizeNonVbr(mode, 10);
        if(payloadLength % quality10FrameSize == 0)
        {
            log.trace("Detected quality 10. Mode = {}", mode);
            return (int) (payloadLength / quality10FrameSize);
        }
        if(mode == 0)
        {
            quality10FrameSize = 61.5f;
            if (payloadLength % quality10FrameSize == 0)
            {
                log.trace("Detected quality 10 - fs 61.5. Mode = {}", mode);
                return (int) (payloadLength / quality10FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 10);
            if (payloadLengthLessOneFrame % quality10FrameSize == 0)
            {
                log.trace("Detected quality 10 - fs 61.5 and 62. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality10FrameSize + 1);
            }
        }

        //Quality 9
        float quality9FrameSize = getFrameSizeNonVbr(mode, 9);
        if(payloadLength % quality9FrameSize == 0)
        {
            log.trace("Detected quality 9. Mode = {}", mode);
            return (int) (payloadLength / quality9FrameSize);
        }
        if(mode == 0)
        {
            quality9FrameSize = 45.5f;
            if (payloadLength % quality9FrameSize == 0)
            {
                log.trace("Detected quality 9 - fs 45.5. Mode = {}", mode);
                return (int) (payloadLength / quality9FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 9);
            if (payloadLengthLessOneFrame % quality9FrameSize == 0)
            {
                log.trace("Detected quality 9 - fs 45.5 and 46. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality9FrameSize + 1);
            }
        }

        //Quality 8
        float quality8FrameSize = getFrameSizeNonVbr(mode, 8);
        if(payloadLength % quality8FrameSize == 0)
        {
            log.trace("Detected quality 8. Mode = {}", mode);
            return (int) (payloadLength / quality8FrameSize);
        }
        if(mode == 0)
        {
            quality8FrameSize = 37.5f;
            if (payloadLength % quality8FrameSize == 0)
            {
                log.trace("Detected quality 7/8 - fs 37.5. Mode = {}", mode);
                return (int) (payloadLength / quality8FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 8);
            if (payloadLengthLessOneFrame % quality8FrameSize == 0)
            {
                log.trace("Detected quality 7/8 - fs 37.5 and 38. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality8FrameSize + 1);
            }
        }
        if(mode == 1)
        {
            quality8FrameSize = 69.5f;
            if (payloadLength % quality8FrameSize == 0)
            {
                log.trace("Detected quality 8 - fs 69.5. Mode = {}", mode);
                return (int) (payloadLength / quality8FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 8);
            if (payloadLengthLessOneFrame % quality8FrameSize == 0)
            {
                log.trace("Detected quality 8 - fs 69.5 and 70. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality8FrameSize + 1);
            }
        }

        //Quality 7
        float quality7FrameSize = getFrameSizeNonVbr(mode, 7);
        if(payloadLength % quality7FrameSize == 0)
        {
            log.trace("Detected quality 7. Mode = {}", mode);
            return (int) (payloadLength / quality7FrameSize);
        }
        if(mode == 1)
        {
            quality7FrameSize = 59.5f;
            if (payloadLength % quality7FrameSize == 0)
            {
                log.trace("Detected quality 7 - fs 59.5. Mode = {}", mode);
                return (int) (payloadLength / quality7FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 7);
            if (payloadLengthLessOneFrame % quality7FrameSize == 0)
            {
                log.trace("Detected quality 7 - fs 59.5 and 60. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality7FrameSize + 1);
            }
        }

        //Quality 6
        float quality6FrameSize = getFrameSizeNonVbr(mode, 6);
        if(payloadLength % quality6FrameSize == 0)
        {
            log.trace("Detected quality 6. Mode = {}", mode);
            return (int) (payloadLength / quality6FrameSize);
        }
        if(mode == 0)
        {
            quality6FrameSize = 27.5f;
            if (payloadLength % quality6FrameSize == 0)
            {
                log.trace("Detected quality 5/6 - fs 27.5. Mode = {}", mode);
                return (int) (payloadLength / quality6FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 6);
            if (payloadLengthLessOneFrame % quality6FrameSize == 0)
            {
                log.trace("Detected quality 5/6 - fs 27.5 and 28. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality6FrameSize + 1);
            }
        }
        if(mode == 1)
        {
            quality6FrameSize = 51.5f;
            if (payloadLength % quality6FrameSize == 0)
            {
                log.trace("Detected quality 6 - fs 51.5. Mode = {}", mode);
                return (int) (payloadLength / quality6FrameSize);
            }
            int payloadLengthLessOneFrame = payloadLength - getFrameSizeNonVbr(mode, 6);
            if (payloadLengthLessOneFrame % quality6FrameSize == 0)
            {
                log.trace("Detected quality 6 - fs 51.5 and 52. Mode = {}", mode);
                return (int) (payloadLengthLessOneFrame / quality6FrameSize + 1);
            }
        }

        return 1;
    }
}

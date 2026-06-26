package com.nuance.nmsp.servlet.audio;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.nmsp.servlet.nmas.audio.JSpeexEnc;
import com.nuance.nmsp.servlet.nmas.audio.OggSpeexHeaderWriter;
import com.nuance.nmsp.servlet.util.HttpUtil;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AudioSinkImpl
        implements PdxAudioSink
{

    private static final Logger log = ESAPI.getLogger(AudioSinkImpl.class);

    private ServletCodecWrapper servletCodec;
    private HttpServletResponse response;
    private boolean convert = false;
    private ByteArrayOutputStream byteArrayOut;
    private final List<AudioSinkListener> audioSinkListenerList =
            Collections.synchronizedList(new ArrayList<>());

    private JSpeexEnc encoder = null;
    private OggSpeexHeaderWriter writer = null;

    public AudioSinkImpl(HttpServletResponse response, boolean convert, ServletCodecWrapper codec)
    {
        servletCodec = codec;
        this.response = response;
        this.convert = convert;

        if (convert)
        {
            byteArrayOut = new ByteArrayOutputStream();
        }
    }

    public static byte[] getSubArray(byte[] array, int offset, int length)
    {
        if (array == null)
        {
            return array;
        }

        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
    }

    public void registerListener(AudioSinkListener audioSinkListener)
    {
        audioSinkListenerList.add(audioSinkListener);
    }

    public List<AudioSinkListener> getListeners()
    {
        return audioSinkListenerList;
    }

    public void removeListener(AudioSinkListener audioSinkListener)
    {
        audioSinkListenerList.remove(audioSinkListener);
    }

    @Override
    public void addAudioBuf(byte[] bufferIn, int offset, int length, boolean isLastBuf)
    {
        try
        {
            ServletOutputStream servletOutputStream = response.getOutputStream();

            ByteArrayInputStream byteArrayIn;
            byte[] buffer = getSubArray(bufferIn, offset, length);
            if (buffer == null)
            {
                if (isLastBuf)
                {
                    log.info(Logger.EVENT_UNSPECIFIED, "Last audio buffer received.");
                    if (convert)
                    {
                        byteArrayIn = new ByteArrayInputStream(byteArrayOut.toByteArray());
                        log.trace(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                                  .getName() + " pcmToWav: length=" +
                                                            byteArrayOut.toByteArray().length);
                        pcmToWav(byteArrayIn, servletOutputStream, byteArrayOut.toByteArray().length);
                    }
                    else
                    {
                        if (servletCodec == ServletCodecWrapper.SPEEX_8K ||
                            servletCodec == ServletCodecWrapper.SPEEX_16K)
                        {
                            closeAndWrite(servletOutputStream);
                        }
                    }

                    for (AudioSinkListener listener : audioSinkListenerList)
                    {
                        if (listener != null)
                        {
                            listener.audioCompleted();
                        }
                    }
                    log.trace(Logger.EVENT_UNSPECIFIED, "audio is sent");
                }
                return;
            }

            log.info(Logger.EVENT_UNSPECIFIED, "addAudioBuf length : " + buffer.length);

            if (convert)
            {
                byteArrayOut.write(HttpUtil.sanitizeAudioBinaryBytes(buffer));
            }
            else
            {
                if (servletCodec == ServletCodecWrapper.SPEEX_8K || servletCodec == ServletCodecWrapper.SPEEX_16K)
                {

                    int sampleRate = servletCodec == ServletCodecWrapper.SPEEX_8K ? 8000 : 16000;
                    int frameSize = sampleRate * 2 / 50;
                    if (encoder == null && writer == null)
                    {
                        encoder = new JSpeexEnc(sampleRate);
                        log.debug(Logger.EVENT_UNSPECIFIED, "frame size:" + frameSize);
                        writer = encoder.generateOggSpeexHeader();
                        servletOutputStream.write(HttpUtil.sanitizeAudioBinaryBytes(writer.getEncodedAudio()));
                    }
                    int numOfFrames = buffer.length / frameSize;
                    for (int i = 0; i < numOfFrames; i++)
                    {
                        byte[] frame = new byte[frameSize];
                        System.arraycopy(buffer, frameSize * i, frame, 0, frameSize);
                        if(encoder != null) encoder.encode(frame, writer);
                        servletOutputStream.write(HttpUtil.sanitizeAudioBinaryBytes(writer.getEncodedAudio()));
                    }
                }
                else
                {
                    servletOutputStream.write(HttpUtil.sanitizeAudioBinaryBytes(buffer));
                }
            }
        }
        catch (Exception e)
        {

            for (AudioSinkListener listener : audioSinkListenerList)
            {
                if (listener != null)
                {
                    listener.audioFailed();
                }
            }
        }
    }

    private void closeAndWrite(ServletOutputStream servletOutputStream)
    {
        try
        {
            writer.close();
            servletOutputStream.write(HttpUtil.sanitizeAudioBinaryBytes(writer.getEncodedAudio()));
            servletOutputStream.close();
        }
        catch (IOException e)
        {
            log.debug(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
        }
    }

    public void pcmToWav(ByteArrayInputStream bais, OutputStream out, long totalByteArray)
    {

        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22000, 16, 1, 2, 22000, false);
        long length = totalByteArray / audioFormat.getFrameSize();

        try (AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length)) {
            log.trace(Logger.EVENT_UNSPECIFIED, "Start convert pcm to wav ...");
            AudioSystem.write(audioInputStreamTemp, AudioFileFormat.Type.WAVE, out);
            log.trace(Logger.EVENT_UNSPECIFIED, "Done convert ...");
        } catch (Exception ex) {
            log.debug(Logger.EVENT_UNSPECIFIED, "Exception  ..." + ex.getMessage(), ex);
        }
    }
}

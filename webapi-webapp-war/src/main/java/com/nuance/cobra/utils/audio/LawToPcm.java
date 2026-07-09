package com.nuance.cobra.utils.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LawToPcm
{
    private static final Logger logger = LogManager.getLogger(LawToPcm.class);

    public static byte[] toPcm(byte[] inputBytes, AudioFormat.Encoding encoding)
    {
        AudioFormat format = new AudioFormat(encoding, 8000, 8,
                1, 1, 8000, false);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputBytes);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try
        {
            int sampleFramesLength = inputBytes.length/format.getFrameSize();
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, format, sampleFramesLength);
            AudioInputStream convertedAudioInputStream = toPcm(format, audioInputStream);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = convertedAudioInputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        }
        catch (IOException e)
        {
            logger.warn("Unable to convert from *law to PCM. IOException: {}", e.getMessage());
        }

        return result.toByteArray();
    }

    private static AudioInputStream toPcm(AudioFormat format, AudioInputStream stream)
    {
        AudioFormat newFormat = new AudioFormat(8000f, 16, 1, true, false);

        return AudioSystem.getAudioInputStream(newFormat, stream);
    }
}

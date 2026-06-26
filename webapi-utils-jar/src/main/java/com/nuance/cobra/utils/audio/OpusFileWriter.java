package com.nuance.cobra.utils.audio;

import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusTags;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/*
Write a list of Opus packets to a file with a valid header. Works for STT, buggy for TTS (beginning of audio gets cut off).
 */

// HOW to use it:
// ----------------------------------------------------
//        if (currentSession.getInputCodec().getEncoding() == AudioCodec.Encoding.OPUS)
//                {
//                WavDumperNode wavDumperNode = new WavDumperNode(nimConnection.getInputCodec().getSampleRate(),"", session, edfEnabledOptional.get(), false);
//                wavDumperNode.makePath();
//                String path = wavDumperNode.getFolderPath();
//                OpusFileWriter opusFileWriter = new OpusFileWriter(path+"/opusSTT_"+sessionId
//                +"_"+session.stepId+".opus");
//                opusFileWriter.writeWithLibrary(opusAudioData);
//                }

public class OpusFileWriter
{
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(OpusFileWriter.class);
    private static final long SAMPLE_RATE = 48000; //for the purposes of writing to a file, should use 48khz
    private static final int MS_CONSTANT = 120;
    private static final long PRE_SKIP = (SAMPLE_RATE * MS_CONSTANT) / 1000;
    private static final String VENDOR = "Nuance Communications Inc. , WebAPI v2";
    private OpusInfo opusInfo;
    private OpusTags opusTags;

    private OutputStream opusFileStream;

    public OpusFileWriter(OutputStream opusFileStream)
    {
        this.opusFileStream = opusFileStream;

        opusInfo = new OpusInfo();
        opusInfo.setNumChannels(1);
        opusInfo.setOutputGain(0);
        opusInfo.setPreSkip((int)PRE_SKIP);
        opusInfo.setSampleRate(SAMPLE_RATE);

        opusTags = new OpusTags();
        List<String> bitrate = new ArrayList<>();
        bitrate.add(" --hard-cbr --comp 10 --bitrate 28000");
        opusTags.setComments("ENCODER_OPTIONS", bitrate);
        opusTags.setVendor(VENDOR);
    }

    public void writeWithLibrary(List<OpusAudioData> opusAudioData)
    {
        int loc = 0;
        OpusFile opusFile = new OpusFile(opusFileStream,opusInfo,opusTags);

        //writeAudioData only buffers data packets (and handles relevant opus stuff) to be written
        for (OpusAudioData o : opusAudioData)
        {

            try
            {
                if (o!=null)
                {
                    o.setGranulePosition(loc);
                    loc += o.getNumberOfSamples();
                    opusFile.writeAudioData(o);
                }
            }
            catch (Exception e)
            {
                logger.error("OpusFileWriter -- Could not write opus audio data to opus file", e);
            }

        }

        //This is the part that actually writes the data
        try
        {
            opusFile.close();
        }
        catch (IOException e)
        {
            logger.error("OpusFileWriter -- Could not write data to OpusFile, aborting", e);
        }
    }
}

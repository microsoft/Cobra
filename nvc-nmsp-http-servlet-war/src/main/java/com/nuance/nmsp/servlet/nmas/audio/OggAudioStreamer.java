package com.nuance.nmsp.servlet.nmas.audio;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.xiph.speex.OggCrc;

import javax.servlet.http.HttpServletRequest;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Streams OGG/Speex audio sent by the client but removes the OGG header first (not supported by the NMSP Gateway).
 */
public class OggAudioStreamer
        extends AudioStreamer
{


    private static final Logger log = ESAPI.getLogger(OggAudioStreamer.class);


    private static final int OGG_HEADERSIZE = 27;
    private static final int OGG_SEGOFFSET = 26;
    static final String OGGID = "OggS";

    private int mode = 0;
    private int nframes = 1;
    private int sampleRate = -1;
    private int channels = 1;


    public OggAudioStreamer(HttpServletRequest request, PdxAudioSink audioSink)
            throws IOException
    {
        super(request, audioSink);
    }

    private static int readInt(final byte[] data, final int offset)
    {
        return (data[offset] & 0xff) |
               ((data[offset + 1] & 0xff) << 8) |
               ((data[offset + 2] & 0xff) << 16) |
               (data[offset + 3] << 24);
    }

    @Override
    public synchronized void run()
    {
        try
        {
            readOGG();
            super.finished = true;
            notifyAll();
        }
        catch (Exception e)
        {
            log.info(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
        }
    }

    public void readOGG()
            throws IOException
    {
        int origchksum;
        int chksum;

        DataInputStream dis = new DataInputStream(inputStream);

        int packetNo = 0;
        while (true)
        {
            int segments = 0;
            int curseg = 0;
            int bodybytes = 0;

            byte[] header = new byte[2048];
            byte[] payload;

            try
            {
                dis.readFully(header, 0, OGG_HEADERSIZE);
            }
            catch (Exception e)
            {
                audioSink.addAudioBuf(null, 0, 0, true);
                return;
            }
            origchksum = readInt(header, 22);
            header[22] = 0;
            header[23] = 0;
            header[24] = 0;
            header[25] = 0;
            chksum = OggCrc.checksum(0, header, 0, OGG_HEADERSIZE);

            if (!OGGID.equals(new String(header, 0, 4)))
            {
                log.error(Logger.EVENT_UNSPECIFIED, "missing ogg id!");
                audioSink.addAudioBuf(null, 0, 0, true);
                return;
            }

            segments = header[OGG_SEGOFFSET] & 0xFF;

            try
            {
                dis.readFully(header, OGG_HEADERSIZE, segments);
            }
            catch (IOException e)
            {
                audioSink.addAudioBuf(null, 0, 0, true);
                return;
            }
            chksum = OggCrc.checksum(chksum, header, OGG_HEADERSIZE, segments);

            for (curseg = 0; curseg < segments; curseg++)
            {
                bodybytes = header[OGG_HEADERSIZE + curseg] & 0xFF;
                if (bodybytes == 255)
                {
                    log.error(Logger.EVENT_UNSPECIFIED, "sorry, don't handle 255 sizes!");
                    audioSink.addAudioBuf(null, 0, 0, true);
                    return;
                }
                try
                {
                    payload = new byte[bodybytes];
                    dis.readFully(payload, 0, bodybytes);
                }
                catch (IOException e)
                {
                    audioSink.addAudioBuf(null, 0, 0, true);
                    return;
                }
                chksum = OggCrc.checksum(chksum, payload, 0, bodybytes);

                if (packetNo == 0)
                {
                    if (readSpeexHeader(payload, 0, bodybytes))
                    {

                        if (log.isDebugEnabled())
                        {
                            log.debug(Logger.EVENT_UNSPECIFIED, "File Format: Ogg Speex");
                            log.debug(Logger.EVENT_UNSPECIFIED, "Sample Rate: " + sampleRate);
                            log.debug(Logger.EVENT_UNSPECIFIED, "Channels: " + channels);
                            log.debug(Logger.EVENT_UNSPECIFIED, "Encoder mode: " + modeToString(mode));
                            log.debug(Logger.EVENT_UNSPECIFIED, "Frames per packet: " + nframes);
                        }

                        packetNo++;
                    }
                    else
                    {
                        packetNo = 0;
                    }
                }
                else if (packetNo == 1)
                {
                    packetNo++;
                }
                else
                {
                    audioSink.addAudioBuf(payload, 0, payload.length, false);
                    packetNo++;
                }
            }
            if (chksum != origchksum)
            {
                log.error(Logger.EVENT_UNSPECIFIED, "Ogg CheckSums do not match");
                audioSink.addAudioBuf(null, 0, 0, true);
                return;
            }
        }
    }

    private String modeToString(int mode)
    {
        if(mode == 0) return "Narrowband";
        if(mode == 1) return "Wideband";
        return "UltraWideband";
    }

    private boolean readSpeexHeader(final byte[] packet,
            final int offset,
            final int bytes)
    {
        if (bytes != 80)
        {
            return false;
        }
        if (!"Speex   ".equals(new String(packet, offset, 8)))
        {
            return false;
        }
        mode = packet[40 + offset] & 0xFF;
        sampleRate = readInt(packet, offset + 36);
        channels = readInt(packet, offset + 48);
        nframes = readInt(packet, offset + 64);

        return true;
    }

}

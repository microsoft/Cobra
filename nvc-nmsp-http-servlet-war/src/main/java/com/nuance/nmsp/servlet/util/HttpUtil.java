package com.nuance.nmsp.servlet.util;

import com.google.common.io.BaseEncoding;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.nmas.AsrContext;
import com.nuance.nmsp.servlet.nmas.audio.AudioStreamer;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings({"java:S1444", "java:S1104", "java:S3008"})
public final class HttpUtil
{

    /**
     * HTTP header definitions
     */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String CONTENT_LEN = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LANGUAGE = "Content-Language";
    public static final String CONTENT_VOICE = "Content-Voice";
    public static final String SESSIONID_HEADER = "x-nuance-sessionid";
    public static final String NUANCE_SESSIONID_HEADER = "Nuance-SessionId";
    public static final String NUANCE_CONTEXT_HEADER = "Nuance-Context";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String ACCEPT_TOPIC = "Accept-Topic";
    public static final String AUTHORIZATION = "Authorization";
    /**
     * Transfer encoding definitions
     */
    public static final String CHUNK_CODING = "chunked";
    /**
     * Common charset definitions
     */
    public static final String UTF_8 = "UTF-8";
    public static final String UTF_16 = "UTF-16";
    public static final String US_ASCII = "US-ASCII";
    public static final String ASCII = "ASCII";
    public static final String ISO_8859_1 = "ISO-8859-1";
    /**
     * Audio file type
     */
    public static final String SPX = ".spx";
    public static final String PCM = ".pcm";
    public static final String MP3 = ".mp3";
    public static final String QCP = ".qcp";
    public static final String EVRC = ".evrc";
    public static final String AMR = ".amr";
    public static final String WAV = ".wav";
    /**
     * Language
     */
    public static final String DEFAULT_LANGUAGE = "enus";
    /**
     * Voice
     */
    public static final String DEFAULT_VOICE = "Samantha";
    /**
     * Topics
     */
    public static final String TOPIC_DICTATION = "Dictation";
    public static final String TOPIC_WEBSEARCH = "WebSearch";
    public static final String TOPIC_AUTOMOTIVE_DICTATION = "Automotive-Dictation";
    public static final String TOPIC_AUTOMOTIVE_WEBSEARCH = "Automotive-WebSearch";
    public static final String TOPIC_DTV_SEARCH = "DTV-Search";
    public static final String TOPIC_DTV = "DTV";
    public static final String DEFAULT_TOPIC = TOPIC_DICTATION;
    private static final Logger log = ESAPI.getLogger(HttpUtil.class);
    // URL parameters
    private static final String CODEC = "codec";
    /**
     * Standard mime type definitions
     */
    public static MimeTypeWrapper OCTET_STREAM_TYPE;
    public static MimeTypeWrapper XML_TYPE;
    public static MimeTypeWrapper JSON_TYPE;
    public static MimeTypeWrapper PLAIN_TEXT_TYPE;
    /**
     * TTS mime type
     */

    public static MimeTypeWrapper SSML_TYPE;
    public static MimeTypeWrapper URL_TYPE; // e.g. message/external-body;access-type=URL;URL="http://www.foo.com/file"
    /**
     * Audio mime type
     */
    public static MimeTypeWrapper WAV_TYPE;
    public static MimeTypeWrapper PCM_16BIT_8KHZ_TYPE;
    public static MimeTypeWrapper PCM_16BIT_11KHZ_TYPE;
    public static MimeTypeWrapper PCM_16BIT_16KHZ_TYPE;
    public static MimeTypeWrapper PCM_16BIT_22KHZ_TYPE;
    public static MimeTypeWrapper SPEEX_TYPE;
    public static MimeTypeWrapper SPEEX_NARROWBAND_TYPE;
    public static MimeTypeWrapper SPEEX_WIDEBAND_TYPE;
    public static MimeTypeWrapper AMR_TYPE;
    public static MimeTypeWrapper QCELP_TYPE;
    public static MimeTypeWrapper EVRC_TYPE;
    public static MimeTypeWrapper MP3_TYPE;
    public static MimeTypeWrapper MP3_8KBPS_TYPE;
    public static MimeTypeWrapper MP3_16KBPS_TYPE;
    public static MimeTypeWrapper MP3_24KBPS_TYPE;
    public static MimeTypeWrapper MP3_32KBPS_TYPE;
    public static MimeTypeWrapper MP3_40KBPS_TYPE;
    public static MimeTypeWrapper MP3_48KBPS_TYPE;
    public static MimeTypeWrapper MP3_56KBPS_TYPE;
    public static MimeTypeWrapper MP3_64KBPS_TYPE;
    public static MimeTypeWrapper MP3_80KBPS_TYPE;
    public static MimeTypeWrapper MP3_96KBPS_TYPE;
    public static MimeTypeWrapper MP3_112KBPS_TYPE;
    public static MimeTypeWrapper MP3_128KBPS_TYPE;
    public static MimeTypeWrapper MP3_144KBPS_TYPE;
    public static MimeTypeWrapper MP3_160KBPS_TYPE;

    /** Static block to initialized all the mime types */

    static
    {
        try
        {
            WAV_TYPE = new MimeTypeWrapper("audio/x-wav");
            PCM_16BIT_8KHZ_TYPE = new MimeTypeWrapper("audio/x-wav;codec=pcm;bit=16;rate=8000");
            PCM_16BIT_11KHZ_TYPE = new MimeTypeWrapper("audio/x-wav;codec=pcm;bit=16;rate=11025");
            PCM_16BIT_16KHZ_TYPE = new MimeTypeWrapper("audio/x-wav;codec=pcm;bit=16;rate=16000");
            PCM_16BIT_22KHZ_TYPE = new MimeTypeWrapper("audio/x-wav;codec=pcm;bit=16;rate=22000");
            SPEEX_TYPE = new MimeTypeWrapper("audio/x-speex");
            SPEEX_NARROWBAND_TYPE = new MimeTypeWrapper("audio/x-speex;rate=8000");
            SPEEX_WIDEBAND_TYPE = new MimeTypeWrapper("audio/x-speex;rate=16000");
            AMR_TYPE = new MimeTypeWrapper("audio/amr");
            QCELP_TYPE = new MimeTypeWrapper("audio/qcelp");
            EVRC_TYPE = new MimeTypeWrapper("audio/evrc");
            MP3_TYPE = new MimeTypeWrapper("audio/mpeg");
            MP3_8KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=8");
            MP3_16KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=16");
            MP3_24KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=24");
            MP3_32KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=32");
            MP3_40KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=40");
            MP3_48KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=48");
            MP3_56KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=56");
            MP3_64KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=64");
            MP3_80KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=80");
            MP3_96KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=96");
            MP3_112KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=112");
            MP3_128KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=128");
            MP3_144KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=144");
            MP3_160KBPS_TYPE = new MimeTypeWrapper("audio/mpeg;bitrate=160");
            OCTET_STREAM_TYPE = new MimeTypeWrapper("application/octet-stream");
            XML_TYPE = new MimeTypeWrapper("application/xml");
            JSON_TYPE = new MimeTypeWrapper("application/json");
            PLAIN_TEXT_TYPE = new MimeTypeWrapper("text/plain");
            SSML_TYPE = new MimeTypeWrapper("application/ssml+xml");
            URL_TYPE = new MimeTypeWrapper("message/external-body");

        }
        catch (MimeTypeParseException e)
        {
            log.warning(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
        }
    }

    /**
     * Converts a Codec into mime type
     *
     * @param codec
     * @return mime type
     */
    public static String codecToMimeType(ServletCodecWrapper codec)
    {
        if (codec == null)
        {
            return null;
        }

        log.debug(Logger.EVENT_UNSPECIFIED, "codecToMimeType for:" + codec.getValue());

        if (codec == ServletCodecWrapper.PCM_16_11K)
        {
            return PCM_16BIT_11KHZ_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.PCM_16_8K)
        {
            return PCM_16BIT_8KHZ_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.PCM_16_16K)
        {
            return PCM_16BIT_16KHZ_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.PCM_16_22K)
        {
            return PCM_16BIT_22KHZ_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.WAV)
        {
            return WAV_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.EVRC)
        {
            return EVRC_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.QCELP13)
        {
            return QCELP_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.SPEEX_8K)
        {
            return SPEEX_NARROWBAND_TYPE.getRawString();
        }
        else if (codec == ServletCodecWrapper.SPEEX_16K)
        {
            return SPEEX_WIDEBAND_TYPE.getRawString();
        }
        else if (codec.isAMR())
        {
            return AMR_TYPE.getRawString();
        }

        return null;
    }

    /**
     * Returns the file type associated with a codec
     *
     * @param codec
     * @return file type
     */
    public static String codecToFileType(ServletCodecWrapper codec)
    {

        if (codec == null)
        {
            return null;
        }

        log.debug(Logger.EVENT_UNSPECIFIED, "codecToFileType for:" + codec.getValue());

        if (codec.isPCM())
        {
            return PCM;
        }
        else if (codec == ServletCodecWrapper.WAV)
        {
            return WAV;
        }
        else if (codec == ServletCodecWrapper.EVRC)
        {
            return EVRC;
        }
        else if (codec == ServletCodecWrapper.QCELP13)
        {
            return QCP;
        }
        else if (codec.isSpeex())
        {
            return SPX;
        }
        else if (codec.isAMR())
        {
            return AMR;
        }

        return ".bin";
    }

    /**
     * Convert customized codec string to standard Mime type for further processing
     */
    public static MimeTypeWrapper stringToMimeType(String codecString)
    {
        if (codecString == null)
        {
            return null;
        }

        if (codecString.equalsIgnoreCase("pcm_16bit_8k"))
        {
            return PCM_16BIT_8KHZ_TYPE;
        }
        else if (codecString.equalsIgnoreCase("pcm_16bit_11k"))
        {
            return PCM_16BIT_11KHZ_TYPE;
        }
        else if (codecString.equalsIgnoreCase("pcm_16bit_16k"))
        {
            return PCM_16BIT_16KHZ_TYPE;
        }
        else if (codecString.equalsIgnoreCase("pcm_16bit_22k"))
        {
            return PCM_16BIT_22KHZ_TYPE;
        }
        else if (codecString.equalsIgnoreCase("wav"))
        {
            return WAV_TYPE;
        }
        else if (codecString.equalsIgnoreCase("speex_nb"))
        {
            return SPEEX_NARROWBAND_TYPE;
        }
        else if (codecString.equalsIgnoreCase("speex_wb"))
        {
            return SPEEX_WIDEBAND_TYPE;
        }
        else if (codecString.equalsIgnoreCase("amr"))
        {
            return AMR_TYPE;
        }
        else if (codecString.equalsIgnoreCase("qcelp"))
        {
            return QCELP_TYPE;
        }
        else if (codecString.equalsIgnoreCase("evrc"))
        {
            return EVRC_TYPE;
        }
        else if (codecString.equalsIgnoreCase("mp3"))
        {
            return EVRC_TYPE;
        }
        return null;
    }

    public static ServletCodecWrapper mimeTypeToCodec(MimeTypeWrapper mimeTypeWrapper)
    {
        if (mimeTypeWrapper == null)
        {
            return null;
        }

        MimeType mimeType = mimeTypeWrapper.getMimeType();

        String bitrate = "bitrate";

        ServletCodecWrapper codec = null;

        if (mimeType.match(WAV_TYPE.getMimeType()))
        {
            if (mimeType.getParameters()
                        .size() == 0)
            {
                codec = ServletCodecWrapper.WAV;
            }
            else if (mimeType.getParameter("bit") != null &&
                     mimeType.getParameter("bit").equals("16") &&
                     mimeType.getParameter("rate") != null)
            {
                if (mimeType.getParameter("rate").equals("8000"))
                {
                    codec = ServletCodecWrapper.PCM_16_8K;
                }
                else if (mimeType.getParameter("rate").equals("16000"))
                {
                    codec = ServletCodecWrapper.PCM_16_16K;
                }
                else if (mimeType.getParameter("rate").equals("11025"))
                {
                    codec = ServletCodecWrapper.PCM_16_11K;
                }
                else if (mimeType.getParameter("rate").equals("22000"))
                {
                    codec = ServletCodecWrapper.PCM_16_22K;
                }
            }
        }
        else if (mimeType.match(SPEEX_TYPE.getMimeType()))
        {
            if (mimeType.getParameters()
                        .size() == 0)
            {
                codec = ServletCodecWrapper.SPEEX_8K;
            }
            else if (mimeType.getParameter("rate") != null)
            {
                if (mimeType.getParameter("rate").equals("8000"))
                {
                    codec = ServletCodecWrapper.SPEEX_8K;
                }
                else if (mimeType.getParameter("rate").equals("16000"))
                {
                    codec = ServletCodecWrapper.SPEEX_16K;
                }
                else if (mimeType.getParameter("rate").equals("11025"))
                {
                    codec = ServletCodecWrapper.SPEEX_11K;
                }
            }
        }
        else if (mimeType.match(AMR_TYPE.getMimeType()))
        {
            codec = ServletCodecWrapper.AMR_07;
        }
        else if (mimeType.match(QCELP_TYPE.getMimeType()))
        {
            codec = ServletCodecWrapper.QCELP13;
        }
        else if (mimeType.match(EVRC_TYPE.getMimeType()))
        {
            codec = ServletCodecWrapper.EVRC;
        }
        else if (mimeType.match(MP3_TYPE.getMimeType()))
        {
            if (mimeType.getParameters().size() == 0)
            {
                codec = ServletCodecWrapper.MP3;
            }
            else if (mimeType.getParameter(bitrate) != null)
            {
                if (mimeType.getParameter(bitrate).matches("8(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_8KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("16(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_16KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("24(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_24KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("32(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_32KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("40(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_40KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("48(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_48KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("56(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_56KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("64(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_64KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("80(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_80KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("96(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_96KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("112(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_112KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("128(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_128KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("144(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_144KBPS;
                }
                else if (mimeType.getParameter(bitrate).matches("160(kbps)?"))
                {
                    codec = ServletCodecWrapper.MP3_160KBPS;
                }
            }
        }
        return codec;
    }

    public static ServletCodecWrapper getCodec(HttpServletRequest request, String header)
    {

        String codecString = null;
        MimeTypeWrapper mimeTypeWrapper = null;

        if ((codecString = HttpUtil.getHeader(request, header)) != null &&
            MethodType.valueOf(request.getMethod()) == MethodType.POST)
        {
            log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                     .getName() + " " + header + ":" + codecString);
            try
            {
                mimeTypeWrapper = new MimeTypeWrapper(codecString);
            }
            catch (MimeTypeParseException e)
            {
                log.error(Logger.EVENT_UNSPECIFIED, "Invalid codec: " + codecString);
                return null;
            }
        }
        else if ((codecString = getSanitizedParameter(request, CODEC)) != null)
        {
            log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                     .getName() + " mime type: " + codecString);
            mimeTypeWrapper = stringToMimeType(codecString);
        }
        else
        {
            return ServletCodecWrapper.DEFAULT;
        }

        return mimeTypeToCodec(mimeTypeWrapper);
    }

    /**
     * Gets Content-Disposition based on the Codec
     *
     * @param codec
     * @return
     */
    public static String getContentDisposition(ServletCodecWrapper codec)
    {
        String contentDisposition;
        if (codec == ServletCodecWrapper.WAV)
        {
            contentDisposition = "filename=\"" + System.currentTimeMillis() + ".wav" + "\";";
        }
        else
        {
            contentDisposition = "attachment;filename=\"" + System.currentTimeMillis() + codecToFileType(codec) + "\";";
        }
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                 .getName() + " " + CONTENT_DISPOSITION + ":" + contentDisposition);
        return contentDisposition;
    }

    /**
     * Gets the content language from HTTP request
     *
     * @param request
     * @return
     */
    public static String getContentLanguage(HttpServletRequest request)
    {
        return getLanguage(request, HttpUtil.CONTENT_LANGUAGE);
    }

    /**
     * Gets the accept language from HTTP request
     *
     * @param request
     * @return
     */
    public static String getAcceptLanguage(HttpServletRequest request)
    {
        return getLanguage(request, HttpUtil.ACCEPT_LANGUAGE);
    }

    /**
     * Gets the language from an HTTP request header
     *
     * @param request
     * @param header
     * @return
     */
    public static String getLanguage(HttpServletRequest request, String header)
    {
        String lang;
        if ( (lang = getSanitizedParameter(request, "lang")) == null &&
             (lang = HttpUtil.getSanitizedHeader(request, header)) == null
           )
        {
            lang = HttpUtil.DEFAULT_LANGUAGE;
        }

        return lang;
    }

    /**
     * Gets the voice from an HTTP request header
     *
     * @param request
     * @return
     */

    public static String getVoice(HttpServletRequest request)
    {
        String voice = null;
        try
        {
            if ((voice = getSanitizedParameter(request, "voice")) == null)
            {
                voice = HttpUtil.getSanitizedHeader(request, HttpUtil.CONTENT_VOICE);
            }
        }
        catch (Exception e)
        {
            voice = null;
        }
        return voice;
    }

    public static String getAccept(HttpServletRequest request)
    {
        return HttpUtil.getSanitizedHeader(request, HttpUtil.ACCEPT);
    }

    public static String getTopic(HttpServletRequest request)
    {
        String topic = null;
        if ( (topic = getSanitizedParameter(request, "topic")) == null &&
             (topic = HttpUtil.getSanitizedHeader(request, HttpUtil.ACCEPT_TOPIC)) == null
           )
        {
            topic = HttpUtil.DEFAULT_TOPIC;
        }
        return topic;
    }

    public static boolean isValidTopic(String topic)
    {
        if (topic == null)
        {
            return false;
        }
        /* Restrict the types of topic */
        return topic.equalsIgnoreCase(TOPIC_DICTATION) ||
               topic.equalsIgnoreCase(TOPIC_WEBSEARCH) ||
               topic.equalsIgnoreCase(TOPIC_AUTOMOTIVE_DICTATION) ||
               topic.equalsIgnoreCase(TOPIC_AUTOMOTIVE_WEBSEARCH) ||
               topic.equalsIgnoreCase(TOPIC_DTV_SEARCH) ||
               topic.equalsIgnoreCase(TOPIC_DTV);
    }

    //Only use for audio (binary) bytes
    public static byte[] sanitizeAudioBinaryBytes(byte[] bytes)
    {
        return sanitizeAudioBinaryBytesBase64(bytes);
    }

    @SuppressWarnings("java:S4738")
    public static byte[] sanitizeAudioBinaryBytesBase64(byte[] bytes)
    {
        String safeString = ESAPI.encoder()
                                 .encodeForBase64(bytes, false);
        return BaseEncoding.base64()
                           .decode(safeString);
    }

    public static byte[] sanitizeAudioBinaryBytesNoOp(byte[] bytes)
    {
        return bytes;
    }

    public static String getParameter(HttpServletRequest request, String parameter)
    {
        return request.getParameter(parameter);
    }

    public static String getSanitizedParameter(HttpServletRequest request, String parameter)
    {
        return ESAPI.encoder()
                    .encodeForHTML(request.getParameter(parameter));
    }

    public static String getHeader(HttpServletRequest request, String header)
    {
        return request.getHeader(header);
    }

    public static String getSanitizedHeader(HttpServletRequest request, String header)
    {
        return ESAPI.encoder()
                    .encodeForHTML(request.getHeader(header));
    }

    public static AsrContext parseAsrContext(HttpServletRequest request)
    {
        AsrContext dictationContext = new AsrContext();

        //Retrieve Audio Source from optional header
        dictationContext.setAudioSource(HttpUtil.getSanitizedHeader(request, "X-Dictation-AudioSource"));

        String tempContext = HttpUtil.getSanitizedHeader(request, "X-Dictation-CursorStart");
        if (tempContext != null)
        {
            dictationContext.setStart(tempContext);
        }
        tempContext = HttpUtil.getSanitizedHeader(request, "X-Dictation-CursorEnd");
        if (tempContext != null)
        {
            dictationContext.setEnd(tempContext);
        }
        tempContext = HttpUtil.getSanitizedHeader(request, "X-Dictation-Buffer");
        if (tempContext != null)
        {
            dictationContext.setText(tempContext);
        }
        tempContext = HttpUtil.getSanitizedHeader(request, "X-Dictation-LogInfo");
        if (tempContext != null)
        {
            dictationContext.setLogInfo(tempContext);
        }

        tempContext = HttpUtil.getSanitizedHeader(request, "X-Dictation-EscapeNewLine");
        if (tempContext != null && tempContext.equalsIgnoreCase("true")) {
            dictationContext.setEscapeNewLines(true);
        }

        tempContext = HttpUtil.getSanitizedHeader(request, "X-Dictation-NBestListSize");
        if (tempContext != null)
        {
            try
            {
                dictationContext.setnBestSize(Integer.parseInt(tempContext));
            }
            catch (Exception e)
            {
                log.debug(Logger.EVENT_UNSPECIFIED, "Failed to parse integer.");
            }
        }

        log.debug(Logger.EVENT_UNSPECIFIED, "Context start cursor: " + dictationContext.getStart());
        log.debug(Logger.EVENT_UNSPECIFIED, "Context end cursor: " + dictationContext.getEnd());
        log.debug(Logger.EVENT_UNSPECIFIED, "Context Text: " + dictationContext.getText());

        return dictationContext;
    }

    private static void cleanupTransactionInternal(Transaction transaction, HttpServletResponse response)
    {
        if (!transaction.isRequestCompleted())
        {
            String sessionID = transaction.getSessionId();
            String errorMsg = (sessionID != null) ? HttpUtil.SESSIONID_HEADER + sessionID + "\n" : "";
            errorMsg = errorMsg + transaction.getError();
            if (transaction.getErrorID() > 0)
            {
                sendError(response, transaction.getErrorID(), errorMsg);
            }
            else
            {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
            }
        }
    }

    public static void cleanupTransaction(Transaction transaction, HttpServletResponse response)
    {
        if (transaction == null)
        {
            return;
        }
        cleanupTransactionInternal(transaction, response);
        transaction.cleanup();
    }

    public static void cleanupTransactionAudioStreamer(Transaction transaction, HttpServletResponse response, AudioStreamer audioStreamer)
    {
        if (transaction == null)
        {
            return;
        }
        cleanupTransactionInternal(transaction, response);
        if (audioStreamer != null) audioStreamer.waitAudioStreamingFinished();
        transaction.cleanup();
    }

    /**
     * Sends http status error
     *
     * @param response
     * @param status
     * @param errorMsg
     * @throws IOException
     */
    public static void sendError(HttpServletResponse response, int status, String errorMsg)
    {
        log.error(Logger.EVENT_UNSPECIFIED, Thread.currentThread().getName() + " " + errorMsg);
        try
        {
            response.sendError(status);
        }
        catch (Exception e)
        {
            log.warning(Logger.EVENT_UNSPECIFIED, "Error while sending error", e);
        }
    }


    public enum MethodType
    {
        GET,
        POST
    }

}


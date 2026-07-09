package com.nuance.nmsp.servlet.nmas.tts;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.nmsp.servlet.util.HttpUtil;
import com.nuance.nmsp.servlet.util.HttpUtil.MethodType;
import org.apache.commons.io.IOUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TTSContent
{

    private static final Logger log = ESAPI.getLogger(TTSContent.class);

    private final String ttsType;
    private String ttsInput;
    private String ttsVoice = null;

    protected TTSContent(HttpServletRequest request)
            throws IOException
    {
        if (MethodType.valueOf(request.getMethod()) == MethodType.POST)
        {
            String contentTypeStr = request.getContentType();
            if (contentTypeStr == null)
            {
                throw new IOException("Content type is not specified.");
            }

            MimeType contentType;
            try
            {
                contentType = new MimeType(contentTypeStr);
            }
            catch (MimeTypeParseException e)
            {
                throw new IOException("Content type is invalid.");
            }
            log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                           .getName() + " Content-Type:" + contentType);

            if (contentType.match(HttpUtil.PLAIN_TEXT_TYPE.getMimeType()))
            {
                ttsType = "text";

                ttsInput = readPostRequestBody(request);

            }
            else if (contentType.match(HttpUtil.URL_TYPE.getMimeType()))
            {
                ttsInput = contentType.getParameter("URL");
                if (ttsInput == null)
                {
                    throw new IOException("Content type is invalid, url is missing.");
                }
                ttsType = "uri";

                ttsInput = readPostRequestBody(request);

            }
            else if (contentType.match(HttpUtil.SSML_TYPE.getMimeType()))
            {
                ttsType = "ssml";
            }
            else
            {
                throw new IOException("Content type is invalid.");
            }

        }
        else
        {
            if ((ttsInput = HttpUtil.getSanitizedParameter(request, "text")) != null)
            {
                ttsType = "text";
            }
            else if ((ttsInput = HttpUtil.getSanitizedParameter(request, "uri")) != null)
            {
                ttsType = "uri";
            }
            else if ((ttsInput = HttpUtil.getSanitizedParameter(request, "ssml")) != null)
            {
                ttsType = "ssml";
            }
            else
            {
                throw new IllegalArgumentException("Text to synthesized is missing.");
            }
        }

        ttsVoice = HttpUtil.getVoice(request);
    }

    private String readPostRequestBody(HttpServletRequest request)
            throws IOException
    {
        return IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
    }

    protected String getTTSType()
    {
        return ttsType;
    }

    protected String getTTSInput()
    {
        return ttsInput;
    }

    protected String getTTSVoice()
    {
        return ttsVoice;
    }

    public NmspDictionary getDictionary()
    {
        NmspDictionary dictionaryTtsParam = new NmspDictionaryImpl();
        dictionaryTtsParam.addUTF8String("tts_type", getTTSType());
        dictionaryTtsParam.addUTF8String("tts_input", getTTSInput());
        return dictionaryTtsParam;
    }
}

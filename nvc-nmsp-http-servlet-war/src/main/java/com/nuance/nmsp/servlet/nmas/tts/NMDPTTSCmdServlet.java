package com.nuance.nmsp.servlet.nmas.tts;

import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.AuthorizationService;
import com.nuance.nmsp.servlet.NMSPServlet;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.audio.AudioSinkFactory;
import com.nuance.nmsp.servlet.audio.AudioSinkImpl;
import com.nuance.nmsp.servlet.util.EnvUtil;
import com.nuance.nmsp.servlet.util.HttpUtil;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static com.nuance.nmsp.servlet.util.HttpUtil.NUANCE_CONTEXT_HEADER;

public class NMDPTTSCmdServlet
        extends NMSPServlet
{

    private static final long serialVersionUID = 1L;

    private static final Logger log = ESAPI.getLogger(NMDPTTSCmdServlet.class);

    private final String nmdpCommand = EnvUtil.useDefault("TTS_COMMAND_NAME", "NMDP_TTS_CMD");
    private final String appVersion = EnvUtil.useDefault("TTS_COMMAND_VERSION", "0.1");
    private final String pdxTTS = EnvUtil.useDefault("TTS_QUERY_PARAM", "TEXT_TO_READ");

    private final AuthorizationService authorizationService = new AuthorizationService();

    private final AudioSinkFactory audioSinkFactory = new AudioSinkFactory();

    public NMDPTTSCmdServlet()
    {
        super();
    }

    @Override
    public void init()
            throws ServletException
    {
        super.init();
        log.info(Logger.EVENT_UNSPECIFIED, "init() servlet completed " + NMDPTTSCmdServlet.class.getName());
    }

    @SuppressWarnings("java:S1989")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        log.trace(Logger.EVENT_UNSPECIFIED, "----------------------------------------- doGet() start");
        printHeaders(request);
        String errorMsg = "";

        AuthorizationInfo authorizationInfo;

        try
        {
            authorizationInfo = buildAuthInfo(request, authorizationService);
        }
        catch (Exception ae)
        {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
            return;
        }

        logRequest(request);

        ServletCodecWrapper servletCodecWrapper = HttpUtil.getCodec(request, HttpUtil.ACCEPT);

        if (servletCodecWrapper == null)
        {
            errorMsg = "Not Acceptable, codec is invalid or not available.";
            sendError(response, HttpServletResponse.SC_NOT_ACCEPTABLE, errorMsg);
            return;
        }

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + " codec: " + servletCodecWrapper.getValue());

        response.setHeader(HttpUtil.CONTENT_DISPOSITION, HttpUtil.getContentDisposition(servletCodecWrapper));

        String contentType = HttpUtil.codecToMimeType(servletCodecWrapper);
        ESAPI.httpUtilities().addHeader(response, "Content-Type", contentType);

        response.addHeader("Expires", "Thu, 19 Nov 1981 08:52:00 GMT");
        response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
        response.addHeader("Pragma", "no-cache");

        ESAPI.httpUtilities().setHeader(response, HttpUtil.SESSIONID_HEADER, "null");
        ESAPI.httpUtilities().setHeader(response, HttpUtil.NUANCE_SESSIONID_HEADER, "null");

        String context = ESAPI.encoder().encodeForHTML(request.getHeader(NUANCE_CONTEXT_HEADER));
        ESAPI.httpUtilities().setHeader(response, NUANCE_CONTEXT_HEADER, context != null ? context : UUID.randomUUID().toString());

        String lang = HttpUtil.getAcceptLanguage(request);

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + "lang:" + lang);

        TTSContent ttsContent;
        try
        {
            ttsContent = new TTSContent(request);
        }
        catch (IllegalArgumentException ie)
        {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, ie.getMessage());
            return;
        }
        catch (Exception e)
        {
            sendError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, e.getMessage());
            return;
        }

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + "tts_type:" + ttsContent.getTTSType());
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + "tts_input:" + ttsContent.getTTSInput());
        if (ttsContent.getTTSVoice() != null)
        {
            log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                           .getName() + "tts_voice:" + ttsContent.getTTSVoice());
        }

        String uniqueId = HttpUtil.getSanitizedParameter(request, "id");
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + "id:" + uniqueId);

        String ttsGenre = HttpUtil.getSanitizedHeader(request, "X-TTS-Genre");
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + "TTS Genre:" + ttsGenre);

        Transaction transaction = null;
        String sessionID;
        try
        {
            transaction = transactionFactory.newTransaction(authorizationInfo, uniqueId);
            sendNMASRequest(servletCodecWrapper, response, HttpUtil.getSanitizedParameter(request, "ttsLang"), ttsContent, transaction, ttsGenre);
            transaction.waitForResponse();
        }
        catch (Exception e)
        {
            log.info(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        finally
        {
            if (transaction != null)
            {
                if (!transaction.isRequestCompleted())
                {
                    sessionID = transaction.getSessionId();
                    errorMsg = (sessionID != null) ? HttpUtil.SESSIONID_HEADER + sessionID + "\n" : "";
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
                transaction.cleanup();
            }
        }
        log.trace(Logger.EVENT_UNSPECIFIED, "----------------------------------------- doGet() end");
    }


    @SuppressWarnings("java:S107")
    public void sendNMASRequest(ServletCodecWrapper codec, HttpServletResponse response, String ttsLanguage,
            TTSContent ttsContent, Transaction transaction, String ttsGenre)
            throws IOException
    {
        String locale = "en_US";
        String uiLanguage = "en";

        NMDPTTSCmdListener listener = new NMDPTTSCmdListener(transaction, response);

        AudioSinkImpl audioSink = audioSinkFactory.newAudioSink(response, codec == ServletCodecWrapper.WAV, codec);
        audioSink.registerListener(listener);
        listener.setAudioSink(audioSink);

        if (codec == ServletCodecWrapper.WAV)
        {
            codec = ServletCodecWrapper.PCM_16_22K;
        }
        else if (codec == ServletCodecWrapper.SPEEX_8K)
        {
            codec = ServletCodecWrapper.PCM_16_8K;
        }
        else if (codec == ServletCodecWrapper.SPEEX_16K)
        {
            codec = ServletCodecWrapper.PCM_16_16K;
        }

        PdxClient client = createPdxClient(
                nmspGateway,
                nmspGatewayPort,
                transaction.getAuthorizationInfo().getAppId(),
                transaction.getAuthorizationInfo().getAppKey(),
                transaction.getUniqueId() == null ? "1234" : transaction.getUniqueId(),
                codec.toICodecResourceCodec(),
                codec.toICodecResourceCodec(),
                nmspSslEnabled,
                nmspSslSelfSigned,
                commandTimeout,
                listener);

        transaction.setClient(client);
        if (!client.connect())
        {
            throw new IOException("Failed to connect to NMSP gateway");
        }
        if (!client.awaitConnected(commandTimeout))
        {
            throw new IOException("Timed out waiting for NMSP gateway COP_CONNECTED");
        }

        client.loadResource("NMASSampleApp");

        NmspDictionary commandDict = new NmspDictionaryImpl();
        if (ttsContent.getTTSVoice() != null)
        {
            commandDict.addUTF8String("tts_voice", ttsContent.getTTSVoice());
        }
        else
        {
            commandDict.addUTF8String("tts_language", ttsLanguage);
        }
        if (ttsGenre != null)
        {
            commandDict.addUTF8String("tts_text_genre", ttsGenre);
        }
        commandDict.addUTF8String("ui_language", uiLanguage);
        commandDict.addUTF8String("nmaid", transaction.getAuthorizationInfo().getAppId());
        commandDict.addUTF8String("phone_submodel", deviceSubModel);
        commandDict.addUTF8String("phone_OS", deviceOs);
        commandDict.addUTF8String("phone_network", deviceNetwork);
        commandDict.addUTF8String("locale", locale);

        client.beginCommand(nmdpCommand, "NMASSampleApp", appVersion,
                ttsLanguage == null ? "enus" : ttsLanguage, carrier, deviceModel, commandDict);

        client.sendDictParam(pdxTTS, ttsContent.getDictionary());

        client.endCommand();

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread().getName() + " endCommand()");
    }

}

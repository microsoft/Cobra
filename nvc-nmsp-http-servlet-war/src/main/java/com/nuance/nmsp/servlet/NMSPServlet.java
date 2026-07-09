package com.nuance.nmsp.servlet;

import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import com.nuance.ncs.nmsp.engine.pdx.PdxResponseListener;
import com.nuance.nmsp.servlet.nmas.AsrContext;
import com.nuance.nmsp.servlet.nmas.TransactionFactory;
import com.nuance.nmsp.servlet.nmas.audio.AudioStreamer;
import com.nuance.nmsp.servlet.util.EnvUtil;
import com.nuance.nmsp.servlet.util.HttpUtil;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class NMSPServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger log = ESAPI.getLogger(NMSPServlet.class);

    protected final String carrier = EnvUtil.useDefault("CARRIER", "Nuance");
    protected final String deviceModel = EnvUtil.useDefault("DEVICE_MODEL", "NMSP");
    protected final String deviceSubModel = EnvUtil.useDefault("DEVICE_SUBMODEL", "servlet");
    protected final String deviceOs = EnvUtil.useDefault("DEVICE_OS", "Java");
    protected final String deviceNetwork = EnvUtil.useDefault("DEVICE_NETWORK", "LAN");

    protected final String nmspGateway = EnvUtil.getOrDefault("NMSPGW_HOST", "127.0.0.1");
    protected final short nmspGatewayPort = Short.parseShort(EnvUtil.getOrDefault("NMSPGW_PORT", "8911"));
    protected final boolean nmspSslEnabled = Boolean.parseBoolean(EnvUtil.getOrDefault("NMSPGW_SSL_SOCKET_ENABLE", "TRUE"));
    protected final boolean nmspSslSelfSigned = Boolean.parseBoolean(EnvUtil.getOrDefault("NMSPGW_SSL_SELF_SIGNED_CERT", "FALSE"));

    protected final int commandTimeout = Integer.parseInt(EnvUtil.useDefault("COMMAND_TIMEOUT", "60000"));

    protected final TransactionFactory transactionFactory = new TransactionFactory();

    @SuppressWarnings("java:S1172")
    public AudioStreamer sendNMASRequest(HttpServletRequest request, ServletCodecWrapper codec,
            AsrContext nluContext, Transaction transaction, String dictationLanguage, String dictationType)
            throws IOException
    {
        return null;
    }

    @SuppressWarnings("java:S107")
    protected PdxClient createPdxClient(String host, int port,
            String appId, byte[] appKey, String userId,
            ICodecResource.Codec inCodec, ICodecResource.Codec outCodec,
            boolean sslEnabled, boolean sslSelfSigned,
            long timeoutMs, PdxResponseListener listener)
    {
        return new PdxClient(host, port, appId, appKey, userId,
                inCodec, outCodec, sslEnabled, sslSelfSigned, timeoutMs, listener);
    }

    @Override
    public void init()
            throws ServletException
    {
        log.info(Logger.EVENT_UNSPECIFIED, "init()");
    }

    @Override
    public void destroy()
    {
        log.info(Logger.EVENT_UNSPECIFIED, "destroy()");
        super.destroy();
    }

    protected void sendError(HttpServletResponse response, int status, String errorMsg)
    {
        HttpUtil.sendError(response, status, errorMsg);
    }

    protected void printHeaders(HttpServletRequest request)
    {
        log.debug(Logger.EVENT_UNSPECIFIED, "++++++++++++++++++ printHeader start");
        String headers = null;
        Enumeration<String> e = request.getHeaderNames();

        while (e.hasMoreElements())
        {
            headers = e.nextElement();
            if (headers != null)
            {
                log.debug(Logger.EVENT_UNSPECIFIED, headers + ":" + HttpUtil.getSanitizedHeader(request, headers));
            }
        }
        log.debug(Logger.EVENT_UNSPECIFIED, "++++++++++++++++++ printHeader end");
    }

    protected AuthorizationInfo buildAuthInfo(HttpServletRequest request, AuthorizationService authorizationService)
            throws IllegalAccessException
    {
        String appId = HttpUtil.getSanitizedParameter(request, "appId");
        String appKey = HttpUtil.getSanitizedParameter(request, "appKey");

        return authorizationService.authorize(appId, appKey);
    }

    protected void logRequest(HttpServletRequest request)
    {
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                 .getName() + " New request from: " + HttpUtil.getSanitizedHeader(request, "Host"));
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                 .getName() + " User-Agent: " + HttpUtil.getSanitizedHeader(request, "User-Agent"));
    }

    @SuppressWarnings("java:S107")
    public void executeNmspAudioTransaction(HttpServletResponse response, AuthorizationInfo authorizationInfo, String lang, String uniqueId,
            HttpServletRequest request, ServletCodecWrapper codec, AsrContext dictationContext, String dictationType)
    {
        Transaction transaction = null;
        AudioStreamer audioStreamer = null;
        try(ServletOutputStream out = response.getOutputStream())
        {
            transaction = transactionFactory.newTransaction(authorizationInfo, uniqueId);
            audioStreamer = sendNMASRequest(request, codec, dictationContext, transaction, lang, dictationType);

            log.trace(Logger.EVENT_UNSPECIFIED, "Waiting ...");
            transaction.waitForResponse();
            log.trace(Logger.EVENT_UNSPECIFIED, "Done ...");

            ESAPI.httpUtilities().addHeader(response, HttpUtil.CONTENT_LANGUAGE, lang);
            ESAPI.httpUtilities().setHeader(response, HttpUtil.SESSIONID_HEADER, transaction.getSessionId());
            ESAPI.httpUtilities().setHeader(response, HttpUtil.NUANCE_SESSIONID_HEADER, transaction.getSessionId());

            out.write(transaction.getResults().getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            log.info(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        finally
        {
            HttpUtil.cleanupTransactionAudioStreamer(transaction, response, audioStreamer);
        }
    }

    protected void doAudioGet(HttpServletRequest request, AuthorizationService authorizationService, HttpServletResponse response)
    {
        log.trace(Logger.EVENT_UNSPECIFIED, "----------------------------------------- doGet() start");
        printHeaders(request);
        String errorMsg = "";

        AsrContext dictationContext = HttpUtil.parseAsrContext(request);

        AuthorizationInfo authorizationInfo = null;
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

        ServletCodecWrapper codec = HttpUtil.getCodec(request, HttpUtil.CONTENT_TYPE);
        if (codec == null)
        {
            errorMsg = "Not Acceptable, codec is invalid or not available.";
            sendError(response, HttpServletResponse.SC_NOT_ACCEPTABLE, errorMsg);
            return;
        }
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread().getName() + " codec: " + codec.getValue());

        String dictationType = HttpUtil.getTopic(request);
        if (!HttpUtil.isValidTopic(dictationType))
        {
            errorMsg = "Not Acceptable, topic type is invalid.";
            sendError(response, HttpServletResponse.SC_NOT_ACCEPTABLE, errorMsg);
            return;
        }

        String lang = HttpUtil.getAcceptLanguage(request);

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread().getName() + "lang:" + lang);

        String uniqueId = HttpUtil.getSanitizedParameter(request, "id");

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread().getName() + "id:" + uniqueId);

        executeNmspAudioTransaction(response, authorizationInfo, lang, uniqueId, request, codec, dictationContext, dictationType);

        log.trace(Logger.EVENT_UNSPECIFIED, "----------------------------------------- doGet() end");
    }
}

package com.nuance.nmsp.servlet.nmas.dictation;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.nmsp.servlet.AuthorizationService;
import com.nuance.nmsp.servlet.NMSPServlet;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.nmas.AsrContext;
import com.nuance.nmsp.servlet.nmas.audio.AudioStreamer;
import com.nuance.nmsp.servlet.nmas.audio.OggAudioStreamer;
import com.nuance.nmsp.servlet.util.EnvUtil;
import com.nuance.nmsp.servlet.util.HttpUtil;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NMDPAsrCmdServlet
        extends NMSPServlet
{

    private static final long serialVersionUID = 1L;

    private static final Logger log = ESAPI.getLogger(NMDPAsrCmdServlet.class);

    private final String nmdpCommand = EnvUtil.useDefault("ASR_COMMAND_NAME", "NMDP_ASR_CMD");
    private final String appVersion = EnvUtil.useDefault("ASR_COMMAND_VERSION", "0.1");
    private final String pdxDict = EnvUtil.useDefault("ASR_QUERY_PARAM", "REQUEST_INFO");
    private final String audio = EnvUtil.useDefault("ASR_AUDIO_PARAM", "AUDIO_INFO");
    private final String nlEscapeSeq = EnvUtil.useDefault("ASR_NL_ESCAPE_SEQ", "\\*new_line");

    private final AuthorizationService authorizationService = new AuthorizationService();

    public NMDPAsrCmdServlet()
    {
        super();
    }

    @Override
    public void init()
            throws ServletException
    {
        super.init();
        log.info(Logger.EVENT_UNSPECIFIED, "init() servlet completed:" + NMDPAsrCmdServlet.class.getName());
    }

    @SuppressWarnings("java:S1989")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        ESAPI.httpUtilities().addHeader(response, HttpUtil.CONTENT_TYPE, HttpUtil.PLAIN_TEXT_TYPE.getRawString());
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        doAudioGet(request, authorizationService, response);
    }

    @SuppressWarnings("java:S107")
    @Override
    public AudioStreamer sendNMASRequest(HttpServletRequest request, ServletCodecWrapper codec,
            AsrContext dictationContext, Transaction transaction, String dictationLanguage, String dictationType)
            throws IOException
    {
        String locale = "en_US";
        String uiLanguage = "en";

        NMDPDictationCmdListener listener =
                new NMDPDictationCmdListener(transaction, dictationContext.getEscapeNewLines(), dictationContext.getnBestSize(), nlEscapeSeq);

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
        commandDict.addUTF8String("dictation_type", dictationType);
        commandDict.addUTF8String("dictation_language", dictationLanguage);
        commandDict.addUTF8String("ui_language", uiLanguage);
        commandDict.addUTF8String("nmaid", transaction.getAuthorizationInfo().getAppId());
        commandDict.addUTF8String("phone_submodel", deviceSubModel);
        commandDict.addUTF8String("phone_OS", deviceOs);
        commandDict.addUTF8String("phone_network", deviceNetwork);
        commandDict.addUTF8String("locale", locale);

        if (dictationContext.getAudioSource() != null)
        {
            commandDict.addUTF8String("locale", dictationContext.getAudioSource());
        }

        client.beginCommand(nmdpCommand, "NMASSampleApp", appVersion,
                dictationLanguage, carrier, deviceModel, commandDict);

        client.sendDictParam(pdxDict, dictationContext.getDictionary(false));

        PdxAudioSink audioSink = client.createAudioSink(audio);
        AudioStreamer audioStreamer;
        if (codec.isSpeex())
        {
            audioStreamer = new OggAudioStreamer(request, audioSink);
        }
        else
        {
            audioStreamer = new AudioStreamer(request, audioSink);
        }
        audioStreamer.start();
        client.endCommand();

        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread().getName() + " endCommand()");
        return audioStreamer;
    }

}

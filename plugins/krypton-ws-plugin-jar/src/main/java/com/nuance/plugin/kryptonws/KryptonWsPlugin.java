package com.nuance.plugin.kryptonws;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.kryptonws.methods.KryptonWsInvalidMethod;
import com.nuance.plugin.kryptonws.methods.KryptonWsMethod;
import com.nuance.plugin.kryptonws.methods.KryptonWsMethodFactory;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseBuilder;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.*;
import static com.nuance.plugin.kryptonws.params.KryptonWsParameters.PLUGIN_PARAMS_NAME;

public class KryptonWsPlugin
        implements IWebApiRawPlugin
{
    private static final Logger logger = LogManager.getLogger();

    public static final String PLUGIN_URL_FILTER = "kryptonws";

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("krypton-ws-plugin-jar-%s").build();
    private ExecutorService executorService;
    private KryptonWsSession kryptonWsSession;

    @Override
    public void onInitPlugin(IWebApiFeatures webapiFeatures)
    {
        logger.trace("onInitPlugin KryptonWs");
        initPluginSettingsAndExecutorService(webapiFeatures);
        kryptonWsSession = new KryptonWsSession(executorService);
        kryptonWsSession.setWebapiFeatures(webapiFeatures);
    }

    void initPluginSettings(IWebApiFeatures webapiFeatures)
            throws IOException
    {
        CombinedConfiguration cc = webapiFeatures.getConfigs();
        WebApiJSONConfiguration pluginSettingsAndDefaults = KryptonWsConfig.getKryptonWsDefaultConfig();
        WebApiJSONConfiguration pluginSettings = (WebApiJSONConfiguration) cc.removeConfiguration(PLUGIN_PARAMS_NAME);
        pluginSettingsAndDefaults.merge(pluginSettings);
        cc.addConfiguration(pluginSettingsAndDefaults, PLUGIN_PARAMS_NAME);
    }

    void initPluginSettingsAndExecutorService(IWebApiFeatures webapiFeatures)
    {
        boolean asyncExecutor;
        try
        {
            initPluginSettings(webapiFeatures);

            asyncExecutor = webapiFeatures.getConfigs()
                                          .getBoolean(KRYPTON_WS_ASYNC_WEBAPI_CALLS.getKey());
        }
        catch (IOException e)
        {
            asyncExecutor = true;
        }

        if (asyncExecutor)
        {
            executorService = Executors.newSingleThreadExecutor(threadFactory);
        }
        else
        {
            executorService = MoreExecutors.newDirectExecutorService();
        }
    }

    @Override
    public String getUrlPattern()
    {
        logger.trace("getUrlPattern KryptonWs");
        return PLUGIN_URL_FILTER;
    }

    @Override
    public boolean onSocketOpen(Session session)
    {
        String id = session != null ? session.getId() : "";
        logger.info("onSocketOpen. KryptonWs. id = {}", id);

        if (session != null) {
            try {
                int maxIdleTimeout = kryptonWsSession.getWebapiFeatures().getConfigs().getInt(KRYPTON_WS_WEBSOCKET_MAX_IDLE_TIMEOUT.getKey());
                session.setMaxIdleTimeout(maxIdleTimeout);
                logger.trace("Successfully set websocket maximum idle timeout to: {} milliseconds", maxIdleTimeout);
            } catch (Exception e) {
                logger.error("Unable to set websocket maximum idle timeout.", e);
            }
        }

        kryptonWsSession.setKryptonWsWsSession(session)
                        .setKryptonWsResponseSender(new KryptonWsResponseSender(session));
        return true;
    }

    @Override
    public boolean onSocketClosed(Session session, CloseReason reason)
    {
        String id = session != null ? session.getId() : "";
        CloseReason closeReason = Optional.ofNullable(reason).orElse(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, ""));

        logger.info("onSocketClosed: KryptonWs websocket is closing. SessionId = {}. ReasonPhrase = \"{}\". CloseCode = {}",
                id, closeReason.getReasonPhrase(), closeReason.getCloseCode().getCode());

        kryptonWsSession.terminateWebapiWsSession(closeReason);
        kryptonWsSession.setKryptonWsWsSession(null);
        kryptonWsSession.setKryptonWsSessionId(null);
        kryptonWsSession.getWebapiFeatures().close();
        kryptonWsSession = null;
        return true;
    }

    @Override
    public boolean onSocketMessage(Session session, String message)
    {
        String id = session != null ? session.getId() : "";
        String messageString = Optional.ofNullable(message).orElse("");
        logger.info("onSocketMessage: KryptonWs Message received for session {}: {}", id, messageString);

        KryptonWsMethod kryptonWsMethod = null;

        try {
            kryptonWsMethod = KryptonWsMethodFactory.createMethod(message, kryptonWsSession);
        } catch (Exception e)
        {
            logger.info("Error while creating method: ", e);
            return false;
        }

        if(kryptonWsMethod == null)
        {
            sendErrorResponseWithoutMethod("Could not parse request message", message);
            return false;
        }

        if(!kryptonWsMethod.validateKryptonSessionEstablished())
        {
            kryptonWsMethod.sendErrorResponse("Method requires an active session (or no active session for CreateSession)", null);
            return false;
        }

        if(!kryptonWsMethod.isKryptonWsSessionIdAssociatedToThisKryptonWsWsSession())
        {
            String errorMessage = String.format("SessionId %s is not associated to this websocket session", kryptonWsMethod.getMethodKryptonWsSessionId());
            kryptonWsMethod.sendErrorResponse(errorMessage, null);
            return false;
        }

        if (!KryptonWsMethod.ProtocolVersion.supported.contains(kryptonWsMethod.getVersion()))
        {
            kryptonWsMethod.sendErrorResponse("Unsupported protocol version", "Supported versions are " + KryptonWsMethod.ProtocolVersion.supported);
            return false;
        }

        if(!kryptonWsMethod.isValidSessionAndRequestId())
        {
            kryptonWsMethod.sendErrorResponse("sessionId or requestId format is invalid", null);
            return false;
        }

        if (!kryptonWsMethod.canRunWhile(getCurrentMethod()))
        {
            KryptonWsResponseBuilder responseBuilder = new KryptonWsResponseBuilder(kryptonWsMethod)
                    .setStatus(KryptonWsMessage.STATUS.FAILED)
                    .addError("Command not supported in this state", getCurrentMethod().getMethodName())
                    .setVersion("1.0");

            kryptonWsSession.getKryptonWsResponseSender().sendResponse(responseBuilder.build().toJson());
            return false;
        }
        else
        {
            kryptonWsMethod.process();
            return true;
        }
    }

    public KryptonWsMethod getCurrentMethod()
    {
        if (kryptonWsSession.getWebapiResponseListener() == null)
        {
            return null;
        }
        else
        {
            return kryptonWsSession.getWebapiResponseListener().getCurrentMethod();
        }
    }

    private void sendErrorResponseWithoutMethod(String errorMessage, String jsonMessage)
    {
        KryptonWsInvalidMethod kryptonWsInvalidMethod = new KryptonWsInvalidMethod(null, kryptonWsSession);
        kryptonWsInvalidMethod.sendErrorResponse(errorMessage, jsonMessage);
    }

    @Override
    public boolean onSocketBinary(Session session, byte[] buffer)
    {
        int len = Optional.ofNullable(buffer).orElse(new byte[0]).length;
        logger.info("onSocketBinary: KryptonWs. Received binary message of size {} bytes.", len);

        if (getCurrentMethod() == null)
        {
            handleStrayAudio(kryptonWsSession, buffer);
            return false;
        }

        getCurrentMethod().onAudio(buffer);
        return true;
    }

    public static void handleStrayAudio(KryptonWsSession kryptonWsSession, byte[] buffer)
    {
        boolean ignoreStrayAudio = kryptonWsSession.getPluginConfig().getBoolean(KRYPTON_WS_IGNORE_STRAY_AUDIO.getKey());
        if(ignoreStrayAudio)
        {
            int len = Optional.ofNullable(buffer).orElse(new byte[0]).length;
            logger.info("Ignoring {} audio bytes", len);
            return;
        }

        KryptonWsResponseBuilder
                responseBuilder = new KryptonWsResponseBuilder("Audio", kryptonWsSession.getKryptonWsSessionId(), UUID.randomUUID().toString())
                .setResponseTo("Audio")
                .setVersion("1.0")
                .setStatus(KryptonWsMessage.STATUS.FAILED)
                .addError(KryptonWsResponseBuilder.buildError("Command not supported in this state", "idle"));

        kryptonWsSession.getKryptonWsResponseSender().sendResponse(responseBuilder.build().toJson());
    }

    @Override
    public boolean onSocketError(Session session, Throwable error)
    {
        String id = session != null ? session.getId() : "";
        logger.info("onSocketError KryptonWs. id = {}", id);

        error = Optional.ofNullable(error).orElse(new Throwable(""));
        String errorMessage = Optional.ofNullable(error.getMessage()).orElse("");

        if (errorMessage.contains("Idle timeout expired") ||
            errorMessage.contains("Timeout on Read") ||
            errorMessage.contains("Broken pipe") ||
            error instanceof IOException)
        {
            logger.info("Websocket {}", errorMessage);
        }
        else
        {
            logger.warn("onSocketError KryptonWs.: ", error);
        }

        return true;
    }

    @Override
    public boolean onSocketPong(Session session, PongMessage pongMessage)
    {
        String id = session != null ? session.getId() : "";
        String messageString = pongMessage != null ? pongMessage.toString() : "";
        logger.info("onSocketPong: KryptonWs Got a pong message for websocket sessionId {}: {}", id, messageString);
        return true;
    }

    public ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Code below is for unit tests
     */
    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    public KryptonWsSession getKryptonWsSession()
    {
        return kryptonWsSession;
    }

    public static void closeKryptonWsWsSession(KryptonWsSession session, CloseReason closeReason) {
        try
        {
            boolean close = session.getPluginConfig().getBoolean(KRYPTON_WS_CLOSE_WS_SESSION.getKey(), (boolean)KRYPTON_WS_CLOSE_WS_SESSION.getDefaultValue());
            session.setKryptonWsSessionId(null);

            if (!close) return;
            session.getKryptonWsWsSession().close(closeReason);
        } catch (IOException e) {

            logger.warn("Caught exception while closing KryptonWs websocket session when webapi session closed.", e);

        }
    }

}

package com.nuance.plugin.kryptonws.webapi;

import com.nuance.plugin.kryptonws.KryptonWsPlugin;
import com.nuance.plugin.kryptonws.methods.KryptonWsMethod;
import com.nuance.plugin.kryptonws.response.KryptonWsEvent;
import com.nuance.plugin.kryptonws.response.KryptonWsEventBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import java.util.Optional;

public class KryptonWsWebapiResponseListener
        implements IResponseSender
{
    private static final Logger logger = LogManager.getLogger(KryptonWsWebapiResponseListener.class);
    private KryptonWsMethod currentMethod;
    private KryptonWsSession kryptonWsSession;

    @Override
    public void sendResponse(String response)
    {
        logger.info("Got response from webapi: {}", response);
        if(currentMethod == null) return;

        currentMethod.processWebapiResponse(response);
    }

    @Override
    public void sendBinary(byte[] data)
    {
        logger.warn("Ignoring {} bytes of binary data from webapi", data.length);
    }

    @Override
    public void closeConnection(CloseReason reason)
    {
        CloseReason closeReason = Optional.ofNullable(reason).orElse(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, ""));
        logger.info("Got closeConnection from webapi. Code {}. Reason: {}", closeReason.getCloseCode(), closeReason.getReasonPhrase());

        KryptonWsEventBuilder endOfSessionEvent = KryptonWsEventBuilder.buildEndOfSessionEvent(kryptonWsSession.getKryptonWsSessionId(), closeReason.getReasonPhrase());
        kryptonWsSession.getKryptonWsResponseSender().sendResponse(endOfSessionEvent.build().toJson());
        kryptonWsSession.terminateWebapiWsSession(reason);
        KryptonWsPlugin.closeKryptonWsWsSession(kryptonWsSession, reason);
    }

    public KryptonWsMethod getCurrentMethod()
    {
        return currentMethod;
    }

    public void setCurrentMethod(KryptonWsMethod currentMethod)
    {
        this.currentMethod = currentMethod;
        if(currentMethod != null)
        {
            kryptonWsSession = currentMethod.getKryptonWsSession();
        }
    }

}

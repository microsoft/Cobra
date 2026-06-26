package com.nuance.plugin.ncs7;

import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.Map;

public class WebApiResponseHandler implements IResponseSender {
    private static final Logger logger = LogManager.getLogger(WebApiResponseHandler.class);
    private final Orchestrator orchestrator;

    public WebApiResponseHandler(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void sendResponse(String response) {
        logger.debug("======< {}", response);
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, response));
    }

    @Override
    public void sendBinary(byte[] data) {
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.DATA, data));
    }

    @Override
    public void closeConnection(CloseReason closeReason) {
        try {
            if (closeReason != null) {
                logger.error("WEBAPI SOCKET CLOSED {}", closeReason.getReasonPhrase());
                orchestrator.webSocketSession.close();
                orchestrator.terminate();
            }
        } catch (IOException e) {
            logger.error("Failed to close socket.", e);
        } finally {
            orchestrator.notify("webapi socket response " + Thread.currentThread().getName()); //wakeup orchestrator
        }
    }
}

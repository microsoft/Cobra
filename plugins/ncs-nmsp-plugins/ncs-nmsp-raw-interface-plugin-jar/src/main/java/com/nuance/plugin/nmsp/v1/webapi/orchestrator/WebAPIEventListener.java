package com.nuance.plugin.nmsp.v1.webapi.orchestrator;

import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;

public class WebAPIEventListener implements IResponseSender {
    private static final Logger logger = LogManager.getLogger();

    //provide a default listener to avoid null pointers.
    IWebapiOrchestratorEvents eventsListener = new IWebapiOrchestratorEvents() {
        @Override
        public void onResponse(String response) {

        }

        @Override
        public void onBinary(byte[] data) {

        }

        @Override
        public void onClose(CloseReason closeReason) {

        }
    };

    public void setListener(IWebapiOrchestratorEvents listener) {
        this.eventsListener = listener;
    }

    @Override
    public void sendResponse(String response) {
        logger.debug("======< Webapi response: {}", response);
        eventsListener.onResponse(response);
    }

    @Override
    public void sendBinary(byte[] data) {
        logger.debug("======< Got {} bytes from webapi", data.length);
        eventsListener.onBinary(data);
    }

    @Override
    public void closeConnection(CloseReason closeReason) {
        logger.debug("======< Webapi onClose: {}", closeReason);
        eventsListener.onClose(closeReason);
    }
}

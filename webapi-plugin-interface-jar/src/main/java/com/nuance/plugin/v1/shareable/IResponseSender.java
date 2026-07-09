package com.nuance.plugin.v1.shareable;

import javax.websocket.CloseReason;

public interface IResponseSender {
    /**
     * Sends a string to the user
     *
     * @param response
     *         The string to be sent
     */
    void sendResponse(String response);

    /**
     * Sends raw data to the user. Typically audio
     *
     * @param data
     *         The audio to be sent
     */
    void sendBinary(byte[] data);

    /**
     *
     * @param closeReason
     */
    void closeConnection(CloseReason closeReason);
}

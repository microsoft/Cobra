package com.nuance.cobra.client.nim;

import org.json.JSONObject;

/**
 * An interface for a client to react to NiM callbacks
 */
interface NiMClientListener
{
    /**
     * An event occurred. For a list of events @see PDXCommandListener
     * @param event The event code
     */
    void onNiMEvent(short event);

    /**
     * An error occurred. The error contains a code and description
     * @param error The error
     */
    void onNiMError(JSONObject error);

    /**
     * A result is available. It can either be partial or final
     * @param result The result
     */
    void onNiMResult(JSONObject result);

    /**
     * A retry is issued. It typically contains a name, a reason and a prompt
     * @param retry The retry object
     */
    void onNiMRetry(JSONObject retry);

    /**
     * Audio is received
     * @param buffer The audio buffer
     * @param offset The audio buffer's offset
     * @param length The audio buffer's length
     * @param isLastBuffer True if this is the last audio buffer
     */
    void onNiMAudio(byte[] buffer, int offset, int length, boolean isLastBuffer);
}


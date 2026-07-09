package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.ResponseEvent;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.webapi.ResponseSender;

/**
 * The ResponseReceiver is a class that intercepts a response before it is sent back
 *
 * @see ResponseHandler     A handler will contain the sender and receiver to abstract implementations
 * @see ResponseFormatter   A formatter will format any webapi client's reponse to a standardized format
 * @see ResponseSender      A sender will send the response to the user
 */
public interface ResponseReceiver
{

    /**
     * Method intercepting the response before it is sent back to the client application. It returns a boolean telling the responsehandler whether the response should be sent to the sender or not.
     *
     * @param response
     *          The result to be handled by the receiver
     * @return
     *      A boolean telling the handler whether or not the response should be sent to the sender after invoking the receiver.
     */
    boolean onResponse(ResponseResult response);

    /**
     * Method intercepting the event before it is sent back to the client application. It returns a boolean telling the responsehandler whether the event should be sent to the sender or not.
     *
     * @param event
     *          The event to be handled by the receiver
     * @return
     *      A boolean telling the handler whether or not the event should be sent to the sender after invoking the receiver.
     */
    boolean onEvent(ResponseEvent event);

    /**
     * Method intercepting the audio before it is sent back to the client application. It returns a boolean telling the responsehandler whether the audio should be sent to the sender or not.
     *
     * @param audio
     *          The audio to be handled by the receiver
     * @param isLastBuffer
     *         True if this is the last audio buffer
     * @return
     *      A boolean telling the handler whether or not the audio should be sent to the sender after invoking the receiver.
     */
    boolean onAudioResponse(byte[] audio, boolean isLastBuffer);

}

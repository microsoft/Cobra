package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseEvent;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.webapi.ResponseSender;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The ResponseHandler is a class given to a client to abstract the endpoint's implementation
 * The client can send audio (as raw bytes) or a response (as a String) and the ResponseSender
 * will communicate it with the user.
 *
 * @see ResponseFormatter   A formatter will format any webapi client's reponse to a standardized format
 * @see ResponseReceiver    A receiver can intercept the response (ie. for command chaining)
 * @see ResponseSender      A sender will send the response to the user
 */
public class ResponseHandler
{
    private static final Logger logger = LogManager.getLogger(ResponseHandler.class);

    /**
     * A ResponseFormatter will ensure a standardized format independently of the client's
     */
    private ResponseFormatter formatter;

    /**
     * A ResponseSender will ensure communication with the user
     * It is the last one to be notified of a response
     */
    private IResponseSender sender;

    /**
     * The receiver can intercept the response before it reaches the sender.
     * (Mostly used for command chaining)
     */
    private ResponseReceiver receiver;

    /**
     * An object that needs to take action before or after the response is sent (e.g. command chain)
     */
    private CommandChainer chainer;

    public ResponseHandler(ResponseReceiver receiver, ResponseFormatter formatter, ResponseSender sender)
    {
        this(receiver, formatter, sender, null);
    }

    public ResponseHandler(ResponseReceiver receiver, ResponseFormatter formatter, IResponseSender sender, CommandChainer chainer)
    {
        this.formatter = formatter;
        this.receiver = receiver;
        this.sender = sender;
        this.chainer = chainer;
    }

    /**
     * Gets the response sender.
     *
     * @return The sender that will send the response back to the user.
     */
    public IResponseSender getSender()
    {
        return sender;
    }

    /**
     * Gets the response formatter.
     *
     * @return The formatter that will standardize the response.
     */
    public ResponseFormatter getFormatter()
    {
        return formatter;
    }

    /**
     * This method is called by any client when a response (Partial or Final) is
     * available. This method will format the response. This method is to be called
     * by clients and receivers will intercept this
     *
     * @param response
     *         The response object
     */
    public void onResponse(ResponseResult response)
    {
        if (sender == null)
        {
            throw new IllegalStateException("received response with no sender");
        }

        boolean sendResponse = true; // TODO do we keep this boolean or not?
        String formattedResponse;
        try
        {
            if (receiver != null)
            {
                sendResponse = receiver.onResponse(response);
            }


            if (sendResponse)
            {
                if (chainer != null)
                {
                    chainer.onResponseWillBeSent(response);
                }

                formattedResponse = formatter.formatResponse(response);
                sender.sendResponse(formattedResponse);

                if (chainer != null)
                {
                    chainer.onResponseWasSent(response, formattedResponse);
                }
            }
        }
        catch (CobraException exceptionFormattingResponse)
        {
            try
            {
                ResponseResult result = exceptionFormattingResponse.getResult();
                formattedResponse = result.toJson();
            }
            catch (CobraException exceptionFormattingException)
            {
                logger.error("Cannot serialize result.", exceptionFormattingException);
                formattedResponse = ResponseCode.createResponseErrorString(ExternalResponseErrorCode.RESPONSE_SERIALIZE_ERROR);
            }
            sender.sendResponse(formattedResponse);
        }
    }

    /**
     * This method is called by any client when a response (Partial or Final) is
     * available. This method will format the response. This method is to be called
     * by receivers and will not be intercepted by receivers
     *
     * @param formattedResponse
     *         The response
     */
    public void sendResponse(String formattedResponse)
    {
        sender.sendResponse(formattedResponse);
    }

    public void sendEvent(ResponseEvent event)
    {
        String formattedResponse;

        try
        {
            formattedResponse = formatter.formatResponse(event);
        }
        catch (CobraException exceptionFormattingResponse)
        {
            try
            {
                ResponseResult result = exceptionFormattingResponse.getResult();
                formattedResponse = result.toJson();
            }
            catch (CobraException exceptionFormattingException)
            {
                logger.error("Cannot serialize result.", exceptionFormattingException);
                formattedResponse = ResponseCode.createResponseErrorString(ExternalResponseErrorCode.RESPONSE_SERIALIZE_ERROR);
            }
        }

        boolean sendEvent = true;
        if (receiver != null)
        {
            sendEvent = receiver.onEvent(event);
        }

        if(sendEvent)
        {
            sender.sendResponse(formattedResponse);
        }
    }

    /**
     * This method is called by any client when audio must be streamed back to the
     * user.
     *
     * @param audio
     *         The audio to be sent
     * @param isLastBuffer
     *         True if this is the last audio buffer
     */
    public void onAudio(byte[] audio, boolean isLastBuffer)
    {
        boolean sendAudio = true;

        if(receiver != null)
        {
            sendAudio = receiver.onAudioResponse(audio, isLastBuffer);
        }

        if(sendAudio)
        {
            sender.sendBinary(audio);
        }
    }

}

package com.nuance.cobra.utils.webapi;

import com.nuance.plugin.v1.shareable.IResponseSender;

/**
 * The ResponseSender is the class that will send back the response to the user.
 *
 * @see ResponseHandler     A handler will contain the sender and receiver to abstract implementations
 * @see ResponseFormatter   A formatter will format any webapi client's reponse to a standardized format
 * @see ResponseReceiver    A receiver can intercept the response (ie. for command chaining)
 */
public interface ResponseSender extends IResponseSender
{
    // this is a merge interface between internal response sender and external (plugin interface response sender);
}

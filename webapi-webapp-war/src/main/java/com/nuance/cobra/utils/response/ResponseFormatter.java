package com.nuance.cobra.utils.response;

import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.webapi.ResponseSender;

/**
 * The ResponseFormatter is the class that will format a response to a standardized string.
 *
 * @see ResponseHandler     A handler will contain the sender and receiver to abstract implementations
 * @see ResponseReceiver    A receiver can intercept the response (ie. for command chaining)
 * @see ResponseSender      A sender will send the response to the user
 */
public interface ResponseFormatter
{
    /**
     * This base class implementation will do no formatting on the response
     *
     * @param response
     *         The unformatted response
     *
     * @return The formatted response
     */
    String formatResponse(Object response) throws CobraException;
}
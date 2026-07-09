package com.nuance.plugin.v1.shareable.edf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IEdfMessage
    extends IEdfMessageBase
{
    /**
     * Returns the commandLogString in json format of the edf message. (the name is a bit misleading).
     * @param prettyPrint   Should pretty print the message?
     * @return  Return the JSON string
     * @throws JsonProcessingException
     */
    String toJson(boolean prettyPrint) throws JsonProcessingException;

    /**
     * Returns the name of the api that produced the log
     * @return  The name of the api.
     */
    String getApiName ();

    /**
     * Returns an id to identify
     * @return  Message type.
     */
    String getLogId ();
}

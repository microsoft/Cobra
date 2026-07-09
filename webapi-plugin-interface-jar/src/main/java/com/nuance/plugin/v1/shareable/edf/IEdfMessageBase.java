package com.nuance.plugin.v1.shareable.edf;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IEdfMessageBase
{
    enum EdfMessageType {
        /**
         * Binary blob edf message format.
         */
        binaryBlobMessage,

        /**
         *  JSON formatted edf message. Note this format needs to match the format that EDF expects.
         */
        dataMessage,
    }

    /**
     * Return what type of message this is. The type of message indicates what type of backend the message should post to.
     * @return  Message type.
     */
    @JsonIgnore
    EdfMessageType getMessageType();
}

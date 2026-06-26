package com.nuance.cobra.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class implements a directive sent by the client
 * It is populated by deserialization of the json sent into the object Request
 */
public class RequestDirective
{
    /**
     * This enum implements all the possible directive names sent by the client
     */
    public enum RequestDirectiveName
    {
        @JsonProperty("stop")STOP,
        @JsonProperty("cancel")CANCEL,
        @JsonProperty("startRecognitionTimers")START_RECOGNITION_TIMERS
    }

    private RequestDirectiveName name;

    public RequestDirectiveName getName() {
        return name;
    }

    public void setName(RequestDirectiveName name) {
        this.name = name;
    }

    /**
     * Creates a string to print out the RequestDirective object
     *
     * @return String
     */
    @Override
    public String toString()
    {
        return "\n---name: " + name;
    }
}

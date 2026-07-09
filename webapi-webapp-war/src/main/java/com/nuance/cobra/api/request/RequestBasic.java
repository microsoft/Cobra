package com.nuance.cobra.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class implements the 2 basic elements of a request: type and command. These 2 elements
 * are part of all requests. It is basically a helper for json parsing. The request received is
 * first deserialized to RequestBasic. Then, a specific parsing is done based on type and command
 * to deserialize the complete Request
 */
public class RequestBasic
{
    public class CommandBasic
    {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonProperty("type")
    public Request.RequestType type;

    @JsonProperty("command")
    public CommandBasic command;

    @JsonProperty("applicationKey")
    public String applicationKey;

    @JsonProperty("verificationCode")
    public String verificationCode;

    @JsonProperty("speechRecognitionCodec")
    public String speechRecognitionCodec;

    @JsonProperty("speechSynthesisCodec")
    public String speechSynthesisCodec;

}

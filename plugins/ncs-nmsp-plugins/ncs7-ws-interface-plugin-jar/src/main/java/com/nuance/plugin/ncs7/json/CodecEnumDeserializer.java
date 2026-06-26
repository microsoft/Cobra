package com.nuance.plugin.ncs7.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nuance.cobra.utils.audio.AudioCodec;

import java.io.IOException;

public class CodecEnumDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return AudioCodec.fromString(jsonParser.getValueAsString());
    }
}

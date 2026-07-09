package com.nuance.plugin.v1.shareable.edf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class IEdfMessageTest
{
    final String API_NAME = "my-api";
    final String LOG_ID = "1234654";

    IEdfMessage message = new IEdfMessage()
    {
        @Override
        public String toJson(boolean prettyPrint)
                throws JsonProcessingException
        {
            return null;
        }

        @Override
        public EdfMessageType getMessageType()
        {
            return EdfMessageType.dataMessage;
        }

        @Override
        public String getApiName()
        {
            return API_NAME;
        }

        @Override
        public String getLogId()
        {
            return LOG_ID;
        }
    };


    @Test
    public void basicFunctionalityTest() throws Exception
    {
        assertEquals(IEdfMessage.EdfMessageType.dataMessage, message.getMessageType());
    }

    @Test
    public void edfMessageSerializationTest() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(bos, JsonEncoding.UTF8);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.writeValue(jsonGenerator, message);

        assertTrue(bos.toString().contains(API_NAME));
        assertTrue(bos.toString().contains(LOG_ID));
    }
}
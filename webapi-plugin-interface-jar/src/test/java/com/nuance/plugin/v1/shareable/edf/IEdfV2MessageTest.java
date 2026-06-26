package com.nuance.plugin.v1.shareable.edf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.nuance.plugin.v1.shareable.edf.EdfMessageConstants.DEFAULT_PLUGIN_LOG_TAG;
import static org.junit.Assert.*;

public class IEdfV2MessageTest
{
    Logger logger = LogManager.getLogger(IEdfV2MessageTest.class);

    private static final String CLIENT_APPLICATION_ID = "my-cool-app";
    private static final String CLIENT_APPLICATION_VERSION = "2.5.13";

    private static final String SPECIFIC_VALUE = "value246545634313";
    private static final String SESSION_ID = "12321-123214-1234";
    private static final String CUSTOMER = "1256326541556";
    private static final int SEQUENCE_NO = 545312;
    private static final String DATA = "{ \"field415465\": \"value46431\" }";

    private static final String TIMESTAMP = "1579699520596";

    /**
     * Message with default values (only the methods which do not have defaults are implemented.
     */
    private static class MessageWithDefaults
            implements IEdfV2Message
    {
        @Override
        public IEdfMessage.EdfMessageType getMessageType()
        {
            return IEdfMessage.EdfMessageType.dataMessage;
        }

        @Override
        public String getCustomer()
        {
            return CUSTOMER;
        }

        @Override
        public String getSessionId()
        {
            return SESSION_ID;
        }

        @Override
        public Integer getSequenceNumber()
        {
            return SEQUENCE_NO;
        }

        @Override
        public String getClientApplicationId()
        {
            return CLIENT_APPLICATION_ID;
        }

        @Override
        public String getClientApplicationVersion()
        {
            return CLIENT_APPLICATION_VERSION;
        }

        @Override
        public String getData()
        {
            return DATA;
        }
    }

    /**
     * Check that the provided values are present in the serialized JSON
     */
    private void checkBaseValues(String json)
    {
        assertTrue(json.contains(SESSION_ID));
        assertTrue(json.contains(CUSTOMER));
        assertTrue(json.contains(Integer.toString(SEQUENCE_NO)));
        assertTrue(json.contains(CLIENT_APPLICATION_ID));
        assertTrue(json.contains(CLIENT_APPLICATION_VERSION));
        String escapedData = DATA.replace("\"", "\\\"");
        assertTrue(json.contains(escapedData));
    }

    @Test
    public void basicFunctionalityTest() throws Exception
    {
        IEdfV2Message message = new MessageWithDefaults();
        assertEquals(IEdfV2Message.EdfMessageType.dataMessage, message.getMessageType());
    }

    @Test
    public void edfV2MessageSerializationWithDefaultsTest() throws Exception
    {
        IEdfV2Message message = new MessageWithDefaults();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(bos, JsonEncoding.UTF8);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.writeValue(jsonGenerator, message);

        logger.debug("Serialized message: {}", bos);
        checkBaseValues(bos.toString());

        // fields which should not be present
        assertFalse(bos.toString().contains("properties"));

        assertFalse(bos.toString().contains("API"));
        assertFalse(bos.toString().contains("logId"));
    }

    @Test
    public void edfV2MessageWithValuesSerializationTest() throws Exception
    {
        Map<String, String> properties = new HashMap<>();
        properties.put("key", "value");
        properties.put("key2", SPECIFIC_VALUE);

        IEdfV2Message message = new MessageWithDefaults()
        {
            @Override
            public String getTimestamp()
            {
                return TIMESTAMP;
            }

            @Override
            public Map<String, String> getProperties()
            {
                return properties;
            }
        };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(bos, JsonEncoding.UTF8);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.writeValue(jsonGenerator, message);

        logger.info("Serialized message: {}", bos);
        checkBaseValues(bos.toString());

        assertTrue(bos.toString().contains(SPECIFIC_VALUE));
    }

    @Test
    public void test_getClientApplicationId() {
        IEdfV2Message message = new IEdfV2Message()
        {
            @Override
            public EdfMessageType getMessageType() {
                return null;
            }

            @Override
            public String getCustomer() {
                return "";
            }

            @Override
            public String getSessionId() {
                return "";
            }

            @Override
            public Integer getSequenceNumber() {
                return 0;
            }

            @Override
            public String getData() {
                return null;
            }
        };
        assertNull(message.getClientApplicationId());
    }

    @Test
    public void test_getPluginLogTag() {
        IEdfV2Message message = new MessageWithDefaults();
        assertEquals(DEFAULT_PLUGIN_LOG_TAG, message.getPluginLogTag());
    }
}
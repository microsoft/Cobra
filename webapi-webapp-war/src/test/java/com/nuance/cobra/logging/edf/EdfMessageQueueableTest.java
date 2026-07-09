package com.nuance.cobra.logging.edf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.session.WebApiConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class EdfMessageQueueableTest
{
    class EdfMessage implements EdfMessageQueueable
    {
        JmsMessageType messageType;

        EdfMessage(JmsMessageType messageType)
        {
            this.messageType = messageType;
        }

        @Override
        public String toJson(boolean prettyPrint)
                throws JsonProcessingException
        {
            return null;
        }

        @Override
        public JmsMessageType getJmsType()
        {
            return messageType;
        }

        @Override
        public String getTenantKey()
        {
            return null;
        }

        @Override
        public boolean postingEnabled()
        {
            return false;
        }

        @Override
        public WebApiConfig.Property getEdfEndpointUrlProperty()
        {
            return null;
        }
    }

    class EdfV2Message extends EdfMessage
    {
        JmsMessageType messageType;

        EdfV2Message(JmsMessageType messageType)
        {
            super(messageType);
        }

        @Override
        public EdfMessageFormat getEdfMessageFormat()
        {
            return EdfMessageFormat.EDF_V2_FORMAT;
        }
    }

    @Test
    public void getEdfTagTest()
    {
        EdfMessageQueueable edfMessage = new EdfMessage(EdfMessageQueueable.JmsMessageType.EDF_LOG);
        assertEquals("edfLog.commandLog.", edfMessage.getEdfLogTag());

        edfMessage = new EdfMessage(EdfMessageQueueable.JmsMessageType.AUDIO_LOG);
        assertEquals("edfLog.audioLog.", edfMessage.getEdfLogTag());

        edfMessage = new EdfV2Message(EdfMessageQueueable.JmsMessageType.EDF_LOG);
        assertEquals("edfLog.v2.commandLog.", edfMessage.getEdfLogTag());
    }
}
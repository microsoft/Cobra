package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Utterance;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTTSServiceRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BCPTTSServiceRequestDeserializer {
    private static Logger logger = LogManager.getLogger(BCPTTSServiceRequestDeserializer.class);

    private BCPTTSServiceRequestDeserializer() {
    }

    public static BCPTTSServiceRequestMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPTTSServiceRequestMessage bcpTTSServiceMsg = new BCPTTSServiceRequestMessage(version, sessionId, transactionId);

        try {
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            BCPTTSServiceRequestSAXHandler handler = new BCPTTSServiceRequestSAXHandler(bcpTTSServiceMsg);
            NMSPMessageDeserializer.parseXMLPayload(payload, handler);
            int index = 1; // The primary payload index is 0
            while (message.isReadable()) {
                // Read the binary payload's length
                length = message.readUnsignedInt();
                payload = new byte[(int) length];
                message.readBytes(payload);

                String strVal = new String(payload);//StringHelper.UTF8_CHARSET

                // Insert the binary data in the proper utterance based on the index...
                for (Parameter parameter : bcpTTSServiceMsg.getParameters()) {
                    if (parameter.getIndex() == index) {
                        parameter.setValue(strVal);
                        break;
                    }
                }

                // Insert the binary data in the proper utterance based on the index...
                for (Utterance utterance : bcpTTSServiceMsg.getUtterances()) {
                    if (utterance.getIndex() == index) {
                        utterance.setText(payload);
                        break;
                    }
                }


                index++;
            }
        } catch (IndexOutOfBoundsException iobe) {
            logger.error("Invalid BCP TTS SERVICE REQUEST message", iobe);
            throw new CorruptedFrameException("Invalid BCP TTS SERVICE REQUEST message");
        }

        // Validate mandatory parameters
        if (bcpTTSServiceMsg.getRequestId() == -1) {
            logger.warn("Invalid BCP TTS SERVICE REQUEST message: missing mandatory element 'request id'");
            throw new CorruptedFrameException("Invalid BCP TTS SERVICE REQUEST message: missing mandatory element 'request id'");
        }


        return bcpTTSServiceMsg;
    }
}

/*
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2015 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.BCPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPDataMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.COPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.*;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.VAPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordEndMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.Vector;

public class NMSPMessageHelper {
    private static Logger logger = LogManager.getLogger();

    static final short QUERY_PARAM = 0x0203;

    static final int AUDIO_PARAM = 1;
    static final int TEXT_PARAM = 2;
    static final int CHOICE_PARAM = 3;
    static final int DATA_PARAM = 4;
    static final int DICT_PARAM = 5;
    static final int SEQ_START_PARAM = 6;
    static final int SEQ_CHUNK_PARAM = 7;
    static final int SEQ_END_PARAM = 8;

    private void check(String name, Object val) {
        if (val == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    public COPConnectMessage createCOPConnect(final Codec fromCodec, final Codec toCodec, final String nmaid, final Vector<Parameter> parameters, final String parentEventId) {
        final COPConnectMessage msg = new COPConnectMessage((short) COPDefaultParams.VERSION.getVersion());

        // codecs are not required for data upload
        check("nmaid", nmaid);
        check("parameters", parameters);

        msg.setFromCodec(fromCodec);
        msg.setToCodec(toCodec);
        msg.setNmaid(nmaid);
        msg.setParent(parentEventId);
        msg.setParameters(parameters);

        return msg;
    }

    public static byte[] serialize(PdxObject pdxObject) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        PdxPacker packer = new PdxPacker();
        packer.packDictionary(pdxObject.getDict());
        byte[] payload = packer.toArray();

        ByteBuf buffer = Unpooled.buffer(2 + payload.length).order(NMSPMessage.byteOrder);

        // Write the command
        buffer.writeShort(pdxObject.getCommand().getValue());
        // Write payload
        buffer.writeBytes(payload);

        byte[] retData = buffer.array();
        return retData;
    }

    public byte[] serializeWithoutHeader(PdxObject pdxObject) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        PdxPacker packer = new PdxPacker();
        packer.packDictionary(pdxObject.getDict());
        byte[] payload = packer.toArray();

        ByteBuf buffer = Unpooled.buffer(payload.length).order(NMSPMessage.byteOrder);

        // Write payload
        buffer.writeBytes(payload);

        byte[] retData = buffer.array();
//    dump("NMSPMessageHelper.serializeWithoutHeader()", retData);
        return retData;
    }


    public BCPBeginMessage createBCPBegin(byte[] sessionId, short transactionId, PdxDictionary dict) {

        // logger.trace("createBCPBegin:" + dict.toString());
        BCPBeginMessage msg = new BCPBeginMessage((short) BCPDefaultParams.VERSION.getVersion(), sessionId, transactionId);

        PdxObject pdxObjectExpected = new PdxObject(PdxCommand.Connect, dict);

        msg.setQueryBegin(dict);
        try {
            msg.setPayload(serializeWithoutHeader(pdxObjectExpected));
        } catch (IOException e) {
            logger.error("Could not pack PDX:" + dict.toString(), e);
        }
        return msg;
    }

    public VAPRecordBeginMessage createVAPRecordBegin(long audioId, Codec fromCodec) {
        VAPRecordBeginMessage msg = new VAPRecordBeginMessage((short) VAPDefaultParams.VERSION.getVersion());
        msg.setCodec(fromCodec);
        msg.setAudioId(audioId);
        return msg;
    }

    public class BCPDataPacker {
        byte[] _sessionId;
        short _transactionId;
        PdxDictionary _dict;

        BCPDataPacker(byte[] sessionId, short transactionId, String name, int type) {
            _sessionId = sessionId;
            _transactionId = transactionId;


            _dict = new PdxDictionary();
            _dict.put("name", name);
            _dict.put("type", type);
        }

        void put(String key, Object val) {
            try {
                _dict.put(key, val);
            } catch (Exception e) {
                logger.error("Could not put BCPDataMessage key", e);
            }
        }

        BCPDataMessage pack() {
            BCPDataMessage msg = new BCPDataMessage((short) BCPDefaultParams.VERSION.getVersion(), _sessionId, _transactionId);

            PdxObject obj = new PdxObject(PdxCommand.QueryParam, _dict);
            try {
                msg.setPayload(serialize(obj));

            } catch (Exception e) {
                logger.error("Could not pack BCPDataMessage", e);
            }
            return msg;
        }
    }


    public BCPDataMessage createBCPDataAudioParam(byte[] sessionId, short transactionId, String name, int audioId) {
        BCPDataPacker packer = new BCPDataPacker(sessionId, transactionId, name, AUDIO_PARAM);
        packer.put("buffer_id", audioId);
        return packer.pack();
    }


    private BCPDataMessage textParam(byte[] sessionId, short transactionId, String name, int type, String txt) {
        BCPDataPacker packer = new BCPDataPacker(sessionId, transactionId, name, type);
        packer.put("text", txt);
        return packer.pack();
    }

    public BCPDataMessage createBCPDataChoiceParam(byte[] sessionId, short transactionId, String name, String choice) {
        return textParam(sessionId, transactionId, name, CHOICE_PARAM, choice);
    }

    public BCPDataMessage createBCPDataTextParam(byte[] sessionId, short transactionId, String name, String choice) {
        return textParam(sessionId, transactionId, name, TEXT_PARAM, choice);
    }


    public BCPDataMessage createBCPDataDataParam(byte[] sessionId, short transactionId, String name, byte[] data) {
        BCPDataPacker packer = new BCPDataPacker(sessionId, transactionId, name, DATA_PARAM);
        packer.put("data", data);
        return packer.pack();
    }


    private BCPDataMessage dictParam(byte[] sessionId, short transactionId, String name, int type, PdxDictionary dict) {
        BCPDataPacker packer = new BCPDataPacker(sessionId, transactionId, name, type);
        packer.put("dict", dict);
        return packer.pack();
    }

    public BCPDataMessage createBCPDataDictParam(byte[] sessionId, short transactionId, String name, PdxDictionary dict) {
        return dictParam(sessionId, transactionId, name, DICT_PARAM, dict);
    }

    public BCPDataMessage createBCPDataSeqStartParam(byte[] sessionId, short transactionId, String name, PdxDictionary dict) {
        return dictParam(sessionId, transactionId, name, SEQ_START_PARAM, dict);
    }

    public BCPDataMessage createBCPDataSeqChunkParam(byte[] sessionId, short transactionId, String name, PdxDictionary dict) {
        return dictParam(sessionId, transactionId, name, SEQ_CHUNK_PARAM, dict);
    }

    public BCPDataMessage createBCPDataSeqEndParam(byte[] sessionId, short transactionId, String name, PdxDictionary dict) {
        return dictParam(sessionId, transactionId, name, SEQ_END_PARAM, dict);
    }


    public VAPRecordMessage createVAPRecord(long audioId, byte[] audio) {
        VAPRecordMessage msg = new VAPRecordMessage((short) VAPDefaultParams.VERSION.getVersion());
        msg.setAudioId(audioId);
        msg.addAudio(audio);
        return msg;
    }

    public VAPRecordEndMessage createVAPRecordEnd(long audioId) {
        VAPRecordEndMessage msg = new VAPRecordEndMessage((short) VAPDefaultParams.VERSION.getVersion());
        msg.setAudioId(audioId);
        return msg;
    }

    public BCPDataMessage createBCPDataQueryEnd(byte[] sessionId, short _transaction) {

        BCPDataMessage msg = new BCPDataMessage((short) BCPDefaultParams.VERSION.getVersion(), sessionId, _transaction);

        PdxObject obj = new PdxObject(PdxCommand.QueryEnd, new PdxDictionary());
        try {
            msg.setPayload(serialize(obj));

        } catch (Exception e) {
            logger.error("Could not pack BCPDataMessage - Query end", e);
        }
        return msg;
    }

}


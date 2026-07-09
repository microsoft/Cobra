/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.utils.ByteHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Represents a single PDX datum while decoding a PDX block.
/// </summary>
public class PdxPacket {
    private static final Logger logger = LogManager.getLogger();
    private PdxClass clz;
    private int startOffset;
    private int payloadOffset;
    private int payloadSize;
    private byte[] data;

    /**
     * Builds a PdxPacket from the contents of the <code>pdata</code> at offset <code>start</code>
     *
     * @param pdata The block of pdx encoded bytes
     * @param start The offset into the byte array
     * @throws PdxUnsupportedDataTypeException
     * @throws PdxException
     */
    public PdxPacket(byte[] pdata, int start) throws PdxUnsupportedDataTypeException, PdxException {
        byte firstByte = pdata[start + 0];
        this.clz = PdxClass.createPdxClassFromByte(firstByte);
        if (this.clz == null) {
            throw new PdxUnsupportedDataTypeException(Byte.toString(firstByte));
        }

        this.startOffset = start;
        this.payloadOffset = -1;
        this.data = pdata;
        this.payloadSize = -1;

        int sizeBits = (0xFF & pdata[start + 1]);

        if (sizeBits <= 127) {
            this.payloadSize = sizeBits;
            this.payloadOffset = this.startOffset + 2;
        } else if (sizeBits == 0x81) {
            this.payloadSize = (0xFF & this.data[start + 2]);
            this.payloadOffset = this.startOffset + 3;
        } else if (sizeBits == 0x82) {
            int s;
            try {
                s = (0xFFFF & ByteHelper.toInt16(this.data, this.startOffset + 2));
            } catch (Exception e) {
                throw new PdxException(e);
            }

            this.payloadSize = s; // Integer.reverseBytes(s);
            this.payloadOffset = this.startOffset + 4;
        } else if (sizeBits == 0x84) {
            int v;
            try {
                v = (0xFFFFFFFF & ByteHelper.toInt32(this.data, this.startOffset + 2));
            } catch (Exception e) {
                throw new PdxException(e);
            }
            this.payloadSize = v; // Integer.reverseBytes(v);
            this.payloadOffset = this.startOffset + 6;
        } else {
            throw new PdxException("Size could not be parsed");
        }
    }

    public PdxClass getPdxClass() {
        return clz;
    }

    public void setPdxClass(PdxClass clz) {
        this.clz = clz;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getPayloadOffset() {
        return payloadOffset;
    }

    public void setPayloadOffset(int payloadOffset) {
        this.payloadOffset = payloadOffset;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    // / <summary>
    // / Pdx uses variable sized headers. This property contains the number of bytes used to encode
    // / this packets header
    // / </summary>
    public int getHeaderSize() {
        return (this.payloadOffset - this.startOffset);
    }

    /**
     * The number of bytes used to encode this packet's data (string, number, dict, list, etc...)
     *
     * @return
     */
    public int getPayloadSize() {
        return this.payloadSize;
    }

    public void setPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    /**
     * The total number of bytes used to encode this packet
     *
     * @return
     */
    public int getSize() {
        return getHeaderSize() + getPayloadSize();
    }

    /*
     * Returns an XML string representation of the packet
     */
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PdxToXml dumper = new PdxToXml(sw);
        try {
            dumper.write(this);
        } catch (IOException e) {
            logger.debug("Failed to convert pdxObject to string.", e);
        }
        String rv = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            logger.debug("Failed to convert pdxObject to string.", e);
        }
        return rv;
    }
}

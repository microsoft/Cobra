/**
 * Copyright 2006-2011, Nuance Communications Inc. All rights reserved.
 * <p>
 * Nuance Communications, Inc. provides this document without representation
 * or warranty of any kind. The information in this document is subject to
 * change without notice and does not represent a commitment by Nuance
 * Communications, Inc. The software and/or databases described in this
 * document are furnished under a license agreement and may be used or
 * copied only in accordance with the terms of such license agreement.
 * Without limiting the rights under copyright reserved herein, and except
 * as permitted by such license agreement, no part of this document may be
 * reproduced or transmitted in any form or by any means, including, without
 * limitation, electronic, mechanical, photocopying, recording, or otherwise,
 * or transferred to information storage and retrieval systems, without the
 * prior written permission of Nuance Communications, Inc.
 * <p>
 * Nuance, the Nuance logo, Nuance Recognizer, and Nuance Vocalizer are
 * trademarks or registered trademarks of Nuance Communications, Inc. or its
 * affiliates in the United States and/or other countries. All other
 * trademarks referenced herein are the property of their respective owners.
 **/

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

import java.io.ByteArrayOutputStream;
public class PDXClass {

    public static final short UTF8 = (short) (0xFF & 0xC1);    //193
    protected static final short BYTES = 0x04;                 //004
    protected static final short NULL = 0x05;                  //005
    protected static final short DOUBLE = 0x06;                   //006
    protected static final short BOOLEAN = 0x07;               //007
    protected static final short SEQ = 0x10;                   //016
    protected static final short ASCII = 0x16;                 //022
    protected static final short INT = (short) (0xC0 & 0xFF);  //192
    protected static final short DICT = (short) (0xFF & 0xE0); //224
    private short CLASS;

    public PDXClass(short c) {
        this.CLASS = c;
    }

    public short getType() {
        return this.CLASS;
    }

    public int getLength(byte[] byteClass, int offset) {
        int length_bits = (byteClass[offset] & 0xFF);
        if (length_bits == 0x81) {
            return (byteClass[offset + 1] & 0xFF);
        } else if (length_bits == 0x82) {
            return (((byteClass[offset + 1] & 0xFF) << 8)
                    + (byteClass[offset + 2] & 0xFF));
        } else if (length_bits == 0x84) {
            return (((byteClass[offset + 1] & 0xFF) << 24)
                    + ((byteClass[offset + 2] & 0xFF) << 16)
                    + ((byteClass[offset + 3] & 0xFF) << 8)
                    + (byteClass[offset + 4] & 0xFF));
        } else {
            return length_bits;
        }
    }

    public int getLengthSize(int length) {
        if (length <= 127) {
            return 1;
        } else if (length <= 255) {
            return 2;
        } else if (length <= 65535) {
            return 3;
        } else {
            return 5;
        }
    }

    public byte[] toByteArray(byte[] content) {
        ByteArrayOutputStream pdxer = new ByteArrayOutputStream();
        pdxer.write(this.CLASS & 0xFF);
        if (content.length > 65535) {
            pdxer.write(0x84);
            pdxer.write((content.length >> 24) & 0xFF);
            pdxer.write((content.length >> 16) & 0xFF);
            pdxer.write((content.length >> 8) & 0xFF);
            pdxer.write(content.length & 0xFF);
        } else if (content.length > 255) {
            pdxer.write(0x82);
            pdxer.write((content.length >> 8) & 0xFF);
            pdxer.write(content.length & 0xFF);
        } else if (content.length > 127) {
            pdxer.write(0x81);
            pdxer.write(content.length);
        } else {
            pdxer.write(content.length);
        }

        pdxer.write(content, 0, content.length);
        return pdxer.toByteArray();
    }
}

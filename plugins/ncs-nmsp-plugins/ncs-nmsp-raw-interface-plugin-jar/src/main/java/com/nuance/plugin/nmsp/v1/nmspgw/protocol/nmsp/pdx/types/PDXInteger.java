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

/**
 * PDX integer representation
 *
 */
public class PDXInteger extends PDXClass {
    private int value;

    public PDXInteger(int value) {
        super(PDXClass.INT);
        this.value = value;
    }

    public PDXInteger(byte[] value) {
        super(PDXClass.INT);
        if (value.length == 1) {
            this.value = (value[0] & 0xFF);
        } else if (value.length == 2) {
            this.value = (((value[1] & 0xFF) << 8) + (value[0] & 0xFF));
        } else if (value.length == 3) {
            this.value = (((value[2] & 0xFF) << 16) + ((value[1] & 0xFF) << 8) + (value[0] & 0xFF));
        } else {
            this.value = (((value[3] & 0xFF) << 24) + ((value[2] & 0xFF) << 16)
                    + ((value[1] & 0xFF) << 8) + (value[0] & 0xFF));
        }
    }

    public int getValue() {
        return this.value;
    }

    public byte[] toByteArray() {
        byte[] v;
        if (Math.abs(this.value) < 0x80) {
            v = new byte[1];
            v[0] = (byte) (this.value & 0xFF);
        } else if (Math.abs(this.value) < 0x8000) {
            v = new byte[2];
            v[0] = (byte) (this.value & 0xFF);
            v[1] = (byte) ((this.value >> 8) & 0xFF);
        } else {
            v = new byte[4];
            v[0] = (byte) (this.value & 0xFF);
            v[1] = (byte) ((this.value >> 8) & 0xFF);
            v[2] = (byte) ((this.value >> 16) & 0xFF);
            v[3] = (byte) ((this.value >> 24) & 0xFF);
        }

        return super.toByteArray(v);
    }
}

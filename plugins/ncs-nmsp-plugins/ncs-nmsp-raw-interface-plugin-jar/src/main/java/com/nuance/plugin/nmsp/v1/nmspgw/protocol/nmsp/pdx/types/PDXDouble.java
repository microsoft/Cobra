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
public class PDXDouble extends PDXClass {
    private Double value;

    public PDXDouble(double value) {
        super(PDXClass.DOUBLE);
        this.value = new Double(value);
    }

    public PDXDouble(Double value) {
        super(PDXClass.DOUBLE);
        this.value = value;
    }

    public PDXDouble(byte[] newValue) {
        super(PDXClass.DOUBLE);
        long data = ((long) (0xff & newValue[7]) << 56 |
                (long) (0xff & newValue[6]) << 48 |
                (long) (0xff & newValue[5]) << 40 |
                (long) (0xff & newValue[4]) << 32 |
                (long) (0xff & newValue[3]) << 24 |
                (long) (0xff & newValue[2]) << 16 |
                (long) (0xff & newValue[1]) << 8 |
                (long) (0xff & newValue[0]) << 0);
        value = new Double(Double.longBitsToDouble(data));
    }

    public double getValue() {
        return this.value.doubleValue();
    }

    public byte[] toByteArray() {
        long x = Double.doubleToRawLongBits(value);
        byte[] data = new byte[]{(byte) x,
                (byte) (x >>> 8),
                (byte) (x >>> 16),
                (byte) (x >>> 24),
                (byte) (x >>> 32),
                (byte) (x >>> 40),
                (byte) (x >>> 48),
                (byte) (x >>> 56)};
        return super.toByteArray(data);
    }

}



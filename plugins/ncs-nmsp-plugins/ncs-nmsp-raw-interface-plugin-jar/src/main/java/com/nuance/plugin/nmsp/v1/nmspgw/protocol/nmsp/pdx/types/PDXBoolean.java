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
 * PDX ASCII string representation
 */
public class PDXBoolean extends PDXClass {
    private Boolean value;

    public PDXBoolean(Boolean value) {
        super(PDXClass.BOOLEAN);
        this.value = value;
    }

    public PDXBoolean(boolean value) {
        super(PDXClass.BOOLEAN);
        this.value = value;
    }

    public PDXBoolean(byte[] value) {
        super(PDXClass.BOOLEAN);
        if (value == null) {
            this.value = false;
        } else if (value.length < 1) {
            this.value = false;
        } else {
            this.value = value[0] != 0;
        }
    }

    public boolean getValue() {
        return this.value;
    }

    public byte[] toByteArray() {
        byte[] retByte = new byte[1];
        retByte[0] = (byte) (value ? 0xFF : 0x00);
        return super.toByteArray(retByte);
    }
}

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

public enum PdxCommand {
    Connect(0x0001), Log(0x0002), QueryBegin(0x0202), QueryParam(0x0203), QueryEnd(0x0204), QueryResult(0x7201), QueryError(0x7202), QueryRetry(0x7205);

    private final int value;

    PdxCommand(int value) {
        this.value = value;
    }

    public static PdxCommand parseValue(int value) {
        for (PdxCommand c : PdxCommand.values()) {
            if (c.getValue() == value) {
                return c;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}

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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPDataMessage;

public class PdxObject {
    private PdxCommand command;
    private PdxDictionary dict;

    public PdxObject(BCPDataMessage bcpDataMessage) throws PdxUnsupportedDataTypeException, PdxException {
        this(PdxCommand.parseValue(
                (short) ((0xff & bcpDataMessage.getPayload()[0]) | (0xff & bcpDataMessage.getPayload()[1]) << 8)),
                PdxUnpacker.unpackDict(bcpDataMessage.getPayload(), 2));
    }

    public PdxObject(PdxCommand command, PdxDictionary dict) {
        this.command = command;
        this.dict = dict;
    }

    public PdxCommand getCommand() {
        return command;
    }

    public void setCommand(PdxCommand command) {
        this.command = command;
    }

    public PdxDictionary getDict() {
        return dict;
    }

    public void setDict(PdxDictionary dict) {
        this.dict = dict;
    }
}

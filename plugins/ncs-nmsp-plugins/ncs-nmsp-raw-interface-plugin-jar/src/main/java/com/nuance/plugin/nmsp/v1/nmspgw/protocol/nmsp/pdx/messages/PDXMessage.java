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

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.messages;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;

import java.io.ByteArrayOutputStream;

public class PDXMessage extends PDXDictionary {
    //#ifdef NMSP_LOG
//@    private static final Log log = LogFactory.getLog(PDXMessage.class);
    //#endif

    //  protected static final short GetScriptUpdate         = 0x0101; //00257
    protected static final short QueryBegin = 0x0202; //00514
    protected static final short QueryParameter = 0x0203; //00515
    protected static final short QueryEnd = 0x0204; //00516
    protected static final short EnrollementSegment = 0x0400; //01024
    //  protected static final short EnrollementComplete     = 0x0401; //01025
    protected static final short EnrollmentAudio = 0x0402; //01026
    //  protected static final short DiagnosticLogUpdate     = 0x0500; //01280
    protected static final short QueryResult = 0x7201; //29185
    protected static final short QueryError = 0x7202; //29186
    //  protected static final short QueryDisambiguate       = 0x7203; //29187
//  protected static final short QueryNarrow             = 0x7204; //29188
    protected static final short QueryRetry = 0x7205; //29189
    private short cmd;
//  protected static final short EnrollementText         = 0x7400; //29696
//  protected static final short ScriptUpdateAvailable   = 0x7300; //29440
//  protected static final short UpdateChunk             = 0x7301; //29441
//  protected static final short NoScriptUpdateAvailable = 0x7302; //29442

    // A PDX message is a dictionary i.e. a set of key/value pair. The key is a PDXAsciiString
    // and the value is one of either: PDXAsciiString, PDXByteString, PDXDictionary, PDXInteger,
    // PDXSequence, PDXUTF8String
    public PDXMessage(short cmd) {
        super();
        //#ifdef NMSP_LOG2
//@        log.debug("PDXMessage() " + cmd);
        //#endif
        this.cmd = cmd;
    }

    public PDXMessage(short cmd, byte[] input) {
        super(input, true);
        this.cmd = cmd;
    }

    public short getCommandCode() {
        //#ifdef NMSP_LOG2
//@        log.debug("PDXMessage.getCommandCode()");
        //#endif
        return cmd;
    }

    public byte[] getMessage() {
        //#ifdef NMSP_LOG2
//@        log.debug("PDXMessage.getMessage()");
        //#endif
        return super.toByteArray();
    }

    public byte[] toByteArray() {
        //#ifdef NMSP_LOG2
//@        log.debug("PDXMessage.toByteArray()");
        //#endif
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(cmd & 0xFF);
        output.write((cmd & 0xFF00) >> 8);

        try {
            output.write(super.toByteArray());
        } catch (java.io.IOException ioe) {
            //#ifdef NMSP_LOG
//@            log.error("PDXMessage.toByteArray() " + ioe.toString() + ". ");
            //#endif
        }
        return output.toByteArray();
    }
}

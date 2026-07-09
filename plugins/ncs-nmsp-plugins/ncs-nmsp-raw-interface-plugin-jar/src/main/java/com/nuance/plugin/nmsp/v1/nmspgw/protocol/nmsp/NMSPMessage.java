/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;

import java.nio.ByteOrder;

/**
 * @author epigeon
 *
 */
public class NMSPMessage {
    public static final short VAP = 0x01;
    public static final short BCP = 0x02;
    public static final short COP = 0x03;
    public static final short HANDSHAKEBYTE = 0x0F;

    public static final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    // Message Header
    // --------------
    // 1 byte Protocol
    // 1 byte Version
    // 2 bytes Command
    // 4 bytes Length
    public static final int protocolIndex = 0;
    public static final int versionIndex = 1;
    public static final int commandIndex = 2;
    public static final int lengthIndex = 4;
    public static final int headerSize = 8;

    private final short protocol;
    private final short version;
    private final int command;

    /**
     * @param protocol
     * @param version
     * @param command
     */
    public NMSPMessage(short protocol, short version, int command) {
        this.protocol = protocol;
        this.version = version;
        this.command = command;
    }

    public static String protocolToString(short protocol) {
        switch (protocol) {
            case VAP:
                return "VAP";
            case BCP:
                return "BCP";
            case COP:
                return "COP";
            case HANDSHAKEBYTE:
                return "HANDSHAKEBYTE";
            default:
                return "UNKNOWN";
        }
    }

    public static String commandToString(short protocol, int command) {
        switch (protocol) {
            case VAP:
                return VAPMessage.commandToString(command);
            case BCP:
                return BCPMessage.commandToString(command);
            case COP:
                return COPMessage.commandToString(command);
            case HANDSHAKEBYTE:
                return "HANDSHAKEBYTE";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * @return the protocol
     */
    public short getProtocol() {
        return protocol;
    }

    /**
     * @return the version
     */
    public short getVersion() {
        return version;
    }

    /**
     * @return the command
     */
    public int getCommand() {
        return command;
    }

    public String toString() {
        return protocolToString(protocol);
    }
}

/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;

/**
 * @author epigeon
 *
 */
public class COPMessage extends NMSPMessage {
    // client commands
    public static final int COP_CONNECT = 0x0100;
    public static final int COP_PING_RESPONSE = 0x0103;
    public static final int COP_ROUTE_REQUEST = 0x0104;
    public static final int COP_CONFIRM = 0x0106;
    // server commands
    public static final int COP_CONNECTED = 0x0101;
    public static final int COP_PING_REQUEST = 0x0102;
    public static final int COP_ROUTE_RESPONSE = 0x0105;
    public static final int COP_CONNECT_FAILED = 0x0300;
    // 2-way commands
    public static final int COP_DISCONNECT = 0x0200;

    // Supported versions
    public static final short COP_MINIMUM_LEGACY_VERSION = 0x17;
    public static final short COP_MINIMUM_VERSION = 0x20;
    public static final short COP_MAXIMUM_VERSION = 0x20;

    /**
     * @param version
     * @param command
     */
    public COPMessage(short version, int command) {
        super(NMSPMessage.COP, version, command);
    }

    @Override
    public String toString() {
        return commandToString(getCommand()) + " protocolVersion: " + getVersion();
    }

    public static String commandToString(int command) {
        switch (command) {
            case COP_CONNECT:
                return "COP_CONNECT";
            case COP_PING_RESPONSE:
                return "COP_PING_RESPONSE";
            case COP_ROUTE_REQUEST:
                return "COP_ROUTE_REQUEST";
            case COP_CONFIRM:
                return "COP_CONFIRM";
            case COP_CONNECTED:
                return "COP_CONNECTED";
            case COP_PING_REQUEST:
                return "COP_PING_REQUEST";
            case COP_ROUTE_RESPONSE:
                return "COP_ROUTE_RESPONSE";
            case COP_CONNECT_FAILED:
                return "COP_CONNECT_FAILED";
            case COP_DISCONNECT:
                return "COP_DISCONNECT";
            default:
                return "UNKNOWN_COMMAND";
        }
    }
}

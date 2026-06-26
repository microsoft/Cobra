/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 *
 */
public class COPMessageSerializer {
    private COPMessageSerializer() {
    }

    public static ByteBuf serialize(COPMessage message) {
        switch (message.getCommand()) {
            case COPMessage.COP_CONNECTED:
                return COPConnectedSerializer.serialize((COPConnectedMessage) message);
            case COPMessage.COP_PING_REQUEST:
                return COPPingRequestSerializer.serialize((COPPingRequestMessage) message);
            // No longer supported
            // case COPMessage.COP_ROUTE_RESPONSE:
            // break;
            case COPMessage.COP_CONNECT_FAILED:
                return COPConnectFailedSerializer.serialize((COPConnectFailedMessage) message);
            case COPMessage.COP_DISCONNECT:
                return COPDisconnectSerializer.serialize((COPDisconnectMessage) message);
            default:
                return Unpooled.EMPTY_BUFFER;
        }
    }

}

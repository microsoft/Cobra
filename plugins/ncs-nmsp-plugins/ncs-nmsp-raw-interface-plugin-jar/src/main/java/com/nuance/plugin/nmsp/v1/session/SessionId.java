/*
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2016 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Author: bertrand_braschi
 * Date  : May 27, 2016
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.session;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SessionId {

    private final String sessionId;
    private final byte[] sessionIdBytes;

    SessionId(String sessionID) {
        sessionId = sessionID;
        sessionIdBytes = sessionID.getBytes(StandardCharsets.UTF_8);
    }

    SessionId(int sessionIDLength) {
        final UUID uuid = UUID.randomUUID();
        sessionId = uuid.toString();
        sessionIdBytes = generateSessionId(uuid, sessionIDLength);
    }

    private static byte[] generateSessionId(final UUID uuid, int sessionIdLength) {
        final ByteBuffer buffer = ByteBuffer.allocate(sessionIdLength);
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else { // TODO to be tested
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(uuid.getMostSignificantBits());
        }

        return buffer.array();
    }

    public String asString() {
        return sessionId;
    }

    public byte[] asByteArray() {
        return sessionIdBytes;
    }
}

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class BCPMdsDeserializerParametrizedTest {

    private String message;

    public BCPMdsDeserializerParametrizedTest(String message) {
        this.message = message;
    }

    // Parameter data method
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Each array corresponds to one of the original test cases
                {"<body>\n  <requestid>-2</requestid>\n</body>"},
                {"<body>\n  <ttl>-2</ttl>\n</body>"},
                {"<body>\n  <requestid>9h8d9g17f3</requestid>\n</body>"}
        });
    }

    @Test(expected = CorruptedFrameException.class)
    public void test_invalidInput() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        BCPMdsDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), byteBuf);
    }
}

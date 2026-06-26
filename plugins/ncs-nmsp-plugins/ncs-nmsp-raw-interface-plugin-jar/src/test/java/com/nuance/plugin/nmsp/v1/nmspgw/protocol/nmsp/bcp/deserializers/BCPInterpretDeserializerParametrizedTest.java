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
public class BCPInterpretDeserializerParametrizedTest {

    private final String message;

    public BCPInterpretDeserializerParametrizedTest(String message) {
        this.message = message;
    }

    // Parameter data method
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Each array corresponds to one of the original test cases, with a type indicator
                {"<body>\n  <rid>-2</rid>\n</body>"},
                {"<body>\n  <resid>-2</resid>\n</body>"},
                {"<body>\n  <i55 w=\"-55\"/>\n</body>"},
                {"<body>\n  <svsp p=\"fakeName\" v=\"-412\"/>\n</body>"},
                {"<body>\n  <slog p=\"fakeName\" v=\"-412\"/>\n</body>"},
                {"<body>\n  <nsslog p=\"fakeName\" v=\"-412\"/>\n</body>"},
                {"<body>\n  <gwlog p=\"fakeName\" v=\"-412\"/>\n</body>"},
                {"<body>\n  <rid>9h8d9g17f3</rid>\n</body>"}
        });
    }

    @Test(expected = CorruptedFrameException.class)
    public void test_invalidInput() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        BCPInterpretDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), (short) 0, byteBuf);
    }
}

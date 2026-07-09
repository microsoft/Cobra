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
public class BCPGenerateAudioDeserializerParametrizedTest {

    private final String message;

    public BCPGenerateAudioDeserializerParametrizedTest(String message) {
        this.message = message;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"<body>\n  <rid>-2</rid>\n</body>"},
                {"<body>\n  <resid>-2</resid>\n</body>"},
                {"<body>\n  <bps>-2</bps>\n</body>"},
                {"<body>\n  <aid>-2</aid>\n</body>"},
                {"<body>\n  <bs>-2</bs>\n</body>"},
                {"<body>\n  <p1>\n    <d>-2</d>\n  </p1>\n</body>"},
                {"<body>\n  <p1>\n    <t>-2</t>\n  </p1>\n</body>"},
                {"<body>\n  <rid>9h8d9g17f3</rid>\n</body>"}
        });
    }

    @Test(expected = CorruptedFrameException.class)
    public void test_invalidInput() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        BCPGenerateAudioDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), (short) 0, byteBuf);
    }
}

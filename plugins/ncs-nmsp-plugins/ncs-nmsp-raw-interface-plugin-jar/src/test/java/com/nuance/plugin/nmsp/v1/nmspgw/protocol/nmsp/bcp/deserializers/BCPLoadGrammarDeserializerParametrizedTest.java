package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class BCPLoadGrammarDeserializerParametrizedTest {

    private final String message;

    // Constructor for the parameters
    public BCPLoadGrammarDeserializerParametrizedTest(String message) {
        this.message = message;
    }

    // Parameter data method
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Each array corresponds to one of the original test cases
                {"<body>\n  <rid>-2</rid>\n</body>"},
                {"<body>\n  <resid>-2</resid>\n</body>"},
                {"<body>\n  <g1>\n    <d>-2</d>\n  </g1>\n</body>"},
                {"<body>\n  <g1>\n    <t>-2</t>\n  </g1>\n</body>"},
                {"<body>\n  <g1>\n    <t>id</t>\n  </g1>\n</body>"},
                {"<body>\n  <rid>9h8d9g17f3</rid>\n</body>"}
        });
    }

    @Test(expected = CorruptedFrameException.class)
    public void testDeserialization() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        BCPLoadGrammarDeserializer.deserialize((short)0, "djih89ud1".getBytes(), (short)0, byteBuf);
    }
}

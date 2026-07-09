package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class BCPLoadResourceDeserializerParametrizedTest {

    private final String message;
    private final boolean expectException;

    // Constructor for the parameters
    public BCPLoadResourceDeserializerParametrizedTest(String message, boolean expectException) {
        this.message = message;
        this.expectException = expectException;
    }

    // Parameter data method
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Each array corresponds to one of the original test cases
                {"<body>\n  <rid>-2</rid>\n</body>", true},
                {"<body>\n  <resid>-2</resid>\n</body>", true},
                {"<body>\n  <oper/>\n  <oper/>\n</body>", true},
                {"<body>\n  <rid>5</rid>\n  <reco/>\n  <tts/>\n</body>", false},
                {"<body>\n  <rid>5</rid>\n  <tts/>\n  <reco/>\n</body>", false},
                {"<body>\n  <rid>5</rid>\n  <dict/>\n</body>", false},
                {"<body>\n  <reco/>\n  <reco/>\n</body>", true},
                {"<body>\n  <tts/>\n  <tts/>\n</body>", true},
                {"<body>\n  <tts/>\n  <dict/>\n</body>", true},
                {"<body>\n  <codec>1</codec>\n</body>", true},
                {"<body>\n  <deferloading>Meow</deferloading>\n</body>", true},
                {"<body>\n  <clientAudioStreamingMaxIdleTimeMSec>-500</clientAudioStreamingMaxIdleTimeMSec>\n</body>", true},
                {"<body>\n  <rid>9h8d9g17f3</rid>\n</body>", true}
        });
    }

    @Test
    public void testDeserialization() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());

        try {
            BCPLoadResourceDeserializer.deserialize((short)0, "djih89ud1".getBytes(), (short)0, byteBuf);
            if (expectException) {
                fail("Expected CorruptedFrameException but none was thrown");
            }
        } catch (CorruptedFrameException e) {
            if (!expectException) {
                fail("Unexpected CorruptedFrameException was thrown");
            }
        }
    }
}

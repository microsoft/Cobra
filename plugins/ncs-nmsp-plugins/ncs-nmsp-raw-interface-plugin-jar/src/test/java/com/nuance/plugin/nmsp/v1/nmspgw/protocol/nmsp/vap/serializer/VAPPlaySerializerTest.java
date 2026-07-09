package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.serializer;

import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.serializers.VAPPlaySerializer;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VAPPlaySerializerTest {

    @Test
    public void testSerialize_speex_size0to127() {
        VAPPlayMessage message = new VAPPlayMessage((short) 0);
        int audioLength = 1;
        message.setAudio(new byte[audioLength]);

        Session session = mock(Session.class);
        when(session.getFromCodec()).thenReturn(ICodecResource.Codec.SPEEX_WB_NR8);

        VAPPlaySerializer.serialize(session, message);

        assertEquals(audioLength + 1, message.getAudio().length);
    }

    @Test
    public void testSerialize_speex_size128to16383() {
        VAPPlayMessage message = new VAPPlayMessage((short) 0);
        int audioLength = 128;
        message.setAudio(new byte[audioLength]);

        Session session = mock(Session.class);
        when(session.getFromCodec()).thenReturn(ICodecResource.Codec.SPEEX_WB_NR8);

        VAPPlaySerializer.serialize(session, message);

        assertEquals(audioLength + 2, message.getAudio().length);
    }

    @Test
    public void testSerialize_speex_size16384to2097151() {
        VAPPlayMessage message = new VAPPlayMessage((short) 0);
        int audioLength = 16384;
        message.setAudio(new byte[audioLength]);

        Session session = mock(Session.class);
        when(session.getFromCodec()).thenReturn(ICodecResource.Codec.SPEEX_WB_NR8);

        VAPPlaySerializer.serialize(session, message);

        assertEquals(audioLength + 3, message.getAudio().length);
    }

    @Test
    public void testSerialize_speex_size2097152to268435455() {
        VAPPlayMessage message = new VAPPlayMessage((short) 0);
        int audioLength = 2097152;
        message.setAudio(new byte[audioLength]);

        Session session = mock(Session.class);
        when(session.getFromCodec()).thenReturn(ICodecResource.Codec.SPEEX_WB_NR8);

        VAPPlaySerializer.serialize(session, message);

        assertEquals(audioLength + 4, message.getAudio().length);
    }

    @Test
    public void testSerialize_speex_sizeGreaterSize268435455() {
        VAPPlayMessage message = new VAPPlayMessage((short) 0);
        int audioLength = 268435456;
        message.setAudio(new byte[audioLength]);

        Session session = mock(Session.class);
        when(session.getFromCodec()).thenReturn(ICodecResource.Codec.SPEEX_WB_NR8);

        VAPPlaySerializer.serialize(session, message);

        assertEquals(audioLength, message.getAudio().length);
    }
}

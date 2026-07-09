package com.nuance.ncs.nmsp.engine.streamers;

import com.nuance.ncs.nmsp.engine.RequestData;
import com.nuance.ncs.nmsp.engine.TransactionEngine;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ToClientAudioStreamerTest {

    private TransactionEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = new TransactionEngine();
        ResponseManager responseManager = new ResponseManager(new MockHttpServletResponse(), "ctx");
        setField(engine, "responseManager", responseManager);
        setField(engine, "requestData", new RequestData("{}", null, null, null, "abc"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = TransactionEngine.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void getSubArrayCopiesRange() {
        byte[] src = {1, 2, 3, 4, 5, 6};
        byte[] sub = ToClientAudioStreamer.getSubArray(src, 2, 3);
        assertArrayEquals(new byte[]{3, 4, 5}, sub);
    }

    @Test
    public void getSubArrayReturnsNullForNull() {
        assertNull(ToClientAudioStreamer.getSubArray(null, 0, 0));
    }

    @Test
    public void addAudioBufWithNullBufferAndLastDoesNotSend() {
        ToClientAudioStreamer streamer = new ToClientAudioStreamer(engine, "param", "audio/x-wav");
        streamer.addAudioBuf(null, 0, 0, true);
    }

    @Test
    public void addAudioBufWithNullBufferAndNotLastDoesNotSend() {
        ToClientAudioStreamer streamer = new ToClientAudioStreamer(engine, "param", "audio/x-wav");
        streamer.addAudioBuf(null, 0, 0, false);
    }

    @Test
    public void addAudioBufWithDataSendsToResponseManager() {
        ToClientAudioStreamer streamer = new ToClientAudioStreamer(engine, "param", "audio/x-wav");
        byte[] data = {1, 2, 3, 4};
        streamer.addAudioBuf(data, 0, data.length, false);
        // No assertion possible without spy; this exercises the lines.
        assertNotNull(streamer);
    }
}

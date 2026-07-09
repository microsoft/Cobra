package com.nuance.ncs.nmsp.servlet.response;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;

public class MultipartStreamerTest
{

    @Test
    public void flushAndAllowFlushTime()
            throws IOException
    {
        MultipartStreamer multipartStreamer = new MultipartStreamer(new MockHttpServletResponse(), "--Nuance_NMSP_vutc5w1XobDdefsYG3wq", "abc123");
        multipartStreamer.flushAndAllowFlushTime(true, 10);
        multipartStreamer.flushAndAllowFlushTime(false, -1);
        multipartStreamer.flushAndAllowFlushTime(false, 0);
        multipartStreamer.flushAndAllowFlushTime(false, 1);
        assertTrue(multipartStreamer.outputStream.isReady());
    }
}
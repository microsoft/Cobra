package com.nuance.nmsp.servlet.nmas.tts;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NMDPTTSCmdListenerTest
{
    private Transaction transaction;
    private MockHttpServletResponse mockHttpServletResponse;
    private NMDPTTSCmdListener listener;

    @Before
    public void setUp()
    {
        transaction = new Transaction(new AuthorizationInfo("abc123", "abc123"), "zyx987");
        mockHttpServletResponse = new MockHttpServletResponse();
        listener = new NMDPTTSCmdListener(transaction, mockHttpServletResponse);
    }

    @Test
    public void audioFailed()
    {
        listener.audioFailed();
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, transaction.getErrorID());
        assertFalse(transaction.isRequestCompleted());
    }

    @Test
    public void audioCompletedMarksTransactionDone()
    {
        listener.audioCompleted();
        assertTrue(transaction.isRequestCompleted());
    }

    @Test
    public void onAudioReceivedWithoutSinkIsNoop()
    {
        // No setAudioSink call → onAudioReceived returns silently.
        listener.onAudioReceived(new byte[]{1, 2, 3});
    }

    @Test
    public void onAudioReceivedWithNullDataIsNoop()
    {
        listener.setAudioSink((buf, off, len, last) -> {});
        listener.onAudioReceived(null);
    }

    @Test
    public void onAudioReceivedForwardsToSink()
    {
        List<byte[]> received = new ArrayList<>();
        PdxAudioSink sink = (buf, off, len, last) -> {
            byte[] copy = new byte[len];
            System.arraycopy(buf, off, copy, 0, len);
            received.add(copy);
        };
        listener.setAudioSink(sink);
        listener.onAudioReceived(new byte[]{9, 8, 7});
        assertEquals(1, received.size());
        assertEquals(3, received.get(0).length);
    }

    @Test
    public void onAudioReceivedSwallowsSinkIOException()
    {
        listener.setAudioSink((buf, off, len, last) -> { throw new IOException("nope"); });
        // Should not propagate the IOException.
        listener.onAudioReceived(new byte[]{1});
    }

    @Test
    public void onQueryResultFinalFlushesSink()
    {
        List<Boolean> lastBufFlags = new ArrayList<>();
        listener.setAudioSink((buf, off, len, last) -> lastBufFlags.add(last));
        listener.onQueryResult(new PdxQueryResult(null, true));
        assertEquals(1, lastBufFlags.size());
        assertTrue(lastBufFlags.get(0));
    }

    @Test
    public void onQueryResultNonFinalDoesNotFlush()
    {
        List<Boolean> lastBufFlags = new ArrayList<>();
        listener.setAudioSink((buf, off, len, last) -> lastBufFlags.add(last));
        listener.onQueryResult(new PdxQueryResult(null, false));
        assertTrue(lastBufFlags.isEmpty());
    }

    @Test
    public void onQueryResultFinalWithoutSinkIsNoop()
    {
        // No sink → final-result path returns without throwing.
        listener.onQueryResult(new PdxQueryResult(null, true));
    }

    @Test
    public void onQueryResultFinalSwallowsSinkIOException()
    {
        listener.setAudioSink((buf, off, len, last) -> { throw new IOException("bad"); });
        listener.onQueryResult(new PdxQueryResult(null, true));
    }
}

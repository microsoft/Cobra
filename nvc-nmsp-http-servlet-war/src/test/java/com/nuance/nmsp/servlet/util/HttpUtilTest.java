package com.nuance.nmsp.servlet.util;

import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletResponse;

import static com.nuance.nmsp.servlet.util.HttpUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HttpUtilTest
{
    @Test
    public void codecToMimeType()
    {
        assertNull(HttpUtil.codecToMimeType(null));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.PCM_16_11K));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.PCM_16_8K));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.PCM_16_16K));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.PCM_16_22K));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.WAV));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.EVRC));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.QCELP13));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.SPEEX_8K));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.SPEEX_16K));
        assertNotNull(HttpUtil.codecToMimeType(ServletCodecWrapper.AMR_07));
        assertNull(HttpUtil.codecToMimeType(ServletCodecWrapper.ULAW));
    }

    @Test
    public void codecToFileType()
    {
        assertNull(HttpUtil.codecToFileType(null));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.PCM_16_11K));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.PCM_16_8K));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.PCM_16_16K));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.PCM_16_22K));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.WAV));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.EVRC));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.QCELP13));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.SPEEX_8K));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.SPEEX_16K));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.AMR_07));
        assertNotNull(HttpUtil.codecToFileType(ServletCodecWrapper.ULAW));
    }

    @Test
    public void stringToMimeType()
    {
        assertNull(HttpUtil.stringToMimeType(null));
        assertNull(HttpUtil.stringToMimeType("null"));
        assertNotNull(HttpUtil.stringToMimeType("pcm_16bit_8k"));
        assertNotNull(HttpUtil.stringToMimeType("pcm_16bit_11k"));
        assertNotNull(HttpUtil.stringToMimeType("pcm_16bit_16k"));
        assertNotNull(HttpUtil.stringToMimeType("pcm_16bit_22k"));
        assertNotNull(HttpUtil.stringToMimeType("wav"));
        assertNotNull(HttpUtil.stringToMimeType("speex_nb"));
        assertNotNull(HttpUtil.stringToMimeType("speex_wb"));
        assertNotNull(HttpUtil.stringToMimeType("amr"));
        assertNotNull(HttpUtil.stringToMimeType("qcelp"));
        assertNotNull(HttpUtil.stringToMimeType("evrc"));
        assertNotNull(HttpUtil.stringToMimeType("mp3"));
    }

    @Test
    public void mimeTypeToCodec()
            throws MimeTypeParseException
    {
        assertNull(HttpUtil.mimeTypeToCodec(null));
        assertNotNull(HttpUtil.mimeTypeToCodec(WAV_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(PCM_16BIT_8KHZ_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(PCM_16BIT_11KHZ_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(PCM_16BIT_16KHZ_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(PCM_16BIT_22KHZ_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(SPEEX_NARROWBAND_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(SPEEX_WIDEBAND_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(new MimeTypeWrapper("audio/x-speex;rate=11025")));
        assertNotNull(HttpUtil.mimeTypeToCodec(SPEEX_WIDEBAND_TYPE));

        assertNotNull(HttpUtil.mimeTypeToCodec(AMR_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(QCELP_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(EVRC_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_8KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_16KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_24KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_32KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_40KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_48KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_56KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_64KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_80KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_96KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_112KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_128KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_144KBPS_TYPE));
        assertNotNull(HttpUtil.mimeTypeToCodec(MP3_160KBPS_TYPE));
    }

    // ── Pure helpers ─────────────────────────────────────────────────────────

    @Test
    public void isValidTopicAcceptsKnownAndRejectsOthers()
    {
        assertFalse(HttpUtil.isValidTopic(null));
        assertFalse(HttpUtil.isValidTopic("bogus"));
        assertTrue(HttpUtil.isValidTopic(HttpUtil.TOPIC_DICTATION));
        assertTrue(HttpUtil.isValidTopic(HttpUtil.TOPIC_WEBSEARCH));
        assertTrue(HttpUtil.isValidTopic(HttpUtil.TOPIC_AUTOMOTIVE_DICTATION));
        assertTrue(HttpUtil.isValidTopic(HttpUtil.TOPIC_AUTOMOTIVE_WEBSEARCH));
        assertTrue(HttpUtil.isValidTopic(HttpUtil.TOPIC_DTV_SEARCH));
        assertTrue(HttpUtil.isValidTopic(HttpUtil.TOPIC_DTV));
    }

    @Test
    public void getTopicReadsParamThenHeaderThenDefault()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertEquals(HttpUtil.DEFAULT_TOPIC, HttpUtil.getTopic(req));

        req = new MockHttpServletRequest();
        req.addHeader("Accept-Topic", "WebSearch");
        assertEquals("WebSearch", HttpUtil.getTopic(req));

        req = new MockHttpServletRequest();
        req.addParameter("topic", "DTV");
        assertEquals("DTV", HttpUtil.getTopic(req));
    }

    @Test
    public void getVoiceFavorsParameterOverHeader()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertNull(HttpUtil.getVoice(req));

        req.addHeader("Content-Voice", "Tom");
        assertEquals("Tom", HttpUtil.getVoice(req));

        req = new MockHttpServletRequest();
        req.addParameter("voice", "Samantha");
        req.addHeader("Content-Voice", "Tom");
        assertEquals("Samantha", HttpUtil.getVoice(req));
    }

    @Test
    public void getAcceptLanguageFallsBackToDefault()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertEquals(HttpUtil.DEFAULT_LANGUAGE, HttpUtil.getAcceptLanguage(req));

        req = new MockHttpServletRequest();
        req.addParameter("lang", "frfr");
        assertEquals("frfr", HttpUtil.getAcceptLanguage(req));

        req = new MockHttpServletRequest();
        req.addHeader("Accept-Language", "esus");
        assertEquals("esus", HttpUtil.getAcceptLanguage(req));
    }

    @Test
    public void getContentLanguageReadsHeader()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Content-Language", "ende");
        assertEquals("ende", HttpUtil.getContentLanguage(req));
    }

    @Test
    public void getAcceptReturnsSanitizedAcceptHeader()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertNull(HttpUtil.getAccept(req));
        req.addHeader("Accept", "plaintext");
        // ESAPI.encoder().encodeForHTML round-trips "plaintext" unchanged.
        assertEquals("plaintext", HttpUtil.getAccept(req));
    }

    @Test
    public void getContentDispositionForWavUsesFilenameForm()
    {
        String disp = HttpUtil.getContentDisposition(ServletCodecWrapper.WAV);
        assertTrue(disp, disp.startsWith("filename="));
        assertTrue(disp, disp.contains(".wav"));
    }

    @Test
    public void getContentDispositionForOtherCodecUsesAttachment()
    {
        String disp = HttpUtil.getContentDisposition(ServletCodecWrapper.PCM_16_8K);
        assertTrue(disp, disp.startsWith("attachment;"));
    }

    @Test
    public void getCodecFromPostHeaderReturnsCodec()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.addHeader(HttpUtil.CONTENT_TYPE, "audio/x-wav");
        assertNotNull(HttpUtil.getCodec(req, HttpUtil.CONTENT_TYPE));
    }

    @Test
    public void getCodecFromPostWithInvalidHeaderReturnsNull()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.addHeader(HttpUtil.CONTENT_TYPE, "not_a_valid_mime/type/extra");
        assertNull(HttpUtil.getCodec(req, HttpUtil.CONTENT_TYPE));
    }

    @Test
    public void getCodecWithNoHeaderAndNoParamReturnsDefault()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertEquals(ServletCodecWrapper.DEFAULT, HttpUtil.getCodec(req, HttpUtil.CONTENT_TYPE));
    }

    @Test
    public void getCodecFromCodecQueryParameter()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addParameter("codec", "wav");
        assertNotNull(HttpUtil.getCodec(req, HttpUtil.CONTENT_TYPE));
    }

    @Test
    public void parseAsrContextReadsAllHeaders()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Dictation-AudioSource", "microphone");
        req.addHeader("X-Dictation-CursorStart", "1");
        req.addHeader("X-Dictation-CursorEnd", "2");
        req.addHeader("X-Dictation-Buffer", "hello");
        req.addHeader("X-Dictation-LogInfo", "info");
        req.addHeader("X-Dictation-EscapeNewLine", "true");
        req.addHeader("X-Dictation-NBestListSize", "5");

        assertNotNull(HttpUtil.parseAsrContext(req));
    }

    @Test
    public void parseAsrContextHandlesBadNBestListSize()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Dictation-NBestListSize", "not-an-int");
        // Should not throw — caught and logged.
        assertNotNull(HttpUtil.parseAsrContext(req));
    }

    @Test
    public void parseAsrContextWithoutHeadersReturnsDefault()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertNotNull(HttpUtil.parseAsrContext(req));
    }

    @Test
    public void sendErrorWritesStatusAndIgnoresIOException()
    {
        MockHttpServletResponse res = new MockHttpServletResponse();
        HttpUtil.sendError(res, HttpServletResponse.SC_BAD_REQUEST, "testMessage");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, res.getStatus());
    }

    // ── cleanup helpers ──────────────────────────────────────────────────────

    @Test
    public void cleanupTransactionWithNullTransactionIsNoop()
    {
        HttpUtil.cleanupTransaction(null, new MockHttpServletResponse());
    }

    @Test
    public void cleanupTransactionWithCompletedTransactionRuns()
    {
        Transaction tx = new Transaction(new AuthorizationInfo("a", "ab"), "id");
        tx.setRequestCompleted(true);
        HttpUtil.cleanupTransaction(tx, new MockHttpServletResponse());
    }

    @Test
    public void cleanupTransactionWithErrorSendsErrorStatus()
    {
        Transaction tx = new Transaction(new AuthorizationInfo("a", "ab"), "id");
        tx.setError("boom");
        tx.setErrorID(HttpServletResponse.SC_BAD_REQUEST);
        tx.setRequestCompleted(false);
        MockHttpServletResponse res = new MockHttpServletResponse();
        HttpUtil.cleanupTransaction(tx, res);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, res.getStatus());
    }

    @Test
    public void cleanupTransactionWithoutErrorIdDefaultsTo500()
    {
        Transaction tx = new Transaction(new AuthorizationInfo("a", "ab"), "id");
        tx.setError("boom");
        tx.setRequestCompleted(false);
        MockHttpServletResponse res = new MockHttpServletResponse();
        HttpUtil.cleanupTransaction(tx, res);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, res.getStatus());
    }

    @Test
    public void cleanupTransactionAudioStreamerNullIsNoop()
    {
        HttpUtil.cleanupTransactionAudioStreamer(null, new MockHttpServletResponse(), null);
    }

    // ── audio byte sanitization ──────────────────────────────────────────────

    @Test
    public void sanitizeAudioBytesIsRoundTrippable()
    {
        byte[] in = new byte[]{1, 2, 3, 4, 5};
        byte[] out = HttpUtil.sanitizeAudioBinaryBytes(in);
        assertNotNull(out);
        assertEquals(in.length, out.length);
    }

    @Test
    public void sanitizeAudioBytesNoOpReturnsSameReference()
    {
        byte[] in = new byte[]{1, 2, 3};
        assertEquals(in, HttpUtil.sanitizeAudioBinaryBytesNoOp(in));
    }

    @Test
    public void getParameterAndGetHeaderDelegate()
    {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addParameter("p", "v");
        req.addHeader("h", "x");
        assertEquals("v", HttpUtil.getParameter(req, "p"));
        assertEquals("x", HttpUtil.getHeader(req, "h"));
    }
}

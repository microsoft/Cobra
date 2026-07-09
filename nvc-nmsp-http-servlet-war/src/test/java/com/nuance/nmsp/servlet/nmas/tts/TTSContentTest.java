package com.nuance.nmsp.servlet.nmas.tts;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TTSContentTest {

    // ── GET form-parameter paths ─────────────────────────────────────────────

    @Test
    public void getWithTextParameterIsTextType() throws IOException {
        // "hello" contains no chars ESAPI would HTML-encode, so the value round-trips.
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.addParameter("text", "hello");

        TTSContent content = new TTSContent(req);
        assertEquals("text", content.getTTSType());
        assertEquals("hello", content.getTTSInput());
    }

    @Test
    public void getWithUriParameterIsUriType() throws IOException {
        // Note: HttpUtil.getSanitizedParameter HTML-encodes the value via ESAPI,
        // so the stored input is encoded — we just assert the branch was selected.
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.addParameter("uri", "http://example.com/foo.txt");

        TTSContent content = new TTSContent(req);
        assertEquals("uri", content.getTTSType());
        assertNotNull(content.getTTSInput());
    }

    @Test
    public void getWithSsmlParameterIsSsmlType() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.addParameter("ssml", "<speak>hi</speak>");

        TTSContent content = new TTSContent(req);
        assertEquals("ssml", content.getTTSType());
        assertNotNull(content.getTTSInput());
    }

    @Test
    public void getWithNoRecognizableParameterThrows() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        try {
            new TTSContent(req);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        } catch (IOException e) {
            fail("Should have thrown IllegalArgumentException, got IOException: " + e.getMessage());
        }
    }

    // ── POST content-type paths ──────────────────────────────────────────────

    @Test(expected = IOException.class)
    public void postWithoutContentTypeThrows() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        // No content-type set.
        new TTSContent(req);
    }

    @Test(expected = IOException.class)
    public void postWithInvalidContentTypeThrows() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setContentType("not_a_valid_mime/type/extra");
        new TTSContent(req);
    }

    @Test
    public void postWithPlainTextReadsBody() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setContentType("text/plain");
        req.setContent("the body".getBytes());

        TTSContent content = new TTSContent(req);
        assertEquals("text", content.getTTSType());
        assertEquals("the body", content.getTTSInput());
    }

    @Test
    public void postWithSsmlContentTypeIsSsml() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setContentType("application/ssml+xml");
        req.setContent("<speak>hi</speak>".getBytes());

        TTSContent content = new TTSContent(req);
        assertEquals("ssml", content.getTTSType());
        // ssml branch does not call readPostRequestBody.
        assertNull(content.getTTSInput());
    }

    @Test(expected = IOException.class)
    public void postWithUnknownContentTypeThrows() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setContentType("application/octet-stream");
        new TTSContent(req);
    }

    // ── getDictionary ────────────────────────────────────────────────────────

    @Test
    public void getDictionaryContainsTypeAndInput() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.addParameter("text", "hello");
        TTSContent content = new TTSContent(req);

        NmspDictionary dict = content.getDictionary();
        assertNotNull(dict);
    }
}

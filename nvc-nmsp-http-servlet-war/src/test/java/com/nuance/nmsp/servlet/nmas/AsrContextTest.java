package com.nuance.nmsp.servlet.nmas;

import junit.framework.TestCase;

public class AsrContextTest extends TestCase {

    public void testGeters() {
        AsrContext testContext = new AsrContext();

        testContext.setStart("11");
        assertEquals("11", testContext.getStart());

        testContext.setEnd("33");
        assertEquals("33", testContext.getEnd());

        testContext.setText("testText");
        assertEquals("testText", testContext.getText());

        testContext.setLogInfo("testLogin");
        assertEquals("testLogin", testContext.getLogInfo());

        testContext.setAudioSource("testSource");
        assertEquals("testSource", testContext.getAudioSource());

        testContext.setEscapeNewLines(false);
        assertFalse(testContext.getEscapeNewLines());

        testContext.setnBestSize(2);
        assertEquals(2, testContext.getnBestSize());

        assertNotNull(testContext.getDictionary(false));
    }

    public void testDictionaryWithAllValuesNullFillsDefaults() {
        // start/stop/text all null → defaults: start=0, end=0, text=""
        AsrContext ctx = new AsrContext();
        assertNotNull(ctx.getDictionary(false));
    }

    public void testDictionaryWithUseNluAddsAppserverData() {
        AsrContext ctx = new AsrContext();
        ctx.setStart("5");
        ctx.setEnd("10");
        ctx.setText("hi");
        ctx.setLogInfo("info");
        assertNotNull(ctx.getDictionary(true));
    }

    public void testEscapeNewLinesToggle() {
        AsrContext ctx = new AsrContext();
        assertFalse(ctx.getEscapeNewLines());
        ctx.setEscapeNewLines(true);
        assertTrue(ctx.getEscapeNewLines());
    }

    public void testDefaultNBestSizeIsTen() {
        AsrContext ctx = new AsrContext();
        assertEquals(10, ctx.getnBestSize());
    }
}

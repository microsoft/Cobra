package com.nuance.nvc.httpservlet.codecwrapper;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServletCodecWrapperTest
{
    @Test
    public void tuples()
    {
        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", false);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", false);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", true);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", false);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", true);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", false);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", true);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", true);
        verifyAllCodecsInit();

        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", true);
        verifyAllCodecsInit();
    }

    @Test
    public void classifiers()
    {
        Whitebox.setInternalState(ServletCodecWrapper.class, "ANDROID", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "BLACKBERRY", false);
        Whitebox.setInternalState(ServletCodecWrapper.class, "J2SE", true);
        ServletCodecWrapper.init();

        assertTrue(ServletCodecWrapper.PCM_16_8K.isPCM());
        assertTrue(ServletCodecWrapper.PCM_16_16K.isPCM());
        assertTrue(ServletCodecWrapper.PCM_16_22K.isPCM());
        assertTrue(ServletCodecWrapper.AMR_00.isAMR());
        assertTrue(ServletCodecWrapper.AMR_07.isAMR());
        assertTrue(ServletCodecWrapper.AMR_NBO_R0.isAMR());
        assertTrue(ServletCodecWrapper.AMR_NBO_R7.isAMR());
        assertTrue(ServletCodecWrapper.SPEEX_8K.isSpeex());
        assertTrue(ServletCodecWrapper.SPEEX_16K.isSpeex());
        assertTrue(ServletCodecWrapper.SPEEX_11K.isSpeex());
    }

    private void verifyAllCodecsInit()
    {
        ServletCodecWrapper.init();
        assertNotNull(ServletCodecWrapper.AMR_00);
        assertNotNull(ServletCodecWrapper.AMR_01);
        assertNotNull(ServletCodecWrapper.AMR_02);
        assertNotNull(ServletCodecWrapper.AMR_03);
        assertNotNull(ServletCodecWrapper.AMR_04);
        assertNotNull(ServletCodecWrapper.AMR_05);
        assertNotNull(ServletCodecWrapper.AMR_06);
        assertNotNull(ServletCodecWrapper.AMR_07);
        assertNotNull(ServletCodecWrapper.ULAW);
        assertNotNull(ServletCodecWrapper.MP3);
        assertNotNull(ServletCodecWrapper.MP3_8KBPS);
        assertNotNull(ServletCodecWrapper.MP3_16KBPS);
        assertNotNull(ServletCodecWrapper.MP3_24KBPS);
        assertNotNull(ServletCodecWrapper.MP3_32KBPS);
        assertNotNull(ServletCodecWrapper.MP3_40KBPS);
        assertNotNull(ServletCodecWrapper.MP3_48KBPS);
        assertNotNull(ServletCodecWrapper.MP3_56KBPS);
        assertNotNull(ServletCodecWrapper.MP3_64KBPS);
        assertNotNull(ServletCodecWrapper.MP3_80KBPS);
        assertNotNull(ServletCodecWrapper.MP3_96KBPS);
        assertNotNull(ServletCodecWrapper.MP3_112KBPS);
        assertNotNull(ServletCodecWrapper.MP3_128KBPS);
        assertNotNull(ServletCodecWrapper.MP3_144KBPS);
        assertNotNull(ServletCodecWrapper.MP3_160KBPS);
        assertNotNull(ServletCodecWrapper.MS_GSM_FR);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R0);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R1);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R2);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R3);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R4);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R5);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R6);
        assertNotNull(ServletCodecWrapper.AMR_NBO_R7);
        assertNotNull(ServletCodecWrapper.QCELP13);
        assertNotNull(ServletCodecWrapper.QCELPVar);
        assertNotNull(ServletCodecWrapper.EVRC);
        assertNotNull(ServletCodecWrapper.ILBC_R0);
        assertNotNull(ServletCodecWrapper.ILBC_R1);
        assertNotNull(ServletCodecWrapper.SPEEX_8K);
        assertNotNull(ServletCodecWrapper.SPEEX_16K);
        assertNotNull(ServletCodecWrapper.SPEEX_11K);
        assertNotNull(ServletCodecWrapper.PCM_16_8K);
        assertNotNull(ServletCodecWrapper.PCM_16_11K);
        assertNotNull(ServletCodecWrapper.PCM_16_16K);
        assertNotNull(ServletCodecWrapper.PCM_16_32K);
        assertNotNull(ServletCodecWrapper.PCM_16_22K);
    }
}

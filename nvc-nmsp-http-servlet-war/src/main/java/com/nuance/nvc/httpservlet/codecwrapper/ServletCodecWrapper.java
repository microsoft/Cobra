package com.nuance.nvc.httpservlet.codecwrapper;

import com.nuance.nmsp.servlet.util.EnvUtil;
import com.nuance.plugin.ncs.utils.ICodecResource;

@SuppressWarnings({"java:S1444", "java:S1104", "java:S3008"})
public class ServletCodecWrapper
{
    private static boolean J2SE = Boolean.parseBoolean(EnvUtil.useDefault("SUPPORT_J2SE", "true"));
    private static boolean ANDROID = Boolean.parseBoolean(EnvUtil.useDefault("SUPPORT_ANDROID", "false"));
    private static boolean BLACKBERRY = Boolean.parseBoolean(EnvUtil.useDefault("SUPPORT_BLACKBERRY", "false"));

    public static ServletCodecWrapper AMR_00;
    public static ServletCodecWrapper AMR_01;
    public static ServletCodecWrapper AMR_02;
    public static ServletCodecWrapper AMR_03;
    public static ServletCodecWrapper AMR_04;
    public static ServletCodecWrapper AMR_05;
    public static ServletCodecWrapper AMR_06;
    public static ServletCodecWrapper AMR_07;

    public static ServletCodecWrapper ULAW;
    public static ServletCodecWrapper PCM_16_8K;
    public static ServletCodecWrapper PCM_16_11K;
    public static ServletCodecWrapper PCM_16_16K;
    public static ServletCodecWrapper PCM_16_32K;
    public static ServletCodecWrapper PCM_16_22K;
    public static ServletCodecWrapper WAV;

    public static ServletCodecWrapper DEFAULT;

    public static ServletCodecWrapper MP3;
    public static ServletCodecWrapper MP3_8KBPS;
    public static ServletCodecWrapper MP3_16KBPS;
    public static ServletCodecWrapper MP3_24KBPS;
    public static ServletCodecWrapper MP3_32KBPS;
    public static ServletCodecWrapper MP3_40KBPS;
    public static ServletCodecWrapper MP3_48KBPS;
    public static ServletCodecWrapper MP3_56KBPS;
    public static ServletCodecWrapper MP3_64KBPS;
    public static ServletCodecWrapper MP3_80KBPS;
    public static ServletCodecWrapper MP3_96KBPS;
    public static ServletCodecWrapper MP3_112KBPS;
    public static ServletCodecWrapper MP3_128KBPS;
    public static ServletCodecWrapper MP3_144KBPS;
    public static ServletCodecWrapper MP3_160KBPS;

    public static ServletCodecWrapper MS_GSM_FR;
    public static ServletCodecWrapper AMR_NBO_R0;
    public static ServletCodecWrapper AMR_NBO_R1;
    public static ServletCodecWrapper AMR_NBO_R2;
    public static ServletCodecWrapper AMR_NBO_R3;
    public static ServletCodecWrapper AMR_NBO_R4;
    public static ServletCodecWrapper AMR_NBO_R5;
    public static ServletCodecWrapper AMR_NBO_R6;
    public static ServletCodecWrapper AMR_NBO_R7;

    public static ServletCodecWrapper QCELP13;
    public static ServletCodecWrapper QCELPVar;
    public static ServletCodecWrapper EVRC;
    public static ServletCodecWrapper ILBC_R0;
    public static ServletCodecWrapper ILBC_R1;

    public static ServletCodecWrapper SPEEX_8K;
    public static ServletCodecWrapper SPEEX_16K;
    public static ServletCodecWrapper SPEEX_11K;

    static
    {
        init();
    }

    public static void init()
    {
        PCM_16_8K = new ServletCodecWrapper((short) 0x0002, true);
        AMR_00 = new ServletCodecWrapper((short) 0x0050, !ANDROID);
        AMR_01 = new ServletCodecWrapper((short) 0x0051, !ANDROID);
        AMR_02 = new ServletCodecWrapper((short) 0x0052, !ANDROID);
        AMR_03 = new ServletCodecWrapper((short) 0x0053, !ANDROID);
        AMR_04 = new ServletCodecWrapper((short) 0x0054, !ANDROID);
        AMR_05 = new ServletCodecWrapper((short) 0x0055, !ANDROID);
        AMR_06 = new ServletCodecWrapper((short) 0x0056, !ANDROID);
        AMR_07 = new ServletCodecWrapper((short) 0x0057, !ANDROID);
        ULAW = new ServletCodecWrapper((short) 0x0008, !ANDROID);
        PCM_16_11K = new ServletCodecWrapper((short) 0x0004, !BLACKBERRY);
        DEFAULT = new ServletCodecWrapper((short) 0x9999, J2SE);

        MP3 = new ServletCodecWrapper((short) 0x0260, J2SE || ANDROID || BLACKBERRY);
        MP3_8KBPS = new ServletCodecWrapper((short) 0x0261, J2SE || ANDROID || BLACKBERRY);
        MP3_16KBPS = new ServletCodecWrapper((short) 0x0262, J2SE || ANDROID || BLACKBERRY);
        MP3_24KBPS = new ServletCodecWrapper((short) 0x0263, J2SE || ANDROID || BLACKBERRY);
        MP3_32KBPS = new ServletCodecWrapper((short) 0x0264, J2SE || ANDROID || BLACKBERRY);
        MP3_40KBPS = new ServletCodecWrapper((short) 0x0265, J2SE || ANDROID || BLACKBERRY);
        MP3_48KBPS = new ServletCodecWrapper((short) 0x0266, J2SE || ANDROID || BLACKBERRY);
        MP3_56KBPS = new ServletCodecWrapper((short) 0x0267, J2SE || ANDROID || BLACKBERRY);
        MP3_64KBPS = new ServletCodecWrapper((short) 0x0268, J2SE || ANDROID || BLACKBERRY);
        MP3_80KBPS = new ServletCodecWrapper((short) 0x0269, J2SE || ANDROID || BLACKBERRY);
        MP3_96KBPS = new ServletCodecWrapper((short) 0x026A, J2SE || ANDROID || BLACKBERRY);
        MP3_112KBPS = new ServletCodecWrapper((short) 0x026B, J2SE || ANDROID || BLACKBERRY);
        MP3_128KBPS = new ServletCodecWrapper((short) 0x026C, J2SE || ANDROID || BLACKBERRY);
        MP3_144KBPS = new ServletCodecWrapper((short) 0x026D, J2SE || ANDROID || BLACKBERRY);
        MP3_160KBPS = new ServletCodecWrapper((short) 0x026E, J2SE || ANDROID || BLACKBERRY);
        MS_GSM_FR = new ServletCodecWrapper((short) 0x0290, J2SE || ANDROID);
        AMR_NBO_R0 = new ServletCodecWrapper((short) 0x0080, J2SE);
        AMR_NBO_R1 = new ServletCodecWrapper((short) 0x0081, J2SE);
        AMR_NBO_R2 = new ServletCodecWrapper((short) 0x0082, J2SE);
        AMR_NBO_R3 = new ServletCodecWrapper((short) 0x0083, J2SE);
        AMR_NBO_R4 = new ServletCodecWrapper((short) 0x0084, J2SE);
        AMR_NBO_R5 = new ServletCodecWrapper((short) 0x0085, J2SE);
        AMR_NBO_R6 = new ServletCodecWrapper((short) 0x0086, J2SE);
        AMR_NBO_R7 = new ServletCodecWrapper((short) 0x0087, J2SE);
        QCELP13 = new ServletCodecWrapper((short) 0x0062, J2SE);
        QCELPVar = new ServletCodecWrapper((short) 0x0063, J2SE);
        EVRC = new ServletCodecWrapper((short) 0x0070, J2SE);
        ILBC_R0 = new ServletCodecWrapper((short) 0x0090, J2SE);
        ILBC_R1 = new ServletCodecWrapper((short) 0x0091, J2SE);
        SPEEX_8K = new ServletCodecWrapper((short) 0x0106, J2SE || ANDROID);
        SPEEX_16K = new ServletCodecWrapper((short) 0x0116, J2SE || ANDROID);
        SPEEX_11K = new ServletCodecWrapper((short) 0x0168, J2SE || ANDROID);
        PCM_16_16K = new ServletCodecWrapper((short) 0x0005, J2SE || ANDROID);
        PCM_16_32K = new ServletCodecWrapper((short) 0x0006, J2SE || ANDROID);
        PCM_16_22K = new ServletCodecWrapper((short) 0x0007, J2SE || ANDROID);
        WAV = new ServletCodecWrapper((short) 0x9999, J2SE);
    }

    private final short value;

    public ServletCodecWrapper(short value, boolean supported)
    {
        this.value = supported ? value : 0;
    }

    public short getValue()
    {
        return value;
    }

    public boolean isPCM()
    {
        return value >= 0x0002 && value <= 0x0007;
    }

    public boolean isAMR()
    {
        return (value >= 0x0050 && value <= 0x0057)
                || (value >= 0x0080 && value <= 0x0087);
    }

    public boolean isSpeex()
    {
        return value == 0x0106 || value == 0x0116 || value == 0x0168;
    }

    public ICodecResource.Codec toICodecResourceCodec()
    {
        return ICodecResource.Codec.fromNMAINCodecId(value & 0xFFFF);
    }
}

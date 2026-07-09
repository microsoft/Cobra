package com.nuance.ncs.nmsp.servlet.utils;

import com.nuance.plugin.ncs.utils.ICodecResource;

public class AudioCodecType {

	AudioCodecType()
	{
		throw new IllegalStateException("Utility class!");
	}

	public static boolean isPcm(ICodecResource.Codec codec)
	{
		return codec.getType() == ICodecResource.Type.L16;
	}

	public static boolean isMpeg(ICodecResource.Codec codec)
	{
		// MP3 codecs use nmaidCodes 608-622 (MP3 through MP3_160KBPS)
		int code = codec.getNmaidCode();
		return code >= 608 && code <= 622;
	}

	public static boolean isSpeex(ICodecResource.Codec codec)
	{
		return codec.getType() == ICodecResource.Type.SPEEX;
	}

}

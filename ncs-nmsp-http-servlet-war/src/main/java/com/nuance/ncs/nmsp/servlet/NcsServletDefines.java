package com.nuance.ncs.nmsp.servlet;

import com.nuance.plugin.ncs.utils.ICodecResource;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("java:S1171")
public final class NcsServletDefines {

	@SuppressWarnings("java:S1104")
	public static class Pair<A, B> {
		// Technically a pair is called first & second, but these are renamed for clarity
	    public A codec;
	    public B mimeType;
	    public Pair(A a, B b) {
	    	codec = a;
	    	mimeType = b;
	    }
	}

	// Custom headers
	public static final String NUANCE_CONTEXT_HEADER = "Nuance-Context";
	public static final String NUANCE_APP_ID_HEADER = "nuance-application-id";
	public static final String NUANCE_APP_KEY_HEADER = "nuance-application-key";
	public static final String NUANCE_UID_HEADER = "nuance-user-id";


	public static final String HTTP_CONTENT_DISPOSITION = "content-disposition";
	public static final String HTTP_CONTENT_TYPE = "content-type";

	public static final String HTTP_REQUEST_ORIGIN_HEADER = "Origin";
	public static final String HTTP_RESPONSE_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

	// For case insensitivity, use lowercase everywhere
	// HTTP part names
	private static final String NCS_REQUEST_DATA = "requestdata";
	private static final String NCS_CALL_LOG_DATA = "calllogdata";
	private static final String NCS_AUDIO_PARAMETER = "audioparameter";
	private static final String NCS_CONCLUDING_AUDIO_PARAMETER = "concludingaudioparameter";
	private static final String NCS_CONCLUDING_PARAMETER = "concludingparameter";
	private static final String NCS_CHOICE_PARAMETER = "choiceparameter";
	private static final String NCS_DATA_PARAMETER = "dataparameter";
	private static final String NCS_TEXT_PARAMETER = "textparameter";
	private static final String NCS_DICT_PARAMETER = "dictparameter";
	private static final String NCS_SEQ_START_PARAMETER = "seqstartparameter";
	private static final String NCS_SEQ_CHUNK_PARAMETER = "seqchunkparameter";
	private static final String NCS_SEQ_END_PARAMETER = "seqendparameter";
	private static final String NCS_TTS_PARAMETER = "ttsparameter";

	public enum BodyPart {
		PART_INVALID,
		PART_NCS_REQUEST_DATA,
		PART_NCS_CALL_LOG_DATA,
		PART_NCS_AUDIO_PARAMETER,
		PART_NCS_CONCLUDING_AUDIO_PARAMETER,
		PART_NCS_CONCLUDING_PARAMETER,
		PART_NCS_CHOICE_PARAMETER,
		PART_NCS_DATA_PARAMETER,
		PART_NCS_TEXT_PARAMETER,
		PART_NCS_DICT_PARAMETER,
		PART_NCS_SEQ_START_PARAMETER,
		PART_NCS_SEQ_CHUNK_PARAMETER,
		PART_NCS_SEQ_END_PARAMETER,
		PART_NCS_TTS_PARAMETER,
	}

	// Defines Reply Body Parts' names
	public static final String NCS_QUERY_ERROR = "QueryError";
	public static final String NCS_QUERY_RESULT = "QueryResult";
	public static final String NCS_QUERY_RETRY = "QueryRetry";
	public static final String NCS_REMOTE_SESSION_ID = "RemoteSessionId";
	public static final String NCS_ERROR = "Error";

	// CallLog reserved remoteEvent
	public static final String NCS_REMOTE_EVENT_KEY = "NcsRemoteEvent";

	private static final String AUDIO_MPEG = "audio/mpeg";

	public static Map<String, Pair<ICodecResource.Codec, String>> getCodecMap()
	{
		return codecMap;
	}

	// maps codec name as string to ICodecResource.Codec and the mime type
	protected static final Map<String, Pair<ICodecResource.Codec, String>> codecMap = new HashMap<>(){
		{
			put("PCM_16_8K",  new Pair<>(ICodecResource.Codec.PCM16_8K,  "audio/x-wav;codec=pcm;bit=16;rate=8000"));
			put("PCM_16_11K", new Pair<>(ICodecResource.Codec.PCM16_11K, "audio/x-wav;codec=pcm;bit=16;rate=11025"));
			put("PCM_16_16K", new Pair<>(ICodecResource.Codec.PCM16_16K, "audio/x-wav;codec=pcm;bit=16;rate=16000"));
			put("PCM_16_22K", new Pair<>(ICodecResource.Codec.PCM16_22K, "audio/x-wav;codec=pcm;bit=16;rate=22050"));
			put("PCM_16_32K", new Pair<>(ICodecResource.Codec.PCM16_32K, "audio/x-wav;codec=pcm;bit=16;rate=32000"));

			put("MP3", new Pair<>(ICodecResource.Codec.MP3, AUDIO_MPEG));

			put("MS_GSM_FR", new Pair<>(ICodecResource.Codec.MS_GSM, "audio/ms-gsm"));

			put("SPEEX_8K",  new Pair<>(ICodecResource.Codec.SPEEX_NB_NR6, "audio/x-speex;rate=8000"));
			put("SPEEX_16K", new Pair<>(ICodecResource.Codec.SPEEX_WB_NR8, "audio/x-speex;rate=16000"));

			put("OPUS_16K", new Pair<>(ICodecResource.Codec.OPUS_16K_MONO_CBR_CPLX0, "audio/x-opus;rate=16000"));
			put("OPUS_8K",  new Pair<>(ICodecResource.Codec.OPUS_8K_MONO_CBR_CPLX0,  "audio/x-opus;rate=8000"));

			put("MP3_8KBPS",   new Pair<>(ICodecResource.Codec.MP3_8KBPS,   AUDIO_MPEG));
			put("MP3_16KBPS",  new Pair<>(ICodecResource.Codec.MP3_16KBPS,  AUDIO_MPEG));
			put("MP3_24KBPS",  new Pair<>(ICodecResource.Codec.MP3_24KBPS,  AUDIO_MPEG));
			put("MP3_32KBPS",  new Pair<>(ICodecResource.Codec.MP3_32KBPS,  AUDIO_MPEG));
			put("MP3_40KBPS",  new Pair<>(ICodecResource.Codec.MP3_40KBPS,  AUDIO_MPEG));
			put("MP3_48KBPS",  new Pair<>(ICodecResource.Codec.MP3_48KBPS,  AUDIO_MPEG));
			put("MP3_56KBPS",  new Pair<>(ICodecResource.Codec.MP3_56KBPS,  AUDIO_MPEG));
			put("MP3_64KBPS",  new Pair<>(ICodecResource.Codec.MP3_64KBPS,  AUDIO_MPEG));
			put("MP3_80KBPS",  new Pair<>(ICodecResource.Codec.MP3_80KBPS,  AUDIO_MPEG));
			put("MP3_96KBPS",  new Pair<>(ICodecResource.Codec.MP3_96KBPS,  AUDIO_MPEG));
			put("MP3_112KBPS", new Pair<>(ICodecResource.Codec.MP3_112KBPS, AUDIO_MPEG));
			put("MP3_128KBPS", new Pair<>(ICodecResource.Codec.MP3_128KBPS, AUDIO_MPEG));
			put("MP3_144KBPS", new Pair<>(ICodecResource.Codec.MP3_144KBPS, AUDIO_MPEG));
			put("MP3_160KBPS", new Pair<>(ICodecResource.Codec.MP3_160KBPS, AUDIO_MPEG));

			put("IMA_ADPCM_16K", new Pair<>(ICodecResource.Codec.IMA_ADPCM_16K, "audio/x-adpcm;rate=16000"));
		}
		private static final long serialVersionUID = 5698521574521L;
	};

	public static Map<String, BodyPart> getBodyPartMap()
	{
		return bodyPartMap;
	}

	// maps body part name as string to BodyPart enum
	private static final Map<String, BodyPart> bodyPartMap = new HashMap<>(){
		{
			put(NCS_REQUEST_DATA,               BodyPart.PART_NCS_REQUEST_DATA);
			put(NCS_CALL_LOG_DATA,              BodyPart.PART_NCS_CALL_LOG_DATA);
			put(NCS_AUDIO_PARAMETER,            BodyPart.PART_NCS_AUDIO_PARAMETER);
			put(NCS_CONCLUDING_AUDIO_PARAMETER, BodyPart.PART_NCS_CONCLUDING_AUDIO_PARAMETER);
			put(NCS_CONCLUDING_PARAMETER,       BodyPart.PART_NCS_CONCLUDING_PARAMETER);
			put(NCS_CHOICE_PARAMETER,           BodyPart.PART_NCS_CHOICE_PARAMETER);
			put(NCS_DATA_PARAMETER,             BodyPart.PART_NCS_DATA_PARAMETER);
			put(NCS_TEXT_PARAMETER,             BodyPart.PART_NCS_TEXT_PARAMETER);
			put(NCS_DICT_PARAMETER,             BodyPart.PART_NCS_DICT_PARAMETER);
			put(NCS_SEQ_START_PARAMETER,        BodyPart.PART_NCS_SEQ_START_PARAMETER);
			put(NCS_SEQ_CHUNK_PARAMETER,        BodyPart.PART_NCS_SEQ_CHUNK_PARAMETER);
			put(NCS_SEQ_END_PARAMETER,          BodyPart.PART_NCS_SEQ_END_PARAMETER);
			put(NCS_TTS_PARAMETER,              BodyPart.PART_NCS_TTS_PARAMETER);
		}
		private static final long serialVersionUID = 546878542138654L;
	};
}

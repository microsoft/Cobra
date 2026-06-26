package com.nuance.plugin.driver_v2.Endpointers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EndpointerFactory {
	private static final Logger logger = LogManager.getLogger();
	private static final String TAG = "TAG-EndpointerFactory";

	public static IEndpointer create(boolean useAdpcm) {
		try {
			logger.info( System.getProperty("user.dir"));
			logger.info( System.getProperty("java.library.path"));
			
			System.loadLibrary("opus");
			System.loadLibrary("opusvadjava");
			
			IEndpointer  vad = new MockVAD(useAdpcm);

			logger.info( "VAD Leading buffer sample size: " + vad.getMaxBufferSamples());
			logger.info( "VAD Frame samples: " + vad.getFrameSamples());
			logger.info( "VAD Frame bytes: " + vad.getFrameBytes());
//			logger.info( "VAD Opus audio frequency: " + MockVAD.AUDIO_FREQ);
//			logger.info( "VAD Opus sample bytes: " + MockVAD.SAMPLE_BYTES);
			logger.info( "VAD Leading buffer sample count: " + vad.getMaxBufferSamples() / vad.getFrameBytes());
            
			return vad;
		} catch (UnsatisfiedLinkError e) {
			logger.info( "OpusVAD library not installed. Falling back to MockVAD");
			IEndpointer vad = new MockVAD(useAdpcm);
			return vad;			
			
		} catch( Exception e) {
			logger.info( "OpusVAD library not installed. Falling back to MockVAD");
			IEndpointer vad = new MockVAD(useAdpcm);
			return vad;			
		}

	}
}

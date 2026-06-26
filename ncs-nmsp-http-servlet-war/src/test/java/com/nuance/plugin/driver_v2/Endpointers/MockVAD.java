package com.nuance.plugin.driver_v2.Endpointers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

public class MockVAD implements IEndpointer {
	private static final Logger logger = LogManager.getLogger();
	private final static String TAG = "TAG-Endpointer";
	
	// ********************* CONSTANTS ********************* 
    /** The Constant MAX_8_BITS_SIGNED. Used in the algorithm to detect audio energy level. */
    final static float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
    
    /** The Constant MAX_8_BITS_UNSIGNED. Used in the algorithm to detect audio energy level. */
    final static float MAX_8_BITS_UNSIGNED = 0xff;
    
    /** The Constant MAX_16_BITS_SIGNED. Used in the algorithm to detect audio energy level. */
    final static float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
    
    /** The Constant MAX_16_BITS_UNSIGNED. Used in the algorithm to detect audio energy level. */
    final static float MAX_16_BITS_UNSIGNED = 0xffff;
    
	/** Audio packet object used by the circular queues for buffering */
	class AudioPacket {
		byte audio[];
		int size = 0;
		
		float energyLevel = 0.0F;
		float meanEnergyLevel = 0.0F;
		double SNR = 0.0;
		double rmsDiff = 0.0;
		int frameTime = 0;
		
		AudioFormat format = null;
		
		public AudioPacket(AudioFormat format, byte buf[], int len) {
			this.format = format;
			audio = buf;
			size = len;
		}
	}
	
	static class AudioAnalyzer {
		static public AudioPacket analyze(AudioPacket sample) {
			
			int max = 0;
			float energyLevel = 0.0F;
			
			AudioFormat format = sample.format;
			int readPoint = 0;
			byte[] buffer = sample.audio;
			int size = sample.size;
			boolean use16Bit = (format.getSampleSizeInBits() == 16);
			boolean signed = (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED);
			boolean bigEndian = (format.isBigEndian());
			float sum = 0.0F;
			int sampleSize = 0;
			double[] array = new double[2048];
			
			if (use16Bit) {
				for (int i = readPoint; i < size; i += 2) {
					int value = 0;
					// deal with endianness
					int hiByte = (bigEndian ? buffer[i] : buffer[i+1]);
					int loByte = (bigEndian ? buffer[i+1] : buffer [i]);
					if (signed) {
						short shortVal = (short) hiByte;
						shortVal = (short) ((shortVal << 8) | (byte) loByte);
						value = shortVal;
					} else {
						value = (hiByte << 8) | loByte;
					}
					max = Math.max(max, value);
					
					sum += value;
					array[sampleSize] = value;
					sampleSize += 1;
					
				} // for
			} else {
				// 8 bit - no endianness issues, just sign
				for (int i = readPoint; i < size; i++) {
					int value = 0;
					if (signed) {
						value = buffer [i];
					} else {
						short shortVal = 0;
						shortVal = (short) (shortVal | buffer [i]);
						value = shortVal;
					}
					max = Math.max (max, value);
					
					sum += value;
					array[sampleSize] = value;
					sampleSize += 1;
				} // for
			} // 8 bit
			// express max as float of 0.0 to 1.0 of max value
			// of 8 or 16 bits (signed or unsigned)
			if (signed) {
				if (use16Bit) { energyLevel = (float) max / MAX_16_BITS_SIGNED; }
				else { energyLevel = (float) max / MAX_8_BITS_SIGNED; }
			} else {
				if (use16Bit) { energyLevel = (float) max / MAX_16_BITS_UNSIGNED; }
				else { energyLevel = (float) max / MAX_8_BITS_UNSIGNED; }
			}
			
			sample.energyLevel = energyLevel;
			sample.meanEnergyLevel = (sum / sampleSize) / ((signed) ? MAX_16_BITS_SIGNED : MAX_16_BITS_UNSIGNED);
			sample.SNR = 20 * Math.log10(max);
			
			return sample;
		}
	}
	
	class SpeechDetectorSettings {
		// Experiment with these parameters to find optimal performance for start of speech detection
		//	The start of speech algorithm used in this sample app is very simple and not meant for production
		//	To effectively disable start of speech detection, set audioEnergyThreshold to 0.0
		int numPacketsToBufferStartOfSpeech = 7;		// Number of audio packets to buffer for start of speech detection    					
		int numPacketsToBufferLeadingSilence = 12;		// Number of audio packets to buffer containing leading silence 	
		float audioEnergyThreshold = 0.030F;
		int endOfSpeechThreshold = 45;
		
		double rmsDiffThreshold = 1.2;
		double rmsAbsoluteThreshold = 600;
		
	}
	
	class SpeechDetectorContext {
		SpeechDetectorSettings settings = new SpeechDetectorSettings();
		
		boolean audioPacketHasSpeech = false;
		boolean startOfSpeechDetected = false;
		boolean endOfSpeechDetected = false;
		int startOfSpeechThresholdCounter = 0;
		int endOfSpeechThresholdCounter = 0;
		
		AudioPacket lastAudioPacket = null;
	}
	
	/*
	 * This speech detector does some analysis on the pcm audio to calculate energy level, SNR, etc.
	 * 
	 * It is very simple in that all it is evaluating is if a window of audio frames exceed a configured
	 * threshold value for energy level. This is not very robust, especially in noisy environments, and
	 * should not be used for a production app.
	 * 
	 * Please work with your assigned account manager or CSM to get a copy of Nuance's opusvad endpointer
	 * which provides production quality voice activity detection and end-pointing.
	 * 
	 */
	protected SpeechDetectorContext speechDetector(AudioPacket audioPacket, SpeechDetectorContext c) {
		
		// Get energy level of audio sample
		audioPacket = AudioAnalyzer.analyze(audioPacket);
		audioPacket.frameTime = (c.lastAudioPacket == null) ? 0 : c.lastAudioPacket.frameTime + 20; 
		
		if( c.lastAudioPacket != null ) {
			audioPacket.rmsDiff = 20.0 * Math.log10(audioPacket.energyLevel / c.lastAudioPacket.energyLevel);
		}
		c.lastAudioPacket = audioPacket;
		
		c.audioPacketHasSpeech = (audioPacket.energyLevel < c.settings.audioEnergyThreshold) ? false : true;
				
		// Speech already detected. Just analyze audio and return...
		if( c.startOfSpeechDetected ) {
			if( c.audioPacketHasSpeech ) c.endOfSpeechThresholdCounter = 0;
			else c.endOfSpeechThresholdCounter++;
			
			c.endOfSpeechDetected = (c.endOfSpeechThresholdCounter == c.settings.endOfSpeechThreshold) ? true : false;
			
			return c;
		}
		
		// Audio packet containing speech has been detected...
		if( c.audioPacketHasSpeech )  {
			// Start of speech has been detected - start sending NCS query commands and buffered audio to NCS cloud
			if( ++c.startOfSpeechThresholdCounter == c.settings.numPacketsToBufferStartOfSpeech ) {
				c.startOfSpeechDetected = true;
				
			}	// Audio packet contains speech, but we haven't reached our threshold yet. Keep buffering...
			else {
				c.startOfSpeechDetected = false;
			}
			
		} // This audio packet is leading silence - buffer it
		else {
			c.startOfSpeechDetected = false;
			
			// Reset start of speech counters and new buffer size
			c.startOfSpeechThresholdCounter = 0;
		}
	
		return c;
	}
	
	SpeechDetectorContext mContext = new SpeechDetectorContext();
	VADResult mVADResult = new VADResult();
	boolean bos_detected = false;
	boolean eos_detected = false;
	boolean speech_confirmed = false;
	
	public MockVAD(boolean useAdpcm) {
	}

    /**
     * Gets the desired audio format.
     * 
     * Sample rate = 16Khz
     * Sample site = 16bits
     * Num Channels = 1
     * Signed = true
     * Big Endian = false
     *
     * @return the format
     */
    static protected AudioFormat getFormat() {
    	float sampleRate = 16000;
    	int sampleSizeInBits = 16;
    	int channels = 1;
    	boolean signed = true;
    	boolean bigEndian = false;
    	return new AudioFormat(sampleRate, 
    			sampleSizeInBits, channels, signed, bigEndian);
    }

	@Override
	public int processAudio(ByteBuffer audiobuf, int num_samples) {
		AudioPacket p = new AudioPacket(MockVAD.getFormat(), audiobuf.array(), num_samples);
		mContext = speechDetector(p, mContext);
		
		if( mContext.endOfSpeechDetected && !eos_detected ) {
			eos_detected = true;
			mVADResult.setResult(2);
			return 0;
		}
		if( mContext.startOfSpeechDetected && !speech_confirmed ) {
			speech_confirmed = true;
			mVADResult.setResult(3);
			return 0;
		}
		if( mContext.audioPacketHasSpeech && !bos_detected ) {
			bos_detected = true;
			mVADResult.setResult(1);
			return 0;
		}
		
		mVADResult.setResult(0);
		return 0;
	}


	@Override
	public int processAudioAdpcm(ByteBuffer audiobuf, int num_samples, int high_nibble_first) {
		logger.info( "ADPCM Audio is not supported!");
		return -1;
	}

	@Override
	public void setHighNibbleFirst() {
        // Nothing to do as this is for adpcm audio
      }

	@Override
	public void setLowNibbleFirst() {
        // Nothing to do as this is for adpcm audio
      }

	@Override
	public IVADResult getVADResult() {
		return mVADResult;
	}


	@Override
	public int getMaxBufferSamples() {
		return 5120;
	}


	@Override
	public int processAudioByteArray(byte[] audiobuf, int offset, int len) {
        return processAudio(ByteBuffer.wrap(audiobuf, offset, len), len );
	}


	@Override
	public int getFrameSamples() {
		return 320;
	}


	@Override
	public int getFrameBytes() {
		return 640;
	}


	@Override
	public int getOpusEncodedBytes(byte[] out, int pos, int maxlen) {
		logger.info( "OPUS Audio is not supported!");
		return -1;
	}
    public boolean supportsOpus() {
    	return false;
    }
}

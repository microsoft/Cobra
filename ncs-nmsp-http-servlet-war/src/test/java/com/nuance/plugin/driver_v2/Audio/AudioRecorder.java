package com.nuance.plugin.driver_v2.Audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.nuance.plugin.driver_v2.Endpointers.IEndpointer;
import com.nuance.plugin.driver_v2.Endpointers.IVADResult;
import com.nuance.plugin.driver_v2.Endpointers.VADResult;
import com.nuance.plugin.driver_v2.util.FileLoader;
import com.nuance.plugin.driver_v2.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioRecorder extends Thread {

    private static final Logger logger = LogManager.getLogger();
    private static final String TAG = "TAG-AudioRecorder";

    public interface Listener {
        void onRecordingStarted();
        void onRecordingStopped(boolean timedOut);
        void onRecordingError(String s);
        void onRecord(byte[] data, int size,int vad_event);
    }

    public class Options {
        public Codecs codec = Codecs.OPUS;
        public boolean useAdpcm = false;
        public boolean useHighNibbleFirst = true;
        public Source source = Source.microphone;
        public String sourceFilePath = null;
        public String savedAudioPath = null;
        public long mRecordingTimeout = 0;
    	
    }
    
    public static class VADEvent {
        public static final int NO_EVENT = 0;
        public static final int SPEECH_DETECTED = 1;
        public static final int END_OF_SPEECH_DETECTED = 2;
        public static final int SPEECH_CONFIRMED = 3;
    }

    public enum Codecs {
        PCM,
        OPUS,
        IMA_ADPCM
    }

    public enum Source {
    	microphone,
    	file
    }

    static protected AudioFormat getFormat() {
    	float sampleRate = 16000;
    	int sampleSizeInBits = 16;
    	int channels = 1;
    	boolean signed = true;
    	boolean bigEndian = false;
    	return new AudioFormat(sampleRate, 
    			sampleSizeInBits, channels, signed, bigEndian);
    }
    
    private AudioRecorder.Options mRecorderOptions = new AudioRecorder.Options();
    private AudioRecorder.Listener mRecorderListener = null;
    private Timer mRecordingTimer = null;
    private boolean mRecordingTimedOut = false;

    private final AudioFormat mAudioFormat = AudioRecorder.getFormat();

    /** The built-in speaker data line that audio will be read from. */
	private TargetDataLine mRecorder = null;
    private DataLine.Info info = new DataLine.Info( TargetDataLine.class, mAudioFormat );

    
    private static final int mAudioBufferSize = 640;	// 640 bytes is 20ms frames of PCM 16kHz 16bit mono

    private boolean mRecording = false;

    private double mEnergyLevel = 0.0;

    IEndpointer mEndpointer = null;
    IVADResult mVADResult = null;

    public boolean isRecording() {
        return mRecording;
    }

    public AudioRecorder(Codecs codec, IEndpointer endpointer, AudioRecorder.Listener listener) throws Exception {

        if( listener == null ) {
            logger.info( "ERROR: Null listener provided to AudioRecorder!");
            throw new Exception("ERROR: Null listener provided to AudioRecorder!");
        }

        this.mRecorderListener = listener;
        
        logger.info( "AudioRecorder audio buffer size: " + mAudioBufferSize);

        mEndpointer = endpointer;
        mVADResult = new VADResult();
    }

    public int getNumSamplesToBufferForLeadingSilence() {
        return mEndpointer.getMaxBufferSamples() / mEndpointer.getFrameBytes();
    }

    private double getSNR(double RMS) {
        return ( 20 * Math.log10(RMS) );
    }

    private double calculateSimpleRMS(byte[] buffer, int size) {
        double rms = 0.0;

        for(int i=0; i < size; i++) {
            rms += buffer[i] * buffer[i];
        }

        rms = Math.sqrt((rms/buffer.length));

        return rms;
    }
    private void calculateEnergyLevel(byte[] audio, int size) {
        double rms = calculateSimpleRMS(audio, size);
        mEnergyLevel = rms - getSNR(rms);
    }

    public double getEnergyLevel() {
        return mEnergyLevel;
    }

    @Override
    public void run() {
        mRecording = true;
        
        logger.info( "Source: " + mRecorderOptions.source);

        if( mRecorderOptions.source == Source.microphone )
        	recordFromMicrophone();
        else
        	recordFromFile();
    }

    private void recordFromMicrophone() {
        byte[] buffer = new byte[mAudioBufferSize];
        byte[] opusEncodedBuffer = new byte[mAudioBufferSize];

		// Open and start capturing audio over the built-in speaker data line
        
		try {
			mRecorder = (TargetDataLine) AudioSystem.getLine(info);
			mRecorder.open(mAudioFormat);
		} catch (LineUnavailableException e) {
			logger.info( "AudioRecorder uanble to be initialized");
			e.printStackTrace();
            mRecording = false;
            mRecorderListener.onRecordingError("AudioRecord failed to properly initialize");
            return;
		}
        
        mRecorder.addLineListener(new LineListener() {
			@Override
			public void update(LineEvent event) {
				logger.info( "Line Event: " + event.toString());				
			}});

        if (!mRecorder.isOpen()) {
            logger.info( "AudioRecorder is not properly initialized");
            mRecording = false;
            mRecorderListener.onRecordingError("AudioRecord is not properly initialized");
            return;
        } else {
            logger.info( "AudioRecorder is initialized");
        }

        mRecorder.start();
        logger.info( "AudioRecorder has started recording...");
        startRecordingTimer();
        mRecorderListener.onRecordingStarted();

		/** Save audio to file. If a folder was provided at the command line, use it. Otherwise, save to present working directory */
		File dir = ( mRecorderOptions.savedAudioPath == null ) ? new File(".") : new File(mRecorderOptions.savedAudioPath);

		// Filename created is audio-{timestamp}.pcm
		long unixTime = System.currentTimeMillis() / 1000L;
		File fout;
		OutputStream FileOutput;
		try {
			fout = new File(dir.getCanonicalPath() + File.separator + "audio-" + unixTime + ".pcm");
			dir = new File(fout.getParent());
			logger.info("Writing to " + fout.getCanonicalPath());
			
			// Create path and file if they do not exist
			if( !fout.exists() ) {
				dir.mkdirs();
				fout.createNewFile();
			}
			// Create the file output stream...
			FileOutput = new FileOutputStream(fout);

			boolean speechConfirmed = false;
			
	        while(mRecording) {
	            //reading data from MIC into buffer
	            int nRead = mRecorder.read(buffer, 0, buffer.length);
	            logger.info( "Bytes Read: " + nRead);
	
	            if( nRead > 0 ) {
	            	FileOutput.write(buffer, 0, nRead);
	                calculateEnergyLevel(buffer, nRead);
	                int res = mEndpointer.processAudioByteArray(buffer, 0, nRead);
	                mVADResult = mEndpointer.getVADResult();
	                if( !speechConfirmed && mVADResult != null && mVADResult.getResult() == VADEvent.SPEECH_CONFIRMED ) speechConfirmed = true;
	                if( mRecorderOptions.codec == Codecs.OPUS ) {
	                    int len = mEndpointer.getOpusEncodedBytes(opusEncodedBuffer, 0, opusEncodedBuffer.length);
	                    logger.info( "Encoded length: " + len);
	                    mRecorderListener.onRecord(opusEncodedBuffer, len, mVADResult.getResult());
	                } else
	                    mRecorderListener.onRecord(buffer, nRead, mVADResult.getResult());
	            }
	        }
	        FileOutput.close();
	        mRecorder.close();
	        
	        if( !speechConfirmed ) fout.delete();	// Don't bother keeping the audio file...
			} catch (IOException e) {
				logger.info( e.getMessage());
				e.printStackTrace();
		}

    	
    }
    
    private void recordFromFile() {
    	
    	File file = new File(mRecorderOptions.sourceFilePath);
    	
    	logger.info( "Codec: " + mRecorderOptions.codec);
    	
    	// AudioChopper requires converting the codec value provided to an alternate format...
		String _codec = (mRecorderOptions.codec == Codecs.IMA_ADPCM) ? com.nuance.plugin.driver_v2.Audio.Codec.IMA_ADPCM_16K :
			"PCM_" + mAudioFormat.getSampleSizeInBits() + '_' + (int)(mAudioFormat.getSampleRate() / 1000) + 'K';

		@SuppressWarnings("unused")
		int audioDuration = 0;
		byte[] audio;
		try {
			audio = FileLoader.load(file);
		} catch (Exception e) {
			logger.info( e.getMessage());
			e.printStackTrace();
			return;
		}
		AudioChopper chopper = AudioChopperFactory.getAudioChopper(_codec, audio);
		
        startRecordingTimer();
        mRecorderListener.onRecordingStarted();

		if( chopper != null && (mRecorderOptions.codec.equals(Codecs.PCM) || 
				mRecorderOptions.codec.equals(Codecs.IMA_ADPCM) || mRecorderOptions.codec == Codecs.OPUS)) {

	        byte[] opusEncodedBuffer = new byte[mAudioBufferSize];
			
			while(chopper.hasMoreFrame() && mRecording) {
				byte[] buffer = chopper.getNextFrame();
				int nRead = buffer.length;
				logger.info( "Bytes Read: " + nRead);
				
	            if( nRead > 0 ) {
	                calculateEnergyLevel(buffer, nRead);
	                int res = mEndpointer.processAudioByteArray(buffer, 0, nRead);
	                mVADResult = mEndpointer.getVADResult();	                
	                if( mRecorderOptions.codec == Codecs.OPUS ) {
	                    int len = mEndpointer.getOpusEncodedBytes(opusEncodedBuffer, 0, opusEncodedBuffer.length);
	                    logger.info( "Encoded length: " + len);
	                    mRecorderListener.onRecord(opusEncodedBuffer, len, mVADResult.getResult());
	                } else
	                    mRecorderListener.onRecord(buffer, nRead, mVADResult.getResult());
	                
					// Simulate real-time streaming of audio (20ms frames)
					int wait = chopper.getFrameLengthInMs();
					synchronized(this) {
						try {
							this.wait(wait);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					audioDuration += wait;
	            }
			}
			
			if( mRecording && !chopper.hasMoreFrame() ) {
				mRecorderListener.onRecordingStopped(mRecordingTimedOut);
			}
		}
		else {
			logger.info( "chopper is null or invalid codec");
		}

		mRecording = false;
		cancelRecordingTimer();
    }
    
    private void cancelRecordingTimer() {
    	try {
	    	if( mRecordingTimer != null ) {
				mRecordingTimer.cancel();
				mRecordingTimer = null;
	    	}
    	} catch (Exception e) {
    		logger.info( e.getMessage());
    	}
    }

    private void startRecordingTimer() {
        if( mRecordingTimer != null && mRecorderOptions.mRecordingTimeout > 0 )
            mRecordingTimer.start(mRecorderOptions.mRecordingTimeout, new Timer.Listener() {

                @Override
                public void onStarted(long start) {
                    logger.info( "Recording timer started!");
                }

                @Override
                public void onCancelled(long duration) {
                    logger.info( "Recording timer cancelled!");
                }

                @Override
                public void onExpired(long duration) {
                    logger.info( "Recording timer expired! " + (duration / 1000F));
                    mRecordingTimedOut = true;
                    stopListening();
                }
            });
    }

    public void startListening() {    	
        startListening(null);
    }

    public void startListening(AudioRecorder.Options options) {
    	mRecorderOptions = (options == null) ? this.new Options() : options;

    	if( mRecorderOptions.useHighNibbleFirst )
    		mEndpointer.setHighNibbleFirst();
    	else
    		mEndpointer.setLowNibbleFirst();

        mRecordingTimedOut = false;

        if( mRecorderOptions.mRecordingTimeout > 0)
            mRecordingTimer = new Timer();
        else
            mRecordingTimer = null;

        start();
    }

    public void resetRecordingTimer(long timeoutInMs) {
        mRecorderOptions.mRecordingTimeout = (timeoutInMs == 0) ? mRecorderOptions.mRecordingTimeout : timeoutInMs;
        if( mRecordingTimer != null ) {

            mRecordingTimedOut = false;
            mRecordingTimer.cancel();
            if( timeoutInMs == 0 )
                mRecordingTimer.reset();
            else
                mRecordingTimer.reset(mRecorderOptions.mRecordingTimeout);
        }
    }

    public void resetRecordingTimer() {
        resetRecordingTimer(0);
    }

    public void stopListening() {
        logger.info( "AudioRecorder is being stopped");

        mRecording = false;

        if( mRecorderOptions.source == Source.microphone && (mRecorder == null || !mRecorder.isActive()) ) {
            logger.info( "AudioRecorder cannot be stopped.");
            return;
        }

        if( mRecorderOptions.source == Source.microphone ) 
        	mRecorder.stop();
        
        mRecorderListener.onRecordingStopped(mRecordingTimedOut);

        if( mRecordingTimer != null )
            mRecordingTimer.cancel();

        mRecordingTimer = null;
    }
}

package com.nuance.plugin.driver_v2.CloudServices;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.nuance.plugin.driver_v2.Configuration;
import com.nuance.plugin.driver_v2.HttpInterfaceClient;
import com.nuance.plugin.driver_v2.Audio.AudioRecorder;
import com.nuance.plugin.driver_v2.Audio.AudioRecorder.Codecs;
import com.nuance.plugin.driver_v2.Audio.AudioRecorder.Source;
import com.nuance.plugin.driver_v2.Audio.AudioRecorder.VADEvent;
import com.nuance.plugin.driver_v2.Endpointers.EndpointerFactory;
import com.nuance.plugin.driver_v2.Endpointers.IEndpointer;
import com.nuance.plugin.driver_v2.util.LatencyMonitor;
import com.nuance.plugin.driver_v2.util.UserIDManager;
import com.nuance.plugin.driver_v2.util.LatencyMonitor.Marker;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


/**
 * Created by peter_freshman on 9/19/16.
 */
public class Recognizer {

    private static final String TAG = "TAG-Recognizer";
    private static final Logger logger = LogManager.getLogger();

    public interface Listener {
        void onStateChange(Recognizer.State state);
        void onEvent(Recognizer.Events event, String s);
    }

    static class Defaults {
    	public static String appName = "Nuance Java Sample HTTP App";
    	public static String appVersion = "1.0";
    	public static String carrier = "unknown";
    	public static String network = "wifi";
    	public static String locale = "USA";
    	public static String organizationId = "Nuance";
    	public static String audioSource = "SpeakerAndMicrophone";
    	public static String language = "eng-USA";
    	public static final String DeviceId = UserIDManager.createUserIDManager().initUserID();
    	public static int RecorderTimeout = 6000;
    }

    public class Options {

        protected String language = Defaults.language;
        protected String recognitionType = null;
        protected String contextTag = null;
        protected String nluText = null;
        protected String wakeUpPhrase = null;
        protected boolean progressiveResults = true;
        protected boolean textWithTimingResults = false;
        protected Codecs codec = Codecs.OPUS;
        protected String deviceModel = System.getProperty("os.arch");
        
        protected boolean encodeToOpus = true;
        protected boolean useAdpcm = false;
        protected boolean endpointingEnabled = true;
        protected boolean useHighNibbleFirst = true;
        
        protected Source source = Source.microphone;
        protected String savedAudioPath = ".";
        protected String sourceFilePath = null;
        
        protected int trxnTimeout = Defaults.RecorderTimeout;

        public boolean isProgressiveResults() {
            return progressiveResults;
        }

        public void setProgressiveResults(boolean progressiveResults) {
            this.progressiveResults = progressiveResults;
        }

        public boolean isTextWithTimingResults() {
            return textWithTimingResults;
        }

        public void setTextWithTimingResults(boolean textWithTimingResults) {
            this.textWithTimingResults = textWithTimingResults;
        }

        public boolean isEncodeToOpus() {
            return encodeToOpus;
        }

        public void setEncodeToOpus(boolean encodeToOpus) {
            this.encodeToOpus = encodeToOpus;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            //if( language == null || language.length() < 7 )
            //    logger.info( "Invalid language code provided: " + ((language == null) ? "null" : language));
            //else
                this.language = language;
        }

        public String getRecognitionType() {
            return recognitionType;
        }

        public void setRecognitionType(String recognitionType) {
            this.recognitionType = recognitionType;
        }

        public String getContextTag() {
            return contextTag;
        }

        public void setContextTag(String contextTag) {
            this.contextTag = contextTag;
        }

        public String getNluText() {
            return nluText;
        }

        public void setNluText(String nluText) {
            this.nluText = nluText;
        }

        public String getWakeUpPhrase() {
            return wakeUpPhrase;
        }

        public void setWakeUpPhrase(String wakeUpPhrase) {
            this.wakeUpPhrase = wakeUpPhrase;
        }

        public boolean isUseAdpcm() {
			return useAdpcm;
		}

		public void setUseAdpcm(boolean useAdpcm) {
			this.useAdpcm = useAdpcm;
		}

        public boolean isUseHighNibbleFirst() {
			return useHighNibbleFirst;
		}

		public void setUseHighNibbleFirst(boolean useHighNibbleFirst) {
			this.useHighNibbleFirst = useHighNibbleFirst;
		}

		public Source getSource() {
			return source;
		}

		public void setSource(Source source) {
			this.source = source;
		}

		public Codecs getCodec() {
			return codec;
		}

		public void setCodec(Codecs codec) {
			this.codec = codec;
		}

		public String getSavedAudioPath() {
			return savedAudioPath;
		}

		public void setSavedAudioPath(String savedAudioPath) {
			this.savedAudioPath = savedAudioPath;
		}

		public String getSourceFilePath() {
			return sourceFilePath;
		}

		public void setSourceFilePath(String sourceFilePath) {
			this.sourceFilePath = sourceFilePath;
		}

		public boolean isEndpointingEnabled() {
			return endpointingEnabled;
		}

		public void setEndpointingEnabled(boolean endpointingEnabled) {
			this.endpointingEnabled = endpointingEnabled;
		}

		public String getDeviceModel() {
			return deviceModel;
		}

		public void setDeviceModel(String deviceModel) {
			this.deviceModel = deviceModel;
		}

		public int getTrxnTimeout() {
			return trxnTimeout;
		}

		public void setTrxnTimeout(int trxnTimeout) {
			this.trxnTimeout = trxnTimeout;
		}

    }

    private class AudioPacket {
        private byte[] data;
        private int len = 0;

        public AudioPacket(byte[] data, int len) {
            this.data = new byte[len];
            System.arraycopy(data, 0, this.data, 0, len);
            this.len = len;
        }
        public byte[] getData() {
            return data;
        }
        @SuppressWarnings("unused")
		public int getLen() {
            return len;
        }
    }

    public enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    public enum Events {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        MESSAGE_RECEIVED,
        CLOSED,
        ERROR
    }

    private class RecognizerTask extends Thread {    	
        
        public void run() {

            logger.info( "Starting recognition");
            mLatencyMonitor.setMarker(Marker.start);

            // Start listening immediately if not an NLU Text request...
            if (mRecognizerOptions.getNluText() == null) startListening();

            // Connect to server if not already connected...
            if (mHttpInterfaceClient == null || !mHttpInterfaceClient.isConnected())
                connectToServer();
            else    // Send NLU Text commands if connected and not an ASR based request...
            {
            	mLatencyMonitor.setMarker(Marker.connecting);
            	mLatencyMonitor.setMarker(Marker.connected);
                if( mRecognizerOptions.getNluText() != null ) sendCommands();
            }

        }
    }

    private Recognizer.Options mRecognizerOptions = null;
    private Recognizer.Listener mRecognizerListener = null;

    private HttpInterfaceClient mHttpInterfaceClient = null;

    private AudioRecorder mRecorder = null;
    private AudioRecorder.Options mRecorderOptions = null;

    private boolean mAudioBufferSent = false;
    private boolean mCommandsSent = false;
    private double mEnergyLevel = 0.0;

    private boolean mInitialSpeechDetected = false;
    private boolean mStartOfSpeechConfirmed = false;
    private boolean mEndOfSpeechDetected = false;

    private CircularFifoQueue<AudioPacket> mLeadingSilenceCQ;
    private CircularFifoQueue<AudioPacket> mLeadingSpeechCQ;

    private boolean mIgnoreDisconnectMessage = false;
    private CountDownLatch mCountDownLatch = null;

	private LatencyMonitor mLatencyMonitor = new LatencyMonitor();

    /** GETTERS / SETTERS **/
    public String getLanguage() {
        return mRecognizerOptions.language;
    }

    public String getTopic() {
        return mRecognizerOptions.recognitionType;
    }

    public String getContextTag() {
        return mRecognizerOptions.contextTag;
    }

    // Constructor
    public Recognizer() {
        logger.info( "Device Id: " + Defaults.DeviceId);
    }


    /** PUBLIC METHODS **/

    public void recognize(Recognizer.Options options, Recognizer.Listener listener) throws Exception {

    	mRecognizerOptions = options;
    	
        if( listener == null ) {
            logger.info( "ERROR: Null listener provided to Recognizer!");
            throw new Exception("ERROR: Null listener provided to Recognizer!");
        }

        if( options.getLanguage() == null || options.getLanguage().trim().isEmpty() )  {
            logger.info( "ERROR: No language provided to Recognizer!");
            throw new Exception("ERROR: No language provided to Recognizer!");
        }

        if( (options.getContextTag() == null || options.getContextTag().trim().isEmpty()) &&
                (options.getRecognitionType() == null || options.getRecognitionType().trim().isEmpty()))  {
            logger.info( "ERROR: No dictation type or context tag provided to Recognizer!");
            throw new Exception("ERROR: No dictation type or context tag provided to Recognizer!");
        }

        // Initialize some flags
        mAudioBufferSent = false;
        mCommandsSent = false;
        mEnergyLevel = 0.0;

        // Initialize vad events
        mInitialSpeechDetected = false;
        mStartOfSpeechConfirmed = false;
        mEndOfSpeechDetected = false;

        // Initialize recognizer options
        mRecognizerListener = listener;
        
        if( mHttpInterfaceClient == null )
        	mHttpInterfaceClient = new HttpInterfaceClient(mHttpInterfaceClientListener);
        
        mRecognizerOptions.setCodec((options.isEncodeToOpus()) ? Codecs.OPUS :  ((options.isUseAdpcm()) ? Codecs.IMA_ADPCM : Codecs.PCM));
        logger.info( "Endpointing Enabled: " + mRecognizerOptions.isEndpointingEnabled());

        Thread t = new Thread() {
        	@Override
        	public void run() {
                // Do recognition
                mCountDownLatch = new CountDownLatch(1);
                
                mLatencyMonitor.reset();
                RecognizerTask task = new RecognizerTask();
                task.start();
                
                try {
                    mCountDownLatch.await();                    
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                	logger.info( e.getMessage());
                }

                mCountDownLatch = null;
                logger.info( "Recognition complete");
                
                mLatencyMonitor.stop();
                showLatencyMarkers();
        	}
        };
        t.start();
    }

    public void stopListening() {
        if( mRecorder != null && mRecorder.isRecording() )
            mRecorder.stopListening();
    }

    public void cancelRecognition() {
        stopRecognizer();
    }

    public double getEnergyLevel() {
        return mEnergyLevel;
    }

    /** PRIVATE METHODS **/

    private String floatToFormattedString(float f) {
    	return String.format("%2.3f", f);    	
    }
    
    /*
     *    Marker.start
     *    
     *    Marker.connecting
     *    Marker.connected
     *    
     *    Marker.audio_recording_begin
     *    Marker.bos_speech_detected
     *    Marker.audio_streaming_begin
     *    
     *    Marker.transaction_begin
     *    
     *    Marker.eos_speech_detected
     *    Marker.audio_recording_end
     *    Marker.audio_streaming_end
     *    
     *    Marker.initial_response
     *    Marker.final_response
     *    
     *    Marker.transaction_end
     *    
     *    Marker.stop
     */
    private void showLatencyMarkers() {
    	String s = "\n";
    	
    	if (mRecognizerOptions.getNluText() == null) {
	    	s += "marker\t\t\t\t\tDuration Since T0\tDuration Since Last Marker" + "\n";
	    	s += "[t0] " + Marker.start + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.start)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.start)) + "sec" + "\n";
	    	
	    	s += "[t1] " + Marker.connecting + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.connecting)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.connecting)) + "sec" + "\n";
	    	s += "[t2] " + Marker.connected + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.connected)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.connecting, Marker.connected)) + "sec" + "\n";
	    	
	    	s += "[t3] " + Marker.audio_recording_begin + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_recording_begin)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_recording_begin)) + "sec (from t0)" + "\n";
	    	s += "[t4] " + Marker.bos_speech_detected + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.bos_speech_detected)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_recording_begin, Marker.bos_speech_detected)) + "sec" + "\n";
	    	
	    	if( mRecognizerOptions.isEndpointingEnabled() )
	    		s += "[t5] " + Marker.audio_streaming_begin + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_streaming_begin)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.bos_speech_detected, Marker.audio_streaming_begin)) + "sec" + "\n";
	    	else
	    		s += "[t5] " + Marker.audio_streaming_begin + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_streaming_begin)) + 
    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_recording_begin, Marker.audio_streaming_begin)) + "sec (from t3)" + "\n";

	    	if( mRecognizerOptions.isEndpointingEnabled() )
	    		s += "[t6] " + Marker.transaction_begin + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.transaction_begin)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.bos_speech_detected, Marker.transaction_begin)) + "sec (from t4)" + "\n";
	    	else
	    		s += "[t6] " + Marker.transaction_begin + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.transaction_begin)) + 
    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_recording_begin, Marker.transaction_begin)) + "sec (from t3)" + "\n";
	    	
	    	s += "[t7] " + Marker.eos_speech_detected + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.eos_speech_detected)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.bos_speech_detected, Marker.eos_speech_detected)) + "sec (from t4)" + "\n";
	    	s += "[t8] " + Marker.audio_recording_end + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_recording_end)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_recording_begin, Marker.audio_recording_end)) + "sec (from t3)" + "\n";
	    	s += "[t9] " + Marker.audio_streaming_end + "\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.audio_streaming_end)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_recording_end, Marker.audio_streaming_end)) + "sec" + "\n";
	    	
	    	
	    	if( mRecognizerOptions.progressiveResults )
	    		s += "[t10] " + Marker.initial_response + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.initial_response)) + 
	    		"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_streaming_begin, Marker.initial_response)) + "sec (from t5)" + "\n";
	    	else
	    		s += "[t10] " + Marker.initial_response + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.initial_response)) + 
	    		"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.initial_response)) + "sec (from t9)" + "\n";
	    	
	    	s += "[t11] " + Marker.final_response + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.final_response)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.audio_streaming_end, Marker.final_response)) + "sec (from t9)" + "\n";
	    	
	    	s += "[t12] " + Marker.transaction_end + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.transaction_end)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.transaction_begin, Marker.transaction_end)) + "sec (from t6)" + "\n";
	    	
	    	s += "[t13] " + Marker.stop + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.stop)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.transaction_end, Marker.stop)) + "sec" + "\n";
    	} else {
	    	s += "marker\t\t\t\t\tDuration Since T0\tDuration Since Last Marker" + "\n";
	    	s += "[t0] " + Marker.start + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.start)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.start)) + "sec" + "\n";
	    	
	    	s += "[t1] " + Marker.connecting + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.connecting)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.connecting)) + "sec" + "\n";
	    	s += "[t2] " + Marker.connected + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.connected)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.connecting, Marker.connected)) + "sec" + "\n";
	    		    	
	    	s += "[t3] " + Marker.transaction_begin + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.transaction_begin)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.connected, Marker.transaction_begin)) + "sec" + "\n";
	    		    	
	    	s += "[t4] " + Marker.initial_response + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.initial_response)) + 
	    		"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.transaction_begin, Marker.initial_response)) + "sec" + "\n";
	    	
	    	s += "[t5] " + Marker.final_response + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.final_response)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.initial_response, Marker.final_response)) + "sec" + "\n";
	    	
	    	s += "[t6] " + Marker.transaction_end + "\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.transaction_end)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.transaction_begin, Marker.transaction_end)) + "sec (from t3)" + "\n";
	    	
	    	s += "[t7] " + Marker.stop + "\t\t\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.start, Marker.stop)) + 
	    			"sec\t\t" + floatToFormattedString(mLatencyMonitor.calculateDistanceBetweenMarkers(Marker.transaction_end, Marker.stop)) + "sec" + "\n";
    		
    	}
    	logger.info( s);
    }
    
    private void connectToServer() {
        try {
            logger.info( "Creating HttpInterfaceClient...");
            if( mHttpInterfaceClient == null )
            	mHttpInterfaceClient = new HttpInterfaceClient(mHttpInterfaceClientListener);

            logger.info( "HttpInterfaceClient connecting...");
            if( !mHttpInterfaceClient.isConnected() )
            	mHttpInterfaceClient.connect();

        } catch (Exception e) {
        	logger.info( e.getMessage());
            e.printStackTrace();

            if( mCountDownLatch != null ) mCountDownLatch.countDown();
        }

    }

    private void startListening() {
        try {
        	IEndpointer endpointer = EndpointerFactory.create(false);
        	if( !endpointer.supportsOpus() && mRecognizerOptions.getCodec() == Codecs.OPUS )
        		mRecognizerOptions.setCodec(Codecs.PCM);
        	
        	mRecorder = new AudioRecorder(mRecognizerOptions.getCodec(), endpointer, mRecorderListener);
        	
        	mLeadingSilenceCQ = new CircularFifoQueue<>(mRecorder.getNumSamplesToBufferForLeadingSilence());
            mLeadingSpeechCQ = new CircularFifoQueue<>(50 * (mRecognizerOptions.getTrxnTimeout()/1000));   //50 samples per second * recorder timeout in seconds

        	mRecorderOptions = mRecorder.new Options();
            mRecorderOptions.codec = mRecognizerOptions.getCodec();
            mRecorderOptions.useAdpcm = mRecognizerOptions.useAdpcm;
            mRecorderOptions.useHighNibbleFirst = mRecognizerOptions.useHighNibbleFirst;
            mRecorderOptions.savedAudioPath = mRecognizerOptions.getSavedAudioPath();
            mRecorderOptions.sourceFilePath = mRecognizerOptions.getSourceFilePath();
            mRecorderOptions.source = mRecognizerOptions.getSource();

            // Add time to recording timeout if network connectivity is very slow (ie longer than actual recording timeout)...
            int additionalTimeForConnecting = ( mHttpInterfaceClient == null || !mHttpInterfaceClient.isConnected() ) ? 500 : 0;
            mRecorderOptions.mRecordingTimeout = mRecognizerOptions.getTrxnTimeout() + additionalTimeForConnecting;

            logger.info( "Calling recorder start listening...");
            mRecorder.startListening(mRecorderOptions);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendCommands() {

    	mCommandsSent = true;
    	
        if( mCountDownLatch != null ) {
        	mLatencyMonitor.setMarker(Marker.transaction_begin);
        	
            sendHeaders();
            sendQueryBegin();
            sendRequestInfoParameter();
            if(mRecognizerOptions.nluText != null) sendAudioTermination();
        }

    }

    private void sendHeaders() {
        try {
        	
        	ArrayList<String> headers = new ArrayList<String>();
        	headers.add("POST /"+ Configuration.PROXY_VERSION +"/ HTTP/1.1");
        	headers.add("Host: " + Configuration.SERVER_HOST);
        	headers.add("Connection: Keep-Alive");
        	headers.add("Keep-Alive: timeout=100");
        	headers.add("User-Agent: " + new java.io.File(Recognizer.class.getProtectionDomain()
													        			  .getCodeSource()
													        			  .getLocation()
													        			  .getPath())
        																  .getName());
        	headers.add("Transfer-Encoding: chunked");
        	headers.add("Content-Type: multipart/form-data; boundary=" + mHttpInterfaceClient.getBoundary());
        	
        	logger.info( headers.toString());
            mHttpInterfaceClient.sendHeaders(headers);

        } catch (Exception e) {
            mRecognizerListener.onEvent(Events.ERROR, e.getMessage());
        }
    }

    private void sendQueryBegin() {

        try {
            String unique_id = UUID.randomUUID().toString();
            String subscriber_id = UserIDManager.createUserIDManager().initUserID();
            String command = (mRecognizerOptions.contextTag == null) ? "NMDP_ASR_CMD" : ( (mRecognizerOptions.nluText == null) ? "NDSP_ASR_APP_CMD" : "NDSP_APP_CMD");
            String paramName = (mRecognizerOptions.contextTag == null) ? "dictation_type" : "context_tag";
            String paramValue = (mRecognizerOptions.contextTag == null) ? mRecognizerOptions.recognitionType : mRecognizerOptions.contextTag;
            String codec = ( mRecognizerOptions.useAdpcm ) ? "IMA_ADPCM_16K" : ( ( mRecognizerOptions.getCodec() == Codecs.OPUS ) ? "OPUS_16K" : "PCM_16_16K" );
            
			JSONObject json = new JSONObject();
			JSONObject cmdDict = new JSONObject();

			json.put("appId", Configuration.APP_ID);
			json.put("appKey", Configuration.APP_KEY);
			json.put("uId", UserIDManager.createUserIDManager().initUserID());
			json.put("inCodec", codec);
			json.put("outCodec", codec);
			json.put("cmdName", command);
			json.put("appName", Defaults.appName);
			json.put("appVersion", Defaults.appVersion);
			json.put("language", mRecognizerOptions.language);
			json.put("cmdTimeout", "5000");		// Optional. Reduce the value of cmdTimeout (in ms) if waiting for a long-running transaction is a concern...
			json.put("carrier", Defaults.carrier);
			json.put("deviceModel", mRecognizerOptions.deviceModel);

			cmdDict.put(paramName, paramValue);
			cmdDict.put("dictation_language", mRecognizerOptions.language);
			cmdDict.put("locale", Defaults.locale);
			cmdDict.put("application_name", Defaults.appName);
			cmdDict.put("organization_id", Defaults.organizationId);
			cmdDict.put("phone_OS", System.getProperty("os.name"));
			cmdDict.put("phone_network", Defaults.network);
			cmdDict.put("audio_source", Defaults.audioSource);
			cmdDict.put("location", "");
			cmdDict.put("application_session_id", unique_id);
			cmdDict.put("subscriber_id", subscriber_id);
			cmdDict.put("utterance_number", 1);
			cmdDict.put("ui_language", mRecognizerOptions.language);
			cmdDict.put("phone_submodel", System.getProperty("os.version"));
			cmdDict.put("application_state_id", "0");

			json.put("cmdDict", cmdDict);
			
			ArrayList<String> headers = new ArrayList<String>();
			headers.add("Content-Disposition: form-data; name=\"RequestData\"");
			headers.add("Content-Type: application/json; charset=utf-8");
			headers.add("Content-Transfer-Encoding: 8bit");
			
			logger.info( headers.toString());
            logger.info( json.toString(4));
			mHttpInterfaceClient.sendMessage(headers, json);

        } catch (Exception e) {
            mRecognizerListener.onEvent(Events.ERROR, e.getMessage());
        }
    }

    private void sendRequestInfoParameter() {

        try {
        	
			JSONObject json = new JSONObject();

            if( mRecognizerOptions.nluText == null ) {
    			json.put("nbest_text_results", 1);
    			json.put("start",0);
    			json.put("end",0);
    			json.put("text","");
                if (mRecognizerOptions.progressiveResults) json.put("intermediate_response_mode", "NoUtteranceDetectionWithPartialRecognition");
                if (mRecognizerOptions.wakeUpPhrase != null) json.put("wake_up_phrase", mRecognizerOptions.wakeUpPhrase);
                if (mRecognizerOptions.textWithTimingResults) json.put("text_with_timing_results", 1);
            }
            else {
                JSONObject appDataDict = new JSONObject();
                appDataDict.put("message", mRecognizerOptions.nluText);
                json.put("appserver_data", appDataDict);
            }
			
            // An example of adding grammars to the REQUEST_INFO dictionary
            /*
            JSONArray grammars = new JSONArray();

            JSONObject inlineGrammar = new JSONObject();
            inlineGrammar.put("type", "instant_item_list");
            inlineGrammar.put("id", "my_phrases");

            JSONArray inlineItems = new JSONArray();
            inlineItems.put("Hey Jessy");
            inlineItems.put("Amy Urhlbach");

            inlineGrammar.put("item_list", inlineItems);
            grammars.put(inlineGrammar);
            json.put("grammar_list", grammars);
            */

			ArrayList<String> headers = new ArrayList<String>();
			headers.add("Content-Disposition: form-data; name=\"DictParameter\";paramName=\"REQUEST_INFO\"");
			headers.add("Content-Type: application/json; charset=utf-8");
			headers.add("Content-Transfer-Encoding: 8bit");
			
			logger.info( headers.toString());
            logger.info( json.toString(4));
			mHttpInterfaceClient.sendMessage(headers, json);

        } catch (Exception e) {
            mRecognizerListener.onEvent(Events.ERROR, e.getMessage());
        }
    }

    private void sendAudio(byte[] audio) {

        /** NOTE: The recording timer is used when calculating the size of the audio buffers, so
         *          there **should** not be a buffer overflow with the logic below... **/

        // Buffer audio leading up to initial speech so first part of speech doesn't get dropped
        if( !mInitialSpeechDetected ) {
            mLeadingSilenceCQ.add(new AudioPacket(audio, audio.length));
            return;
        }

        // Buffer all the audio from time of initial speech detected to when speech is confirmed
        if( !mStartOfSpeechConfirmed || (mHttpInterfaceClient == null ) || (!mHttpInterfaceClient.isConnected() && mStartOfSpeechConfirmed)) {
            mLeadingSpeechCQ.add(new AudioPacket(audio, audio.length));
            return;
        }

        String contentType = "audio/x-wav;codec=pcm;bit=16;rate=16000";
        if( mRecorderOptions.codec == Codecs.OPUS )
        	contentType = "audio/opus;codec=opus;bit=16;rate=16000";
        if( mRecorderOptions.codec == Codecs.IMA_ADPCM )
        	contentType = "audio/adpcm;codec=adpcm;bit=4;rate=16000";
        
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("Content-Disposition: form-data; name=\"ConcludingAudioParameter\";paramName=\"AUDIO_INFO\"");
		headers.add("Content-Type: " + contentType);
		headers.add("Content-Transfer-Encoding: binary");

        // Start of speech is confirmed
        if(!mAudioBufferSent && mStartOfSpeechConfirmed) {
        	mLatencyMonitor.setMarker(Marker.audio_streaming_begin);
        	
            Iterator<AudioPacket> lsiIterator = mLeadingSilenceCQ.iterator();
            while( lsiIterator.hasNext() ) {
                AudioPacket p = lsiIterator.next();
                mHttpInterfaceClient.sendMessage(headers, p.getData());
            }

            Iterator<AudioPacket> lspIterator = mLeadingSpeechCQ.iterator();
            while( lspIterator.hasNext() ) {
                AudioPacket p = lspIterator.next();
                mHttpInterfaceClient.sendMessage(headers, p.getData());
            }
            mAudioBufferSent = true;
        }

        if( !mEndOfSpeechDetected )
        	mHttpInterfaceClient.sendMessage(headers, audio);
    }

    private void sendAudioTermination() {
        try {
        	mLatencyMonitor.setMarker(Marker.audio_streaming_end);
        	
            mHttpInterfaceClient.sendMessage();

        } catch (Exception e) {
            mRecognizerListener.onEvent(Events.ERROR, e.getMessage());
        }
    }

    private void stopRecognizer() {
    	
    	//mHttpInterfaceClient = null;
    	
    	try {
	        if( mCountDownLatch != null ) {
	            logger.info( "Stopping Recognizer - releasing mCountDownLatch...");
	            mCountDownLatch.countDown();
	        } else {
	        	logger.info( "Countdown latch is null, cannot release recognizer");
	        } 
    	}catch( Exception e ) {
    		logger.info( "CountDownLatch Exception in stopRecognizer(): " + e.getMessage());    		
        }
    }

    /** LISTENERS **/
    private final HttpInterfaceClient.Listener mHttpInterfaceClientListener = new HttpInterfaceClient.Listener() {

        @Override
        public void onOpen() {
        	mLatencyMonitor.setMarker(Marker.connecting);
            mRecognizerListener.onEvent(Events.CONNECTING, "Connecting to HTTP Interface");
        }

        @Override
        public void onConnected() {
        	logger.info( "Connected");
        	mLatencyMonitor.setMarker(Marker.connected);
            mRecognizerListener.onEvent(Events.CONNECTED, "Connected to HTTP Interface");

            if( mRecognizerOptions.getNluText() != null ) sendCommands();
        }

        @Override
        public void onDisconnected() {

            if( !mIgnoreDisconnectMessage ) {
                mRecognizerListener.onEvent(Events.DISCONNECTED, "Disconnected from HTTP Interface");
                stopRecognizer();
            } else {
                mCountDownLatch.countDown();
            }
        }

        @Override
        public void onMessage(ByteBuffer msg) {
            mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, "audio received");
        }

        @Override
        public void onClose() {
            mRecognizerListener.onEvent(Events.CLOSED, "HTTP Interface Connection closed");
        }

        @Override
        public void onError(String error) {
            logger.info( "HTTP Interface error: " + error);
            if( error.toLowerCase().contains("server timedout")) {
            	mHttpInterfaceClient = null;
            	mRecognizerListener.onEvent(Events.DISCONNECTED, "Disconnected from HTTP Interface");
                stopRecognizer();
            }
            
            stopListening();
            stopRecognizer();
            mRecognizerListener.onEvent(Events.ERROR, error);
            mRecognizerListener.onStateChange(State.IDLE);
        }

        @Override
		public void onQueryResponse(JSONObject msg) {
			try {
            	if( mLatencyMonitor.getMarker(Marker.initial_response) == -1 )
            		mLatencyMonitor.setMarker(Marker.initial_response);

                logger.info( "Query Response [session id = " + msg.getString("NMAS_PRFX_SESSION_ID") + ']');

				msg.put("message", "query_response");
				logger.info( "Received QueryResponse: " + msg.toString(4));
				mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, msg.toString(4));
			} catch( Exception e ) {
				e.printStackTrace();
			}			
		}

		@Override
		public void onQueryRetry(JSONObject msg) {
			try {
            	if( mLatencyMonitor.getMarker(Marker.initial_response) == -1 )
            		mLatencyMonitor.setMarker(Marker.initial_response);

                logger.info( "Query Retry [session id = " + msg.getString("NMAS_PRFX_SESSION_ID") + ']');
                logger.info( "Query Retry: " + msg.getString("Cause") + ": " + msg.getString("Prompt"));
                stopListening();

				logger.info( "Received QueryRetry: " + msg.toString(4));
				msg.put("message", "query_retry");
				mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, msg.toString(4));
			} catch( Exception e ) {
				e.printStackTrace();
			}			
		}

		@Override
		public void onQueryError(JSONObject msg) {
			try {
            	if( mLatencyMonitor.getMarker(Marker.initial_response) == -1 )
            		mLatencyMonitor.setMarker(Marker.initial_response);

                logger.info( "Query Error [session id = " + msg.getString("NMAS_PRFX_SESSION_ID") + ']');
                logger.info( "Query Error: " + msg.getString("Description"));
                stopListening();

				logger.info( "Received QueryError: " + msg.toString(4));
				msg.put("message", "query_error");
				mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, msg.toString(4));
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onQueryEnd() {
            logger.info( "Query ended");
            mLatencyMonitor.setMarker(Marker.final_response);
            mLatencyMonitor.setMarker(Marker.transaction_end);
            
            if( mCountDownLatch != null ) mCountDownLatch.countDown();
            mRecognizerListener.onStateChange(State.IDLE);
            stopRecognizer();
		}
    };

    private final AudioRecorder.Listener mRecorderListener = new AudioRecorder.Listener() {

        @Override
        public void onRecordingStarted() {
            mLatencyMonitor.setMarker(Marker.audio_recording_begin);

            logger.info( "Recording started");
            mRecognizerListener.onStateChange(State.LISTENING);
        }

        @Override
        public void onRecordingStopped(boolean timedOut) {
        	mLatencyMonitor.setMarker(Marker.audio_recording_end);
        	
            logger.info( "Recording stopped. Timed out = " + timedOut);

            if (timedOut && !mInitialSpeechDetected && !mStartOfSpeechConfirmed) {
                logger.info( "Recording Timed Out Waiting for Initial Speech");
                mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, "Recording Timed Out Waiting for Initial Speech");
            }

            if( timedOut && mInitialSpeechDetected && !mStartOfSpeechConfirmed ) {
                logger.info( "Recording Timed Out Waiting for Confirmed Start of Speech");
                mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, "Recording Timed Out Waiting for Confirmed Start of Speech");
            }

            if( timedOut && mStartOfSpeechConfirmed ) {
                logger.info( "Recording Timed Out Waiting for End of Speech");
                mRecognizerListener.onEvent(Events.MESSAGE_RECEIVED, "Recording Timed Out Waiting for End of Speech");
            }

            if( !mHttpInterfaceClient.isConnected() ) {
                logger.info( "Http Interface Client Is Not Connected, Just Stopping the Recognizer!");
                mRecognizerListener.onStateChange(State.IDLE);
                stopRecognizer();
            }
            else if( mCommandsSent ) {
            //else if( mAudioBeginMessageSent ) {
                logger.info( "Sending Audio Termination Message!");
                sendAudioTermination();
                mRecognizerListener.onStateChange(State.PROCESSING);
            }
            else {
                logger.info( "No Audio Sent, Just Stopping the Recognizer!");
                mRecognizerListener.onStateChange(State.IDLE);
                stopRecognizer();
            }
        }

        @Override
        public void onRecordingError(String s) {
        	if( mLatencyMonitor.getMarker(Marker.audio_recording_begin) == -1 )
        		mLatencyMonitor.setMarker(Marker.audio_recording_begin);
        	
        	if( mLatencyMonitor.getMarker(Marker.audio_recording_end) == -1 )
        		mLatencyMonitor.setMarker(Marker.audio_recording_end);
            
        	logger.info( "Recording error: " + s);
            mRecognizerListener.onEvent(Events.ERROR, s);
            mRecognizerListener.onStateChange(State.IDLE);

            stopRecognizer();
        }

        @Override
        public void onRecord(final byte[] data, final int size, int vad_event) {
            /// send audio to NCS...
            mEnergyLevel = mRecorder.getEnergyLevel();

            if( !mInitialSpeechDetected && !mRecognizerOptions.isEndpointingEnabled() ) {
            	mInitialSpeechDetected = true;
            	mStartOfSpeechConfirmed = true;
            }
            
            switch( vad_event ) {
                case VADEvent.SPEECH_DETECTED:
                    logger.info( "Initial Speech Detected.");
                    mInitialSpeechDetected = true;
                    break;

                case VADEvent.END_OF_SPEECH_DETECTED:
                	mLatencyMonitor.setMarker(Marker.eos_speech_detected);
                    logger.info( "End of Speech Detected.");
                    mEndOfSpeechDetected = true;
                    stopListening();

                    break;

                case VADEvent.SPEECH_CONFIRMED:
                	mLatencyMonitor.setMarker(Marker.bos_speech_detected);
                    logger.info( "Start of Speech Confirmed.");
                    mStartOfSpeechConfirmed = true;
                    break;

                default:
                    break;

            }

            if ( !mCommandsSent && mStartOfSpeechConfirmed && mHttpInterfaceClient != null && mHttpInterfaceClient.isConnected() ) {
                sendCommands();
                mRecorder.resetRecordingTimer(mRecognizerOptions.getTrxnTimeout());    // Give the user a fresh clock to speak their mind...
            }

            ByteBuffer buffer = ByteBuffer.allocate(size);
            buffer.put(data, 0, size);
            sendAudio(buffer.array());
        }
    };

}

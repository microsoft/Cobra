package com.nuance.plugin.driver_v2.Activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nuance.plugin.driver_v2.RecognitionType;
import com.nuance.plugin.driver_v2.Audio.AudioRecorder.Codecs;
import com.nuance.plugin.driver_v2.Audio.AudioRecorder.Source;
import com.nuance.plugin.driver_v2.CloudServices.Recognizer;
import com.nuance.plugin.driver_v2.CloudServices.Recognizer.Events;
import com.nuance.plugin.driver_v2.CloudServices.Recognizer.State;


public class ASRActivity {
    private static final Logger logger = LogManager.getLogger();
	
	private static final String TAG = "TAG-ASRActivity";

	private JSONObject mOptions = null;
	private Recognizer mRecognizer = null;
    private State mState = State.IDLE;
	
	private CountDownLatch latch;

    private void batchMode() {
		// Loop thru and process each file in the path provided.
		//	NOTE: Assumes all files are valid raw PCM 16K 16bit audio
    	String path = mOptions.optString("audio-source");
		logger.info( path);
		
		File dir = new File(path);
		FilenameFilter fileNameFilter = new FilenameFilter() {
			   
            @Override
            public boolean accept(File dir, String name) {
			logger.info( name);
			if(name.lastIndexOf('.')>0)
		   {
		      // get last index for '.' char
			  int lastIndex = name.lastIndexOf('.');
			  
			  // get extension
			  String str = name.substring(lastIndex);
			  
			  // match path name extension
			  if( mOptions.optBoolean("adpcm-encoded", false) && ( str.equalsIgnoreCase(".adpcm") ) )
				  return true;
			  
			  if( !mOptions.optBoolean("adpcm-encoded", false) && ( str.equalsIgnoreCase(".pcm") || str.equalsIgnoreCase(".raw") ) )
			         return true;
			   
			   }
			   return false;
		    }
         };
		File[] directoryListing = dir.listFiles(fileNameFilter);
		
		// Initialize directory listing with single file if folder not provided 
		if( directoryListing == null ) {
			directoryListing = new File[1];
			directoryListing[0] = new File(path);
		}

		if (directoryListing != null) {
			logger.info( "directory listing length: " + directoryListing.length);
			for (File file : directoryListing) {
				logger.info( "Processing audio file: " + file.getAbsolutePath());
				try {
					mOptions.put("source-file-path", file.getPath());
				} catch (JSONException e) {
					logger.info( e.getMessage());
					e.printStackTrace();
					continue;
				}
				latch = new CountDownLatch(1);
				recognize();
				try {
					latch.await(mOptions.optInt("txn-timeout", 30000) + 5000, TimeUnit.MILLISECONDS);
            		synchronized(this) {
						try {
							this.wait(500);	// A little pause to allow Recognizer to finish writing stats to output...
						} catch (InterruptedException e) {
							logger.info( e.getMessage());
							e.printStackTrace();
							continue;
						}
					}
				} catch (InterruptedException e) {
					logger.info( e.getMessage());
					e.printStackTrace();
					continue;
				}				
			}
		}
    }

    private void consoleMode() {
    	logger.info( "Press enter to start capturing audio. (q <enter> to quit)");
    	
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while(true) {
            String s = null;
            try {
                s = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if( s != null && s.equalsIgnoreCase("q") ) {
            	if( mState == State.LISTENING ) stopRecording();
            	if( mState == State.PROCESSING ) cancel();
            	
            	logger.info( "Shutting down...");
            	return;
            }
            if( s != null && s.length() == 0) {
            	toggleReco(); // Toggle listening mode if the user has pressed <enter>
            }
        }				
    }
    
    public void start(JSONObject options) {
    	
    	mOptions = options;
    	
    	if( mOptions.optString("audio-source", "microphone").equalsIgnoreCase("microphone") )
    		consoleMode();
    	else
    		batchMode();
    	

    }
    
    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */
    protected void toggleReco() {
        switch (mState) {
            case IDLE:
                recognize();
                break;
            case LISTENING:
                stopRecording();
                break;
            case PROCESSING:
                cancel();
                break;
        }
    }

    protected void recognize() {
    	
    	try {
            if( mRecognizer == null ) {
                logger.info( "Need to initialize Recognizer");
                mRecognizer = new Recognizer();
            } else {
                logger.info( "Recognizer already initialized...reusing.");
            }

            Recognizer.Options _options = mRecognizer.new Options();
            _options.setLanguage(mOptions.optString("language", "eng-USA"));
            _options.setRecognitionType(mOptions.optString("language-topic", RecognitionType.PERSONAL_ASSISTANT));
            _options.setProgressiveResults(mOptions.optBoolean("word-by-word-streaming", false));
            _options.setEncodeToOpus(mOptions.optBoolean("opus-encoded", false));
            _options.setUseAdpcm(mOptions.optBoolean("adpcm-encoded", false));
            _options.setUseHighNibbleFirst(mOptions.optBoolean("high-nibble-first", true));
            _options.setSource(mOptions.optString("audio-source", "microphone").equalsIgnoreCase("microphone") ? Source.microphone : Source.file);
            _options.setCodec((_options.isEncodeToOpus()) ? Codecs.OPUS : (_options.isUseAdpcm()) ? Codecs.IMA_ADPCM : Codecs.PCM);
            _options.setSavedAudioPath(mOptions.optString("saved-audio-path"));
            _options.setSourceFilePath(mOptions.optString("source-file-path"));
            _options.setEndpointingEnabled(!mOptions.optBoolean("disable-start-of-speech-detection", false));
            _options.setDeviceModel(mOptions.optString("device-model", System.getProperty("os.arch")));
            if (mOptions.has("txn-timeout") ) _options.setTrxnTimeout(Integer.valueOf(mOptions.optString("txn-timeout")));
            if( mOptions.has("wake-up-phrase") ) _options.setWakeUpPhrase(mOptions.optString("wake-up-phrase", null));
            _options.setTextWithTimingResults(mOptions.optBoolean("text-with-timing-results", false));

			mRecognizer.recognize(_options, mRecognizerListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void stopRecording() {
        if( mRecognizer != null )
            mRecognizer.stopListening();
    }

    protected void cancel() {
        if( mRecognizer != null )
            mRecognizer.cancelRecognition();
        
        mState = State.IDLE;
    }

    protected void processMessage(JSONObject msg) {
        try {
            if (msg.has("message")) {
                String message = msg.getString("message");
                if( message.equalsIgnoreCase("connected") ) {
                    logger.info( "Session Id: " + msg.getString("session_id"));
                }
                if( message.equalsIgnoreCase("query_error") ) {
                	logger.info( "Query Error [" + msg.getInt("Error") + "]: " + msg.getString("Description"));
                }
                if( message.equalsIgnoreCase("query_response") ) {
                    int final_response = msg.getInt("final_response");
                    String cadence_regulatable_result = msg.getString("cadence_regulatable_result");
                    JSONArray transcriptions = msg.optJSONArray("transcriptions");

                    if( final_response == 0 )
                    	logger.info( cadence_regulatable_result + ": " + transcriptions.get(0));
                    else if( msg.getString("result_format").equalsIgnoreCase("empty_final_response")) {
                    	logger.info( "No recognition results due to poor audio.");
                    }
                    else {
                        for(int i = 0; i < transcriptions.length(); i++)
                        	logger.info( cadence_regulatable_result + '[' + i + "]: " + transcriptions.get(i));
                    }
                }
                if( message.equalsIgnoreCase("query_end") ) {
                	logger.info( "Query End: " + msg.getString("transaction_id"));
                }
                if( message.equalsIgnoreCase("disconnect") ) {
                	logger.info( "Disconnect: " + msg.optString("reason", "<no reason provided>"));
                }

            } else
            	logger.info( msg.toString(4));
        } catch (Exception e) {
        	logger.info( e.getMessage());
            logger.info( e.getMessage());
            try {
                logger.info( msg.toString(4));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

    }

	protected Recognizer.Listener mRecognizerListener = new Recognizer.Listener() {

        @Override
        public void onStateChange(Recognizer.State state) {
        	mState = state;
        	
            logger.info( "State change: " + state);

            switch (state) {
                case IDLE:
                    logger.info( "IDLE");
                    if( !mOptions.optString("audio-source", "microphone").equalsIgnoreCase("microphone") )
                    	if( latch != null ) latch.countDown();
                    else
                    	logger.info( "Press enter to start recording (\'q\' to quit):");
                    break;
                case LISTENING:
                    logger.info( "LISTENING");
                    break;
                case PROCESSING:
                    logger.info( "PROCESSING");
                    break;
                default:
            }

        }

        @Override
        public void onEvent(Events event, String s) {
            logger.info( "Event received: " + event);

            switch (event) {
                case CONNECTING:
                    logger.info( "CONNECTING: " + s);
                    break;
                case ERROR:
                	logger.info( "ERROR: " + s);
                	//Log.i("TAG", "Press enter to start recording (\'q\' to quit):");
                    break;
                case MESSAGE_RECEIVED:
                    if( isJSON(s) )
                        try {
                            processMessage(new JSONObject(s));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    else
                    	logger.info( "MSG RECEIVED: " + s);
                    break;
                case DISCONNECTED:
                	logger.info( "DISCONNECTED: " + s);
                    if( mRecognizer != null ) mRecognizer = null;
                    if( mState == State.PROCESSING ) toggleReco();
                    mState = State.IDLE;
                    if( latch != null ) latch.countDown();
                    break;
                case CLOSED:
                	logger.info( "CLOSED: " + s);
                    if( mRecognizer != null ) mRecognizer = null;
                    break;
                case CONNECTED:
                	logger.info( "CONNECTED: " + s);
                    break;
                default:
            }

        }
		
	};
	
    private boolean isJSON(String s) {
        try {
            @SuppressWarnings("unused")
			JSONObject json = new JSONObject(s);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

}

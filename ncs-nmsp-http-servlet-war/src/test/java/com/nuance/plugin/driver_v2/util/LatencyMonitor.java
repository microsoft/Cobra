package com.nuance.plugin.driver_v2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class LatencyMonitor.
 */
public class LatencyMonitor {
	private static final Logger logger = LogManager.getLogger();

	/*
	 * transaction start timestamp
	 * transaction running latency timestamp
	 * audio streaming begin timestamp
	 * audio streaming end timestamp
	 * audio streaming calculated time to complete
	 * transaction initial response timestamp
	 * transaction initial calculated user perceived latency
	 * transaction final response timestamp
	 * transaction response calculated time to complete
	 * transaction final calculated user perceived latency
	 * transaction total duration
	 * 
	 */

	/**
	 * The Class Marker.
	 */
	public class Marker {
		
		/** The Constant start. */
		public static final String start = "start";

		/** The Constant connecting. */
		public static final String connecting = "connecting";

		/** The Constant connected. */
		public static final String connected = "connected";
		
		/** The Constant audio_recording_begin. */
		public static final String audio_recording_begin = "audio_recording_begin";
		
		/** The Constant audio_recording_end. */
		public static final String audio_recording_end = "audio_recording_end";

		/** The Constant audio_playing_begin. */
		public static final String audio_playing_begin = "audio_playing_begin";
		
		/** The Constant audio_playing_end. */
		public static final String audio_playing_end = "audio_playing_end";

		/** The Constant transaction_begin. */
		public static final String transaction_begin = "transaction_begin";
		
		/** The Constant beginning of speech detected. */
		public static final String bos_speech_detected = "bos_speech_detected";
		
		/** The Constant end of speech detected. */
		public static final String eos_speech_detected = "eos_speech_detected";

		/** The Constant audio_streaming_begin. */
		public static final String audio_streaming_begin = "audio_streaming_begin";
		
		/** The Constant audio_streaming_end. */
		public static final String audio_streaming_end = "audio_streaming_end";
		
		/** The Constant transaction_end. */
		public static final String transaction_end = "transaction_end";
		
		/** The Constant initial_response. */
		public static final String initial_response = "initial_response";
		
		/** The Constant final_response. */
		public static final String final_response = "final_response";
		
		/** The Constant stop. */
		public static final String stop = "stop";
	}
	
	/** The markers. */
	private Map<String, Long> mMarkers = new HashMap<String, Long>();
		
	/**
	 * Sets a named marker.
	 *
	 * @param marker the new marker
	 */
	public void setMarker( String marker ) {
		getmMarkers().put( marker, new Date().getTime() );
	}
	
	public void removeMarker( String marker ) {
		if( getmMarkers().containsKey(marker) )
			getmMarkers().remove(marker);		
	}
	
	/**
	 * Gets a named marker.
	 *
	 * @param marker the marker
	 * @return the marker
	 */
	public long getMarker( String marker ) {
		if( !getmMarkers().containsKey(marker) )
			return -1;
		
		return (long)getmMarkers().get( marker );
	}
	
	/**
	 * Calculate distance between markers.
	 *
	 * @param m1 the m1
	 * @param m2 the m2
	 * @return the float
	 */
	public float calculateDistanceBetweenMarkers( String m1, String m2 ) {
		long start = getMarker(m1);
		long end = getMarker(m2);
		
		return ( (end - start) / 1000F );
	}

	/**
	 * Calculate distance since start.
	 *
	 * @return the float
	 */
	public float calculateDistanceSinceStart( ) {
		long start = getMarker(Marker.start);
		long end = new Date().getTime();
		
		return ( (end - start) / 1000F );
	}

	/**
	 * Start.
	 *
	 * @return true, if successful
	 */
	public boolean start() {
		
		if( getmMarkers().containsKey(Marker.start)) {
			logger.info("LatencyMonitor.start() Error: monitor already running. Stop() and/or reset() monitor before calling start().");
			return false;
		}
		
		getmMarkers().put( Marker.start, new Date().getTime() );
		return true;
	}
	
	/**
	 * Stop.
	 *
	 * @return true, if successful
	 */
	public boolean stop() {
		
		if( !getmMarkers().containsKey(Marker.start)) {
			logger.info("LatencyMonitor.stop() Error: monitor not running. Reset() and/or start() monitoring before calling stop().");
			return false;
		}

		getmMarkers().put( Marker.stop, new Date().getTime() );
		return true;
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		getmMarkers().clear();		
	}

	public Map<String, Long> getmMarkers() {
		return mMarkers;
	}

	public void setmMarkers(Map<String, Long> mMarkers) {
		this.mMarkers = mMarkers;
	}	
}

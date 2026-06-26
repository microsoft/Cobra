package com.nuance.ncs.nmsp.engine.streamers;

import com.nuance.ncs.nmsp.engine.TransactionEngine;
import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * ToClientAudioStreamer streams TTS audio back to the HTTP client.
 */
public class ToClientAudioStreamer implements PdxAudioSink {

	private static final Logger log = LogManager.getLogger(ToClientAudioStreamer.class);

	private final String paramName;
	private final String mimeType;
	private TransactionEngine responseManager;

	public ToClientAudioStreamer(TransactionEngine responseManager, String paramName, String mimeType){
		this.responseManager = responseManager;
		this.paramName = paramName;
		this.mimeType = mimeType;
	}

	public void addAudioBuf(byte[] bufferIn, int offset, int length, boolean isLastBuf) {
		log.trace("ToClientAudioStreamer: addAudioBuf Received buffer of size: " + length + " and offset: " + offset);
		byte[] buffer = getSubArray(bufferIn, offset, length);
		if(buffer == null) {
			if(isLastBuf) {
				log.trace("ToClientAudioStreamer: addAudioBuf Last buffer received");
			}
		}
		else {
			responseManager.sendAudioPart(buffer, paramName, mimeType);
		}
	}

	public static byte[] getSubArray(byte[] array, int offset, int length) {
		if(array == null)
			return array;

		byte[] result = new byte[length];
	    System.arraycopy(array, offset, result, 0, length);
	    return result;
	}

}

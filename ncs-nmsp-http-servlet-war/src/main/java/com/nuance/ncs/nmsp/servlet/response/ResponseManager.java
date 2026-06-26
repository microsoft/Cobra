package com.nuance.ncs.nmsp.servlet.response;

import com.nuance.ncs.nmsp.servlet.NcsServletDefines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResponseManager {
	private static final Logger log = LogManager.getLogger(ResponseManager.class);

	private static final String BOUNDARY = "--Nuance_NMSP_vutc5w1XobDdefsYG3wq";
	
	private static final String JSON_CONTENT_DIPOSITION = "form-data; name=\"<REP>\"";
	private static final String JSON_CONTENT_TYPE = "application/JSON; charset=utf-8";
	private static final String AUDIO_CONTENT_DIPOSITION = "form-data; name=\"<REP>\"; paramName=\"<PRM>\"";
	private static final String AUDIO_CONTENT_TYPE = "";
		
	private final HttpServletResponse response;
	private final String context;
	private MultipartStreamer multiPartStreamer;
	private String sessionId = null;
	private boolean firstResponse;
	private boolean terminated = false;
	
	public ResponseManager(HttpServletResponse response, String context)
	{
		firstResponse = true;
		this.response = response;
		this.context = (context == null) ? UUID.randomUUID().toString() : context;
		log.debug("context:{}", this.context);
		ESAPI.httpUtilities().addHeader(this.response, NcsServletDefines.NUANCE_CONTEXT_HEADER, this.context);
	}
	
	private void initiateSuccessReply(String origin) throws IOException
	{
		log.trace("initiateSuccessReply");
		firstResponse = false;
	
		response.setStatus(HttpServletResponse.SC_OK);
		
		response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");
		
		response.addHeader("Transfer-Encoding", "chunked");

		ESAPI.httpUtilities().addHeader(response, "Nuance-SessionId", sessionId);
		ESAPI.httpUtilities().addHeader(response, "x-nuance-sessionid", sessionId);
				
		if(origin != null && !origin.isEmpty())
		{
			ESAPI.httpUtilities().addHeader(response, NcsServletDefines.HTTP_RESPONSE_ALLOW_ORIGIN_HEADER, origin);
		}
		
		multiPartStreamer = new MultipartStreamer(response, BOUNDARY, context);
	}
	
	
	public synchronized void sendJsonPart(String partName, JSONObject jsonData, boolean finalResponse, String origin) {
		log.trace("Sending JSON part");
		log.debug("JSON part is final response: {}", finalResponse);
		
		try {
			if (firstResponse)
				initiateSuccessReply(origin);

			multiPartStreamer.addPart(JSON_CONTENT_DIPOSITION.replace("<REP>", partName),
									   JSON_CONTENT_TYPE,
									   jsonData.toString().getBytes(StandardCharsets.UTF_8),
									   finalResponse, false);
			if(finalResponse) 
				terminate();
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported Encoding Exception in sendJsonPart(): ", e);
			terminate();
		} catch (Exception e) {
			log.error("Exception  in sendJsonPart(): ", e);
			terminate();
		}
	}
	
	public synchronized void sendAudioPart(String partName, byte[] data, String paramName, String mimeType, String origin) {
		log.trace("Sending Audio Part");
		try {
			if (firstResponse)
				initiateSuccessReply(origin);
		
			multiPartStreamer.addPart(AUDIO_CONTENT_DIPOSITION.replace("<REP>", partName).replace("<PRM>", paramName),
									   AUDIO_CONTENT_TYPE + mimeType, 
									   data, false, true);
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported Encoding Exception in sendAudioPart(): ", e);
			terminate();
		} catch (Exception e) {
			log.error("Exception in sendAudioPart(): ", e);
			terminate();
		}
	}
	
	public synchronized void setSessionId(String sessionId)
	{			
		this.sessionId = sessionId;
	}
	
	private synchronized void terminate()
	{		
		log.trace("Indicating processing ended");
		terminated = true;
		notifyAll();
		log.debug("Signalling all that response was sent");
	}
	
	/**
	 * Sends http status error
	 * 
	 * @param status
	 * @param errorMsg
	 */
	public synchronized void sendErrorReply( int status, String errorMsg)
	{
		log.error(errorMsg);
		firstResponse = false;
		try {
			if(sessionId != null)
			{
				ESAPI.httpUtilities().addHeader(response, "Nuance-SessionId", sessionId);
			}
			quietSendError(response, status, errorMsg);
		} catch (Exception e)
		{
			//Do nothing
		}
		terminate();
	}

	public static void quietSendError(HttpServletResponse resp, int code, String message) {
		try {
			resp.setStatus(code);
			resp.getOutputStream().print(message);
			resp.flushBuffer();
		} catch (IOException e) {
			//Do nothing
		}
	}

	/**
	 * Sends http status error
	 */
	public synchronized void waitForResponse() {
		// Only wait for 5 minutes
		AtomicBoolean done = new AtomicBoolean(false);
		while (!terminated || !done.get())
		{
			try {
				wait(300000);
			} catch (InterruptedException e) {
				log.warn("_syncObj.wait() e: ", e);
				Thread.currentThread().interrupt();
			}
			if (!terminated) // i.e., timed out
			{
				log.warn("Timed out waiting for final response to be sent.");
				sendErrorReply(HttpServletResponse.SC_BAD_GATEWAY, "Timed out waiting for final results");
			}
			done.set(true);
		}
	}
}

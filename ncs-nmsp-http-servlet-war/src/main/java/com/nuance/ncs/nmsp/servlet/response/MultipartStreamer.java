package com.nuance.ncs.nmsp.servlet.response;

import com.nuance.ncs.nmsp.servlet.NcsServletDefaults;
import com.nuance.ncs.nmsp.servlet.NcsServletDefines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MultipartStreamer
{
	private static final Logger log = LogManager.getLogger(MultipartStreamer.class);

	private static final String RESPONSE_CONTENT_TYPE = "multipart/form-data; boundary=";
	private static final String CONTENT_DISPOSITION = "Content-Disposition: ";
	private static final String PART_CONTENT_TYPE = "Content-Type: ";

	private static final String CRLF = "\r\n";
	private static final byte[] CRLF_BYTES = CRLF.getBytes(StandardCharsets.UTF_8);

	private final String boundaryText;
	private final HttpServletResponse httpResponse;
	private final String nuanceContext;

	ServletOutputStream outputStream;

	public MultipartStreamer(HttpServletResponse response, String boundary, String context)
			throws IOException
	{
		// Save the response object and output stream
		httpResponse = response;
		boundaryText = boundary;
		nuanceContext = context;

		outputStream = response.getOutputStream();

		//Content-Type: multipart/form-data; boundary=--Nuance_NMSP_vutc5w1XobDdefsYG3wq
		httpResponse.setContentType(RESPONSE_CONTENT_TYPE + boundaryText);

		//----Nuance_NMSP_vutc5w1XobDdefsYG3wq
		outputStream.write(getBoundaryBytes(false));
	}

	public void addPart(String contentDisposition, String contentType, byte[] data, boolean finalResult, boolean isAudioBodyPart)
			throws IOException
	{
		log.trace("Writing HTTP part to output stream");

		//Content-Disposition: form-data; name="QueryResult"
		String contentDispositionHeader = CONTENT_DISPOSITION + contentDisposition + CRLF;
		outputStream.write(contentDispositionHeader.getBytes(StandardCharsets.UTF_8));

		//Content-Type: application/JSON; charset=utf-8
		String contentTypeHeader = PART_CONTENT_TYPE + contentType + CRLF;
		outputStream.write(contentTypeHeader.getBytes(StandardCharsets.UTF_8));

		//Nuance-Context: 21fc8ee5-e7f4-4e1f-abbc-5b8a3803c5c9
		String nuanceContextHeader = NcsServletDefines.NUANCE_CONTEXT_HEADER + ": " + nuanceContext + CRLF;
		outputStream.write(nuanceContextHeader.getBytes(StandardCharsets.UTF_8));

		//line break between headers and body
		outputStream.write(CRLF_BYTES);

		//body
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(data);
		byteArrayOutputStream.write(CRLF_BYTES);
		outputStream.write(byteArrayOutputStream.toByteArray());

		flushAndAllowFlushTime(isAudioBodyPart, NcsServletDefaults.FLUSH_TIME_MS);

		//----Nuance_NMSP_vutc5w1XobDdefsYG3wq(--)
		if (finalResult)
		{
			outputStream.write(getBoundaryBytes(true));
			outputStream.close();
			outputStream = null;
		}
		else
		{
			outputStream.write(getBoundaryBytes(false));
		}
	}

	void flushAndAllowFlushTime(boolean isAudioBodyPart, int flushTime)
			throws IOException
	{
		//For TTS, skip this
		if(isAudioBodyPart)
		{
			return;
		}

		/*
		 *  Flush and give time for jetty to write the chunk for ASR for YesTV
		 */

		/*
		 *  -1 or less, no flush, no sleep
		 *  0           flush   , no sleep
		 *  >0          flush,    sleep
		 */

		if (flushTime > -1)
		{
			log.trace("Flushing outputStream");
			outputStream.flush();
		}
		if (flushTime < 1)
		{
			return;
		}

		try
		{
			log.trace("Sleeping " + flushTime + "ms for flush to write");
			Thread.sleep(flushTime);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread()
				  .interrupt();
		}
	}

	private byte[] getBoundaryBytes(boolean isTerminating)
	{
		String boundaryString = "--" + boundaryText;
		if(isTerminating) boundaryString += "--";
		boundaryString += CRLF;
		return boundaryString.getBytes(StandardCharsets.UTF_8);
	}

}

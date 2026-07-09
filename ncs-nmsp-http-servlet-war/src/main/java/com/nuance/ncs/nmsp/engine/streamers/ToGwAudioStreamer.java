package com.nuance.ncs.nmsp.engine.streamers;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.engine.utils.OggStreamer;
import com.nuance.ncs.nmsp.servlet.utils.AudioCodecType;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ToGwAudioStreamer {

	private static final Logger log = LogManager.getLogger(ToGwAudioStreamer.class);
	private static final int MAX_BUFFERING_LENGTH = 4*24*1024;
	private static final String OGG_ENVELOPE = "audio/ogg";

	protected MultipartStream multiStream;
	protected InputStream inputStream;
	protected final PdxAudioSink audioSink;
	protected final ICodecResource.Codec codec;
	private boolean encapsulatedOgg;
	OggStreamer oggStreamer;

	protected boolean finished = false;

	private static final String PUSHING = "ToGwAudioStreamer: Pushing ";
	private static final String BYTES_OF_AUDIO = "bytes of audio to GW";

	// The codec is used to determine how to split audio
	public ToGwAudioStreamer(PdxAudioSink audioSink, ICodecResource.Codec codec) throws IOException {
		this.audioSink = audioSink;
		this.codec = codec;
		oggStreamer = new OggStreamer(audioSink);
	}

	public void initialize(MultipartStream multipartStream, String partContentType) {
		this.multiStream = multipartStream;
		finished = false;
		if (partContentType != null && partContentType.startsWith(OGG_ENVELOPE)){
			encapsulatedOgg = true;
		}
	}

	public synchronized void stream(){

		if(audioSink == null)
		{
			log.error("ToGwAudioStreamer: AudioSink is null. Possibly the Request Data was wrong");
			return;
		}
		if (isEncapsulatedOgg())
			streamOgg();
		else
			streamPlainAudio();
	}

	private void streamOgg() {
		try {
			log.debug("Stream ogg");
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			while ((multiStream.readBodyData(output)) > 0){
				oggStreamer.stream(output.toByteArray());
			}
			oggStreamer.flush();
		} catch (IOException e) {
			log.error("ToGwAudioStreamer: while streaming ogg ", e);
			finished = true;
		}

	}

	private void streamPlainAudio() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			// If PCM or MP3 take care of splitting here
			if(AudioCodecType.isPcm(codec) || AudioCodecType.isMpeg(codec)) {
				while (multiStream.readBodyData(output) > 0){
					sendInParts(output.toByteArray());
					output.reset();
				}
			}
			else { // else Read all data in part

				while ((multiStream.readBodyData(output)) > 0)
				{
					//Do nothing
				}
				// Push audio to GW in one part
				byte[] audioBuf = new byte[output.size()];
				System.arraycopy(output.toByteArray(), 0, audioBuf, 0, output.size());
				log.trace(PUSHING + audioBuf.length + BYTES_OF_AUDIO);
				int length = audioBuf.length;
				audioSink.addAudioBuf(audioBuf, 0, length, false);
			}

			finished = true;
			log.trace("ToGwAudioStreamer: Streamed all audio available in audio part.");
		} catch (IOException e) {
			log.error("ToGwAudioStreamer: While streaming received IOException: ", e);
			finished = true;
		}
	}

	//Send final audio buffer to indicate no more audio for this AudioSink
	public void terminate()
	{
		try {
			audioSink.addAudioBuf(null, 0, 0, true);
		} catch (IOException e) {
			log.error("ToGwAudioStreamer: While terminating received IOException: ", e);
		} // Last audio buffer sent
	}

	private void sendInParts(byte[] audioToSplit) throws IOException
	{
		if(audioToSplit.length < MAX_BUFFERING_LENGTH) {
			// Push audio to GW in one part
			byte[] audioBuf = new byte[audioToSplit.length];
			System.arraycopy(audioToSplit, 0, audioBuf, 0, audioToSplit.length);

			log.trace(PUSHING + audioBuf.length + BYTES_OF_AUDIO);
			int length = audioBuf.length;
			audioSink.addAudioBuf(audioBuf, 0, length, false);
		}
		else {
			int bookMark = 0;
			// While there is at least MAX_BUFFERING_LENGTH number of bytes
			while ((bookMark*MAX_BUFFERING_LENGTH) < (audioToSplit.length - MAX_BUFFERING_LENGTH)) {
				byte[] audioBuf = new byte[MAX_BUFFERING_LENGTH];

				System.arraycopy(audioToSplit, bookMark*MAX_BUFFERING_LENGTH, audioBuf, 0, MAX_BUFFERING_LENGTH);
				bookMark++;

				log.trace(PUSHING + audioBuf.length + BYTES_OF_AUDIO);
				int length = audioBuf.length;
				audioSink.addAudioBuf(audioBuf, 0, length, false);
			}

			// Send remaining audio
			int leftOver = audioToSplit.length - bookMark*MAX_BUFFERING_LENGTH;
			if(leftOver > 0) {
				byte[] audioBuf = new byte[leftOver];
				System.arraycopy(audioToSplit, bookMark*MAX_BUFFERING_LENGTH, audioBuf, 0, leftOver);

				log.trace(PUSHING + audioBuf.length + BYTES_OF_AUDIO);
				int length = audioBuf.length;
				audioSink.addAudioBuf(audioBuf, 0, length, false);
			}
		}
	}

	public boolean isEncapsulatedOgg() {
		return encapsulatedOgg;
	}

	public void setEncapsulatedOgg(boolean encapsulatedOgg) {
		this.encapsulatedOgg = encapsulatedOgg;
	}
}

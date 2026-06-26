package com.nuance.ncs.nmsp.engine.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.xiph.speex.OggCrc;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("java:S3776")
public class OggStreamer {
	private static final Logger log = LogManager.getLogger(OggStreamer.class);

	enum OggStreamStatus
	{
		SEEKING_NEW_HEADER,
		SEEKING_NEW_HEADER_FOUND_O,
		SEEKING_NEW_HEADER_FOUND_OG,
		SEEKING_NEW_HEADER_FOUND_OGG,
		READING_HEADER,
		READING_DATA,
		READING_SEGMENTS
	}

	OggStreamStatus status = OggStreamStatus.SEEKING_NEW_HEADER;

	private static final int NUM_SEGMENTS_DIR = 26;
	private static final int OGG_HEADERSIZE = 27;

	private static final int BUFFER_SIZE = 1310720;
	private static final int MAX_STREAM_WINDOW = 131072;

	PipedOutputStream outputStream;
	PipedInputStream inputStream;
	private DataInputStream dataInputStream;
	byte [] oggSignature = "OggS".getBytes();

	byte[] header = new byte[2048];
	private int remainingBytes;
	private int chksum;
	private int numSegments;
	private int currentSegment;

	private PdxAudioSink audioSink;

	private int originalChecksum;

	public OggStreamer(PdxAudioSink audioSink) throws IOException {
		this.audioSink = audioSink;
		outputStream = new PipedOutputStream();
		inputStream = new PipedInputStream(BUFFER_SIZE);
		inputStream.connect(outputStream);
		dataInputStream = new DataInputStream(inputStream);
	}
	public void stream(byte[] data) throws IOException{
		log.debug("Stream");
		int dataLength = (data==null)?0:data.length;
		if (log.isDebugEnabled()){
			log.debug(String.format("About to stream part fragment. Len=%d", dataLength));
		}
		int bufferPos;
		int bufferSize;
		for (bufferPos = 0; dataLength > 0; dataLength-= MAX_STREAM_WINDOW){
			bufferSize = (dataLength < MAX_STREAM_WINDOW)?dataLength:MAX_STREAM_WINDOW;
			stream(data, bufferPos, bufferSize);
			bufferPos += bufferSize;
		}
	}
	protected void stream(byte[] data, int offset, int length) throws IOException {
		try {
			log.debug("Ogg stream");
			outputStream.write(data,offset, length);
			remainingBytes += length;
			while (remainingBytes > 0) {
				switch(status){
				case SEEKING_NEW_HEADER:
				case SEEKING_NEW_HEADER_FOUND_O:
				case SEEKING_NEW_HEADER_FOUND_OG:
				case SEEKING_NEW_HEADER_FOUND_OGG:
					findHeader();
					break;
				case READING_HEADER:
					log.debug(">>> READING_HEADER");
					if (remainingBytes < OGG_HEADERSIZE-4) {
						log.debug(">>> Not enough info to read header");
						return;
					}
					System.arraycopy(oggSignature, 0, header, 0, oggSignature.length);
					dataInputStream.readFully(header, 4, OGG_HEADERSIZE-4);
					handleReadHeader();
					break;
				case READING_SEGMENTS:
					log.debug(">>> READING SEGMENTS");
					if (remainingBytes < numSegments)
						return;
					handleReadSegments();
					break;
				case READING_DATA:
					log.debug(">>> READING DATA");
					while (currentSegment < numSegments) {
						int bytesInCurrentSegment = header[NUM_SEGMENTS_DIR + 1
								+ currentSegment] & 0xff;
						if (remainingBytes < bytesInCurrentSegment) {
							log.debug("Not enough bytes to read for current segment");
							return;
						}
						handleReadData(bytesInCurrentSegment);
					}
					verifyChecksum();
					status = OggStreamStatus.SEEKING_NEW_HEADER;
					break;
				}
			}
		} catch (EOFException eofe) {
			log.error("Not enough info to read. Incomplete segment in part.");
		} catch (IOException ioe) {
			log.error("Error processing audio stream", ioe);
		}
	}

	private void verifyChecksum() {
		if (originalChecksum != chksum) {
			log.error("Checksum mismatch");
			if (log.isDebugEnabled()) {
				log.debug(String.format("Expected %d got %d",
						originalChecksum, chksum));
			}
		}
	}

	private void handleReadData(int bytesInCurrentSegment) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug(String.format(">>> Current Segment : %d",
					currentSegment));
			log.debug(String.format(
					">>> Bytes in current Segment : %d",
					bytesInCurrentSegment));
		}
		byte[] payload = new byte[bytesInCurrentSegment];
		dataInputStream.readFully(payload);
		remainingBytes -= bytesInCurrentSegment;
		chksum = OggCrc.checksum(chksum, payload, 0,
				bytesInCurrentSegment);
		log.debug(String.format("Chksum: %d", chksum));
		if (bytesInCurrentSegment > 0) {
			log.debug(String.format(">>> Sending DATA %d",
					payload.length));
			audioSink.addAudioBuf(payload, 0, payload.length,
					false);
		}
		currentSegment++;
	}

	private void handleReadSegments() throws IOException {
		dataInputStream.readFully(header, OGG_HEADERSIZE,
				numSegments);
		currentSegment = 0;
		chksum = OggCrc.checksum(chksum, header, OGG_HEADERSIZE,
				numSegments);
		log.debug(String.format(
				"numSegments: %d; remainingBytes = %d",
				numSegments, remainingBytes));
		status = OggStreamStatus.READING_DATA;
		remainingBytes -= numSegments;
	}

	private void handleReadHeader() {
		ByteBuffer bb = ByteBuffer.wrap(header);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		originalChecksum = bb.getInt(22);
		bb.putInt(22, 0);
		chksum = OggCrc.checksum(0, header, 0, OGG_HEADERSIZE);
		numSegments = header[NUM_SEGMENTS_DIR] & 0xff;
		if (log.isDebugEnabled()) {
			log.debug(String.format("Numsegments: %d", numSegments));
			log.debug(String.format("Checksum: %d",
					originalChecksum));
			log.debug(String.format("Header: %d %d %d %d",
					header[22] & 0xff, header[23] & 0xff,
					header[24] & 0xff, header[25] & 0xff));
		}
		remainingBytes -= OGG_HEADERSIZE;
		status = OggStreamStatus.READING_SEGMENTS;
	}

	public void flush() throws IOException {
			stream(new byte[0],0,0);
	}

	private void findHeader() throws IOException {
		log.debug("Finding Ogg Header");
		while(remainingBytes > 0){
			int b = dataInputStream.read();
			remainingBytes --;
			switch(status){
			case SEEKING_NEW_HEADER:
				if (b == oggSignature[0]){
					status = OggStreamStatus.SEEKING_NEW_HEADER_FOUND_O;
				}
				break;
			case SEEKING_NEW_HEADER_FOUND_O:
				if(b == oggSignature[0])
				{
					//Do nothing
				} else if (b == oggSignature[1]){
					status = OggStreamStatus.SEEKING_NEW_HEADER_FOUND_OG;
				} else {
					status = OggStreamStatus.SEEKING_NEW_HEADER;
				}
				break;
			case SEEKING_NEW_HEADER_FOUND_OG:
				if (b == oggSignature[0])
				{
					//Do nothing
				} else if (b == oggSignature[2]){
					status=OggStreamStatus.SEEKING_NEW_HEADER_FOUND_OGG;
				} else {
					status = OggStreamStatus.SEEKING_NEW_HEADER;
				}
				break;
			case SEEKING_NEW_HEADER_FOUND_OGG:
				if (b == oggSignature[0])
				{
					//Do nothing
				} else if (b == oggSignature[3]){
					status = OggStreamStatus.READING_HEADER;
					log.debug("Header found");
					return;
				}else{
					log.debug("False header, seeking header again");
					status = OggStreamStatus.SEEKING_NEW_HEADER;
				}
				break;
			default:
				status = OggStreamStatus.SEEKING_NEW_HEADER;
			}
		}
	}

	public PdxAudioSink getAudioSink() {
		return audioSink;
	}

	public void setAudioSink(PdxAudioSink audioSink) {
		this.audioSink = audioSink;
	}
}

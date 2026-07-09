package com.nuance.plugin.driver_v2;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpInterfaceParser extends Thread {
	private static final Logger logger = LogManager.getLogger();
	private static final String TAG = "TAG-HttpInterfaceParser";
	
	public interface Listener {
		public void onStatusLine(int code, String status);
		public void onHttpHeader(String name, String value);
		public void onChunkHeader(String name, String value);
		public void onQueryResult(JSONObject msg);
		public void onQueryRetry(JSONObject msg);
		public void onQueryError(JSONObject msg);
		public void onError(String msg);
		public void onEndOfStream();
		public void onAudio(ByteBuffer audio);
	}
	
	private enum responseType {
		QueryResult,
		QueryRetry,
		QueryError,
		Error,
		Unknown
	}
	
	private HttpInterfaceParser.Listener mListener = null;
	private int statusCode = 0;
	private String statusString = null;
	private String nuanceSessionId = null;
	private String boundary = null;
	private boolean mStopped = false;
	
	private BlockingQueue<ByteBuffer> queue = null;
	
	private boolean mHeadersParsed = false;
	private responseType chunkResponseType = responseType.Unknown;
	
	public HttpInterfaceParser(HttpInterfaceParser.Listener listener) {
		mListener = listener;
	}
	
	public String getNuanceSessionId() {
		return nuanceSessionId;
	}
	
	@Override
	public void run() {

		try {
			queue = new ArrayBlockingQueue<ByteBuffer>(500);
			boolean parseAudio = false;
			boolean parseJSON = false;
			String partialJSON = "";
			int chunkHeaderCount = 0;
			int audioCounter = 0;
			boolean encodingIsChunked = false;
			
			while( !mStopped ) {
				String line = null;
				ByteBuffer buf = queue.take();
				
				BufferedReader mBufferedReader = new BufferedReader(new StringReader(new String(buf.array())));
				int pos = 0;

				mBufferedReader.mark(buf.capacity());
				while((line = mBufferedReader.readLine()) != null) {
					pos += line.length() + 4;

					if( (statusCode == 0 || statusCode == 100) && line.contains("HTTP/1") ) {
						logger.info( "Status Line: " + line);
						String[] statusDetails = line.split(" ", 3);
						statusCode = Integer.parseInt(statusDetails[1]);
						statusString = statusDetails[2];
						mListener.onStatusLine(statusCode, statusString);
						continue;
					}
					
					if( statusCode == 100 )
						continue;
					
					if( !mHeadersParsed ) {
						if( line.trim().isEmpty() ) {
							logger.info( "Headers Parsed");
							mHeadersParsed = true;
						} else {
							logger.info( "Header Line: " + line);
							String[] header = line.split(":", 2);
							if( header.length > 1 )
								mListener.onHttpHeader(header[0], header[1].trim());
							else
								mListener.onHttpHeader(header[0], "");
							
							if( header[0].equalsIgnoreCase("nuance-sessionid") ) {
								nuanceSessionId = header[1].trim();
							}
							else if( header[0].equalsIgnoreCase("transfer-encoding") ) {
								encodingIsChunked = header[1].trim().equalsIgnoreCase("chunked");
							}
							else if( header[0].equalsIgnoreCase("content-disposition") &&
									 header[1].toLowerCase().contains("error") ) {
								chunkResponseType = responseType.Error;
							}
							else if( header[0].equalsIgnoreCase("content-type") ) {
								String[] split = header[1].split("boundary=");
								if( split.length > 1 )
									boundary = split[1].trim();
							}
						}
						continue;
					}

					if( line.matches("0") ) {
						logger.info( "End of stream parsed");
						statusCode = 0;
						mHeadersParsed = false;
						mListener.onEndOfStream();
						break;
					}

					// This will happen if there is an HTTP error...
					if( !encodingIsChunked ) {
						logger.info(  "Non chunked data stream: " + line);
						continue;
					}

					if( encodingIsChunked && line.contains(boundary) ) {
						logger.info( "Boundary parsed");
						parseAudio = false;
						parseJSON = false;
						partialJSON = "";
						chunkHeaderCount = 0;
						continue;
					}

					if( parseJSON && chunkHeaderCount >= 3 ) {
						if( line.trim().isEmpty() )
							continue;
						
						if( !isJSON(line) ) {
							partialJSON += line;
							if( isJSON( partialJSON ) ) {
								line = partialJSON;
								partialJSON = "";
								parseJSON = false;
							}
							else
								continue;
						}
					}
					
					if( parseAudio && chunkHeaderCount >= 3 ) {

						if( line.trim().isEmpty() && parseAudio ) {
							logger.info( "Marker position: " + pos);

							if( (buf.capacity() - pos) <= 0 ) {
								logger.info( "Capacity - Pos <= 0");
								parseAudio = false;
								continue;
							}
							
							logger.info( "Received audio: " + (++audioCounter));
							byte[] initAudio = new byte[buf.capacity() - pos];
							System.arraycopy(buf.array(), pos, initAudio, 0, initAudio.length);
							mListener.onAudio(ByteBuffer.wrap(initAudio));
							break;
						}
						
						logger.info( "Received audio: " + (++audioCounter));
						buf.rewind();
						mListener.onAudio(buf);
						break;
					}
					
					if( line.matches("-?[0-9a-fA-F]+") ) {
						logger.info( "Hex value parsed");
						continue;
					}
					
					if( line.trim().isEmpty() ) {
						logger.info( "End of line/Empty line parsed");
						if( parseAudio ) {
							logger.info( "Marker position: " + pos);
						}
						continue;
					}
										
					if( isJSON(line) ) {
						logger.info( "JSON message parsed");
						
						JSONObject msg = new JSONObject(line);
						
						switch( chunkResponseType ) {
							case QueryResult:
								mListener.onQueryResult(msg);
								break;
							case QueryRetry:
								mListener.onQueryRetry(msg);
								break;
							case QueryError:
								mListener.onQueryError(msg);
								break;
							case Error:
								mListener.onError(msg.toString(4));
								break;
							case Unknown:
								break;
							default:
								break;
						}
						continue;
					} 
					
					if(StringUtils.isAsciiPrintable(line.trim())) {
						logger.info( "Chunk header parsed");
						chunkHeaderCount++;
						
						// must be a chunk header...
						String[] header = line.split(":", 2);
						if( header.length < 2 ) {
							//logger.info( header.toString());
							logger.info( "Failed Chunk Header Parsing: " +  line);
							continue;
						}
						mListener.onChunkHeader(header[0], header[1]);
						
						if( header[0].equalsIgnoreCase("content-disposition") &&
								header[1].toLowerCase().contains("name=\"audio\"")) {
							parseAudio = true;
						}

						if( header[0].equalsIgnoreCase("content-type") &&
								header[1].toLowerCase().contains("application/json")) {
							parseJSON = true;
						}

						if( header[0].equalsIgnoreCase("content-disposition") ) {
							if( header[1].toLowerCase().contains("queryresult") )
								chunkResponseType = responseType.QueryResult;
							else if( header[1].toLowerCase().contains("queryretry") )
								chunkResponseType = responseType.QueryRetry;
							else if( header[1].toLowerCase().contains("queryerror") )
								chunkResponseType = responseType.QueryError;
							else if( header[1].toLowerCase().contains("error") )
								chunkResponseType = responseType.Error;
						}
						continue;						
					}
					else {
						// More audio...
						pos = 0;
						logger.info( "Received audio: " + (++audioCounter));
						byte[] initAudio = new byte[buf.capacity() - pos];
						System.arraycopy(buf.array(), pos, initAudio, 0, initAudio.length);
						mListener.onAudio(ByteBuffer.wrap(initAudio));
						parseAudio = true;
						break;
					}
				}
			}

		} catch( Exception e ) {
			e.printStackTrace();
			logger.info( e.getMessage());
		}
		
	}
	
	private boolean isJSON(String s) {
        try {
            @SuppressWarnings("unused")
			JSONObject json = new JSONObject(s);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

	public void stopParser() {
		mStopped = true;
		queue.clear();
		queue = null;
	}

	public void parseLine(String line) {
		queue.add(ByteBuffer.wrap(line.getBytes()));
	}
	
	public void parseBuffer(ByteBuffer buf) {
		queue.add(buf);
	}
}

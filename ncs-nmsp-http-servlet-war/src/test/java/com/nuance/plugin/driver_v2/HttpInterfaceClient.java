package com.nuance.plugin.driver_v2;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.nuance.plugin.driver_v2.io.SocketConnector;
import com.nuance.plugin.driver_v2.io.SocketReader;
import com.nuance.plugin.driver_v2.io.SocketWriter;
import com.nuance.plugin.driver_v2.util.Chunk;

public class HttpInterfaceClient {
	private static final Logger logger = LogManager.getLogger();
    private static final String TAG = "TAG-HttpInterfaceClient";

    public interface Listener {
        void onOpen();
        void onConnected();
        void onDisconnected();
        void onMessage(ByteBuffer msg);
        void onQueryResponse(JSONObject msg);
        void onQueryRetry(JSONObject msg);
        void onQueryError(JSONObject msg);
        void onQueryEnd();
        void onClose();
        void onError(String error);
    }

    private SocketConnector mSocketConnector = null;
    private SocketReader mSocketReader = null;
    private SocketWriter mSocketWriter = null;
    private HttpInterfaceParser mParser = null;
    
    private HttpInterfaceClient.Listener mHttpInterfaceClientListener = null;
    private Socket mSocket = null;
    private boolean mConnected = false;
    private boolean mConnecting = false;
    
    private volatile String mBoundary = null;
    
	private volatile Thread mConnectionMonitorThread = null;
	private volatile static Object mConnectionWaitLock = new Object();
	protected int mConnectionTimeout = 60000;
	protected volatile boolean mConnectionTimedOut = false;
	
    private long socketFactoryTimestamp = 0;
    private long createSocketTimestamp = 0;
    private long connectAsyncBeginTimestamp = 0;
    private long connectAsyncEndTimestamp = 0;

    /** GETTERS/SETTERS **/
    public boolean isConnected() {
        return mConnected;
    }
    
    public boolean isConnecting() {
    	return mConnecting;
    }
    
    public String getBoundary() {
    	return mBoundary;
    }
    
    /** CONSTRUCTOR **/
    public HttpInterfaceClient( HttpInterfaceClient.Listener listener ) throws Exception {
    	
        if( listener == null ) {
            logger.info( "ERROR: Null listener provided to HttpInterfaceClient!");
            throw new Exception("ERROR: Null listener provided to HttpInterfaceClient!");
        }

        mHttpInterfaceClientListener = listener;
        
        logger.info( "URI: " + Configuration.SERVER_URI.toString());
        //URI uri = URI.create(Configuration.SERVER_URI.toString());

    }
 
    private void createSocket() {
    	
    	try {
            // Create a WebSocketFactory instance.
            socketFactoryTimestamp = System.currentTimeMillis();
            logger.info( "Socket factory timestamp: " + socketFactoryTimestamp);
            
            mSocketConnector = new SocketConnector(new SocketConnector.Listener() {
				
				@Override
				public void onError(String msg) {
					logger.info( msg);
		    		mConnected = false;
		    		mConnecting = false;
		    		mHttpInterfaceClientListener.onError(msg);
				}
				
				@Override
				public void onConnected(Socket socket) {
					try {
						logger.info( "Socket connection created.");
						mConnected = true;
						mConnecting = false;
						mSocket = socket;
						mConnectionTimeout = mSocket.getSoTimeout();
						
						mParser = new HttpInterfaceParser(new HttpInterfaceParser.Listener() {
							
							@Override
							public void onStatusLine(int code, String status) {
								logger.info( "Status Code: " + code);
								logger.info( "Status Message: " + status);
								
								if( code >= 400 )
									mHttpInterfaceClientListener.onError(status);
							}
							
							@Override
							public void onQueryRetry(JSONObject msg) {
								try {
									logger.info( "QueryRetry: " + msg.toString(4));
									msg.put("NMAS_PRFX_SESSION_ID", mParser.getNuanceSessionId());
									mHttpInterfaceClientListener.onQueryRetry(msg);
								} catch (JSONException e) {
									logger.info( e.getMessage());
								}
							}
							
							@Override
							public void onQueryResult(JSONObject msg) {
								try {
									logger.info( "QueryResult: " + msg.toString(4));
									mHttpInterfaceClientListener.onQueryResponse(msg);
								} catch (JSONException e) {
									logger.info( e.getMessage());
								}
							}
							
							@Override
							public void onQueryError(JSONObject msg) {
								try {
									logger.info( "QueryError: " + msg.toString(4));
									msg.put("NMAS_PRFX_SESSION_ID", mParser.getNuanceSessionId());
									mHttpInterfaceClientListener.onQueryError(msg);
								} catch (JSONException e) {
									logger.info( e.getMessage());
								}
							}
							
							@Override
							public void onAudio(ByteBuffer audio) {
								try {
									logger.info( "Received Audio: " + audio.capacity());
									mHttpInterfaceClientListener.onMessage(audio);
								} catch( Exception e ) {
									logger.info( e.getMessage());
								}
							}
							
							@Override
							public void onHttpHeader(String name, String value) {
								logger.info( "HTTP Header: " + name + ": " + value.trim());
							}
							
							@Override
							public void onError(String msg) {
								logger.info( "Parse Error: " + msg);
							}
							
							@Override
							public void onChunkHeader(String name, String value) {
								logger.info( "Chunk Header: " + name + ": " + value.trim());								
							}

							@Override
							public void onEndOfStream() {
								logger.info(  "End of Response Stream");
								mHttpInterfaceClientListener.onQueryEnd();
								
							}
						});
						mParser.start();
						
						mSocketReader = new SocketReader(mSocket, new SocketReader.Listener() {
							
							@Override
							public void onMessage(String line) {
								if( StringUtils.isAlphanumeric(line))
									logger.info( "Socket Message Received: " + line);
								mParser.parseLine(line);
							}
							
							@Override
							public void onMessage(ByteBuffer buf) {
								mParser.parseBuffer(buf);
							}
							
							@Override
							public void onError(String msg) {
								logger.info( "Socket Read Error Received: " + msg);
								mHttpInterfaceClientListener.onError(msg);
							}

						});
						mSocketReader.start();

						mSocketWriter = new SocketWriter(mSocket, new SocketWriter.Listener() {
							
							@Override
							public void onWrite(String line) {
								logger.info( line);
							}
							
							@Override
							public void onError(String msg) {
								logger.info( msg);
								mHttpInterfaceClientListener.onError(msg);
							}
						});
						mSocketWriter.start();
						
						mBoundary = UUID.randomUUID().toString().replaceAll("-", "");

		    			// Monitor the connection timeout 
		    			mConnectionMonitorThread = new Thread(null, mConnectionTimeoutMonitor, "connection-monitor-thread");				
		    			mConnectionMonitorThread.start();
		    			
		    			mHttpInterfaceClientListener.onConnected();

					} catch( Exception e ) {
						logger.info( e.getMessage());
			    		mConnected = false;
			    		mConnecting = false;
			    		mHttpInterfaceClientListener.onError(e.getMessage());
					}
					
					
				}
			});
            mHttpInterfaceClientListener.onOpen();
            mSocketConnector.start();

    	} catch( Exception e ) {
    		logger.info( e.getMessage());
    		mConnected = false;
    		mConnecting = false;
    		mHttpInterfaceClientListener.onError(e.getMessage());
    	}
    }
    
    /** PUBLIC METHODS **/
    public void connect() throws Exception {
        logger.info( "Creating connection to HTTP Interface");

        if(mConnected) {
            logger.info( "Already mConnected to HTTP Interface");
            return;
        }
        else if ( mSocket != null && mConnecting ) {
            logger.info( "Already trying to connect to HTTP Interface");
            return;
        }
        
        mConnecting = true;

        connectAsyncBeginTimestamp = System.currentTimeMillis();
        logger.info( "Connect Asynchronously Begin timestamp: " + connectAsyncBeginTimestamp);
        createSocket();
    }
    
    public void sendHeaders(final ArrayList<String> headers) {
    	try {
    		if(!mSocket.isConnected()) {
    			logger.info( "Sending to disconnected socket");
    			return;
    		}
    		
    		StringBuilder sb = new StringBuilder();
    		
    		Iterator<String> list = headers.iterator();
    		while( list.hasNext() )
    			sb.append(list.next() + "\r\n");
    		sb.append("\r\n");

    		mSocket.getOutputStream().write(sb.toString().getBytes());
    		logger.info(  mSocket.toString());
    		
    		mSocket.getOutputStream().flush();

    	} catch (Exception e) {
			logger.info( e.getMessage());
		}
    }
    
    public void sendMessage(final ArrayList<String> headers, final JSONObject dictionary) {
    	try {
    		if(!mSocket.isConnected()) {
    			logger.info( "Sending to disconnected socket");
    			return;
    		}
    		
			Chunk chunk = new Chunk();
			chunk.append("--" + mBoundary + "\r\n");
    		Iterator<String> list = headers.iterator();
    		while( list.hasNext() )
    			chunk.append(list.next() + "\r\n");
			chunk.append("\r\n");
			chunk.append(dictionary.toString(2));
			chunk.append("\r\n");
			
			mSocketWriter.write(chunk);
    		
    	} catch (Exception e) {
			logger.info( e.getMessage());
		}
    }

    public void sendMessage(final ArrayList<String> headers, final byte[] audio) {
    	try {
    		if(!mSocket.isConnected()) {
    			logger.info( "Sending to disconnected socket");
    			return;
    		}
    		
			Chunk chunk = new Chunk();
			chunk.append("--" + mBoundary + "\r\n");
			Iterator<String> list = headers.iterator();
    		while( list.hasNext() )
    			chunk.append(list.next() + "\r\n");
			chunk.append("\r\n");
			chunk.append(audio);
			chunk.append("\r\n");
			
			mSocketWriter.write(chunk);
						
    	} catch (Exception e) {
			logger.info( e.getMessage());
		}
    }

    public void sendMessage() {
    	try {
    		if(!mSocket.isConnected()) return;
    		
			Chunk chunk = new Chunk();
			chunk.append("--" + mBoundary + "--\r\n");
			mSocketWriter.write(chunk);
	
			Chunk terminatingChunk = new Chunk();
			mSocketWriter.write(terminatingChunk);
			
		} catch (Exception e) {
			logger.info( e.getMessage());
		}

    }
    
    Runnable mConnectionTimeoutMonitor = new Runnable() {

		@Override
		public void run() {
			mConnectionTimedOut = false;
			
			synchronized(mConnectionWaitLock) {
				try {					
					logger.info( "Connection timer started." );
					mConnectionWaitLock.wait(mConnectionTimeout);
					
					logger.info( "Connection timed out.");
					
					mHttpInterfaceClientListener.onClose();
					mHttpInterfaceClientListener.onDisconnected();
					
					// reset some flags...
					mConnectionTimedOut = true;
					
				} catch( InterruptedException e) {
					// Timer cancelled. Nothing to do...
					logger.info( "Connection timer interrupted." );					
				}
			}

		}
		
	};
	
}
	
package com.nuance.plugin.driver_v2.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketReader extends Thread {
	private static final Logger logger = LogManager.getLogger();
	private static final String TAG = "TAG-SocketReader";
	
	public interface Listener {
		public void onMessage(String line);
		public void onMessage(ByteBuffer buf);
		public void onError(String msg);
	}
	
	Socket mSocket = null;
	SocketReader.Listener mListener = null;
	BufferedReader mBufferedReader = null;
	BufferedInputStream mBufferedInputStream = null;
	
	public SocketReader(Socket s, SocketReader.Listener listener) {
		mSocket = s;
		mListener = listener;
		
		try {			
			mBufferedInputStream = new BufferedInputStream(mSocket.getInputStream());
		} catch (IOException e) {
			logger.info( e.getMessage());
			mListener.onError(e.getMessage());
		}
	}

	public void run() {

		try 
		{
			int size = 4096;
			byte[] b = new byte[size];			
			int nRead = 0;
			
			while(((nRead = mBufferedInputStream.read(b, 0, size)) != -1)) {
				for( int i = 0; i < nRead; i++ )
					System.err.print((char)b[i]);
				System.err.println();
				ByteBuffer buf = ByteBuffer.allocate(nRead);
				buf.put(b, 0, nRead);
				
				mListener.onMessage(buf);
				
			}
		}
		catch (Exception e) {
			logger.info( e.getMessage());
			mListener.onError(e.getMessage());
		}
	}
}

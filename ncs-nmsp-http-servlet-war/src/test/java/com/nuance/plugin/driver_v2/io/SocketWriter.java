package com.nuance.plugin.driver_v2.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.nuance.plugin.driver_v2.util.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocketWriter extends Thread {
	private static final Logger logger = LogManager.getLogger();
	private static final String TAG = "TAG-SocketWriter";
	
	public interface Listener {
		public void onWrite(String line);
		public void onError(String msg);
	}
	
	Socket mSocket = null;
	SocketWriter.Listener mListener = null;
	volatile OutputStream mOutputStream = null;
	BlockingQueue<Chunk> queue = null;
	boolean mStopped = false;
	
	public SocketWriter(Socket s, SocketWriter.Listener listener) {
		mSocket = s;
		mListener = listener;
		
		try {
			mOutputStream = mSocket.getOutputStream();
		} catch (IOException e) {
			logger.info( e.getMessage());
			mListener.onError(e.getMessage());
		}
	}

	public void stopWriter() {
		mStopped = true;
		queue.clear();
	}
	
	public void run() {
		try 
		{
			mStopped = false;
			queue = new ArrayBlockingQueue<Chunk>(1024);
			int chunckCount = 0;
			while( !mStopped ) {
				Chunk chunk = queue.take();			
				chunk.writeTo(mOutputStream);
				chunckCount++;
				mListener.onWrite("Chunk " + chunckCount + " sent");
//				Thread.sleep(20);
			}
		}
		catch (Exception e) {
			logger.info( e.getMessage());
			mListener.onError(e.getMessage());
		}
	}
	
	public void write(Chunk chunk) {
		queue.add(chunk);
	}

}

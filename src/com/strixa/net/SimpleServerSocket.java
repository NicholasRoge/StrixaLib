package com.strixa.net;

import java.net.ServerSocket;

import com.strixa.net.SimpleClientSocket.DataReceivedListener;

public class SimpleServerSocket
implements DataReceivedListener{
	public interface ClientConnectListener{
		
	}
	
	private int          __port;
	private ServerSocket __socket;
	private boolean      __verbose;
	
	
	/*Begin Constructors*/
	public SimpleServerSocket(int port){
		this.__port = port;
	}
	/*End Constructors*/
	
	/*Begin Getter/Setter Methods*/
	/**
	 * Sets this object to be verboseness.
	 * 
	 * @param verbose If true, log messages will be printed.  
	 */
	public void setVerbose(boolean verbose){
		this.__verbose = verbose;
	}
	/*End Getter/Setter Methods*/
	
	/*Begin Other Methods*/
	public void connect(){
		
	}
	
	public void onDataReceived(SimpleClientSocket client,byte[] data){
		
	}
	/*End Other Methods*/
}

package com.strixa.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

import com.strixa.util.Log;

/**
 * Simplifies the use of sockets by automating some of the concepts of socket creation such as listing for and sending data. 
 */
public class SimpleClientSocket implements Runnable{
	/**
	 * Notifies the listener of data received by a {@link com.strixa.net.SimpleClientSocket} object.
	 */
	public interface DataReceivedListener{
		/**
		 * Called when data is received by the {@link com.strixa.net.SimpleClientSocket}. 
		 * 
		 * @param socket {@link com.strixa.net.SimpleClientSocket} which received the data.
		 * @param data Data which was received.
		 */
		public void onDataReceived(SimpleClientSocket socket,byte[] data);
	}
	
	/**
	 * Distributes the data to this objects {@link com.strixa.net.SimpleClientSocket.DataReceivedListener}s.
	 */
	private class DataReceivedDistributor implements Runnable{
		private byte[] __data;
		private DataReceivedListener __listener;
		
		
		/*Begin Constructors*/
		/**
		 * Constructs this object.
		 * 
		 * @param listener Listener to be notified of data received.
		 * @param data Data which was received.
		 */
		public DataReceivedDistributor(DataReceivedListener listener,byte[] data){
			this.__data = data;
			this.__listener = listener;
		}
		/*End Constructors*/
		
		/*Begin Other Methods*/
		public void run(){
			this.__listener.onDataReceived(SimpleClientSocket.this,this.__data);
		}
		/*End Other Methods*/
	}
	
	private final ArrayList<DataReceivedListener> __data_received_listeners = new ArrayList<DataReceivedListener>();
	
	private String             __host;
	private ObjectInputStream  __input_stream;
	private Thread             __listener_thread;
	private ObjectOutputStream __output_stream;
	private int                __port;
	private Socket             __socket;
	private boolean            __verbose;
	
	
	/*Begin Constructors*/
	/**
	 * Constructs the client socket.
	 * 
	 * @param host Host you would like to connect to.  This may in the form of a URL or an IP address.
	 * @param port Port to which you would like to connect.
	 */
	public SimpleClientSocket(String host,int port){
		this.__host = host;
		this.__port = port;
		this.__verbose = false;
	}
	/*End Constructors*/
	
	/*Begin Getter/Setter Methods*/
	/**
	 * Retrieves the thread on which the socket is listening for incoming data.
	 * 	
	 * @return Returns the listener thread.  Will be null if the 'connect' method has not yet been called.
	 */
	public Thread getListenerThread(){
		return this.__listener_thread;
	}
	
	/**
	 * Check to determine if this object is connected to the server or not.
	 * 
	 * @return Returns true if the connection has been made, and false, otherwise.
	 */
	public boolean isConnected(){
		if(this.__socket == null || this.__socket.isConnected()){
			return false;
		}
		
		return true;
	}
	
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
	/**
	 * Adds a listener to be notified of data received.
	 * 
	 * @param listener Listener to be notified.
	 */
	public void addDataReceivedListener(DataReceivedListener listener){
		if(listener == null){
			throw new NullPointerException("Argument 'listener' must not be null.");
		}
		
		if(this.__data_received_listeners.contains(listener)){
			if(this.__verbose){
				Log.logEvent(Log.Type.NOTICE,"You have already added the requsted listener to this object's list of DataReceivedListeners.  It will not be added again.");
			}
		}else{
			this.__data_received_listeners.add(listener);
		}
	}
	
	/**
	 * Connects to the host and port set when this object was constructed.
	 * 
	 * @return Returns true if the connection was successful, and false, otherwise.
	 */
	public boolean connect(){
		if(this.__verbose){
			Log.logEvent(Log.Type.NOTICE,"Attempting to connect to host='" + this.__host + "' and port='" + this.__port + "'.");
		}
		
		/*Create the connection*/
		try{
			this.__socket = new Socket(this.__host,this.__port);
		}catch(IOException e){
			if(this.__verbose){
				Log.logEvent(Log.Type.ERROR,"Could not connect to the requested host and port with the specified information.");
			}
			
			return false;
		}catch(SecurityException e){
			if(this.__verbose){
				Log.logEvent(Log.Type.ERROR,"Could not connect to the requested host and port because of a security permissions issue.");
			}
			
			return false;
		}
		
		/*Retrieve the input stream.*/
		try{
			this.__input_stream = new ObjectInputStream(this.__socket.getInputStream());
		}catch(IOException e){
			if(this.__verbose){
				Log.logEvent(Log.Type.WARNING,"The input stream for the socket could not be retrieved.  The user will not be able to recieve incoming data.");
			}
			
			this.__input_stream = null;
		}
		
		/*Retrieve the output stream.*/
		try{
			this.__output_stream = new ObjectOutputStream(this.__socket.getOutputStream());
		}catch(IOException e){
			if(this.__verbose){
				Log.logEvent(Log.Type.WARNING,"The output stream for the socket could not be retrieved.  The user will not be able to send data.");
			}
			
			this.__output_stream = null;
		}
		
		/*Start the listener thread.*/
		if(this.__input_stream != null){
			this.__listener_thread = new Thread(this,"SimpleClientSocket Data Listener");
			this.__listener_thread.start();
		}
		
		return true;
	}
	
	/**
	 * Disconnects from the server.
	 */
	public void disconnect(){
		try{			
			this.__socket.close();
			this.__socket.shutdownInput();
			this.__socket.shutdownOutput();
			this.__socket = null;
			
			this.__listener_thread.interrupt();
			this.__listener_thread = null;
			
			this.__input_stream = null;
			this.__output_stream = null;
		}catch(IOException e){
			Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to disconnect the socket.");
		}
	}
	
	/**
	 * Removes the requested listener, stopping it from receiving future updates of data retrieval.
	 * 
	 * @param listener Listener to be removed.
	 */
	public void removeDataReceivedListener(DataReceivedListener listener){
		if(listener == null){
			throw new NullPointerException("Argument 'listener' must not be null.");
		}
		
		if(!this.__data_received_listeners.contains(listener)){
			if(this.__verbose){
				Log.logEvent(Log.Type.NOTICE,"The requested listener was not in this object's list of DataReceievedListeners.");
			}
		}else{
			this.__data_received_listeners.remove(listener);
		}
	}
	
	public void run(){
		int    available = 0;
		byte[] data = null;
		
		
		while(true){
			/*Find out how much data there is (if any) to be read.*/
			try{
				available = this.__input_stream.available();
			}catch(IOException e){
				if(this.__verbose){
					Log.logEvent(Log.Type.WARNING,"Could not retrieve number of bytes available in the stream.");
				}
				
				available = 0;
			}
			if(available == 0){
				continue;
			}
			
			/*Read the data and distribute it.*/
			data = new byte[available];
			try{
				if(this.__input_stream.read(data) == -1){  //If it returns -1, it means we're at the server closed the connection to the stream.
					if(this.__verbose){
						Log.logEvent(Log.Type.NOTICE,"EoF was reached reading input stream.  Closing socket.");
					}
					
					this.disconnect();
				}
			}catch(IOException e){
				if(this.__verbose){
					Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to read the data from the stream.");
				}
					
				continue;
			}
			
			for(int index = 0,end_index = this.__data_received_listeners.size() - 1;index <= end_index;index++){
				new Thread(new DataReceivedDistributor(this.__data_received_listeners.get(index),data)).start();
			}
		}
	}
	
	/**
	 * Sends the requested data to the server.
	 * 
	 * @param data Data to be sent.
	 * 
	 * @return Returns true if the data could be sent, and false, otherwise.
	 */
	public boolean send(byte[] data){
		if(data == null){
			throw new NullPointerException("Argument 'data' must not be null.");
		}else if(data.length == 0){
			if(this.__verbose){
				Log.logEvent(Log.Type.NOTICE,"Attempted to send a byte array with no data.");
			}
			
			return false;
		}
		
		try{
			this.__output_stream.write(data);
		}catch(IOException e){
			if(this.__verbose){
				Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to send the data.");
			}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends the requested data to the server.<br />
	 * <strong>Note:</strong>  The data must implement the {@link java.io.Serializable} interface to be sent via this method.
	 * 
	 * @param data Data to be sent.
	 * 
	 * @return Returns true if the data could be sent, and false, otherwise.
	 */
	public boolean send(Object data){
		ByteArrayOutputStream ba_stream = null;
		ObjectOutputStream o_stream = null;
		
		
		if(data == null){
			throw new NullPointerException("Argument 'data' must not be null.");
		}else if(data instanceof Serializable){
			if(this.__verbose){
				Log.logEvent(Log.Type.WARNING,"Data intended to be sent was not marked as serializable and will not be sent.");
			}
			
			return false;
		}
		
		ba_stream = new ByteArrayOutputStream();
		try{
			o_stream = new ObjectOutputStream(ba_stream);
			o_stream.writeObject(data);
		}catch(IOException e){
			if(this.__verbose){
				Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to serialize the requested data.");
			}
			
			return false;
		}
		return this.send(ba_stream.toByteArray());		
	}
	/*End Other Methods*/
}
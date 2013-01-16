package com.strixa.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.strixa.util.Log;

/**
 * Simplifies the use of sockets by automating some of the concepts of socket creation such as listing for and sending data. 
 */
public class SimpleClientSocket{
    /*Begin Data Control Keys*/
    /**Indicates that the data contained in the message is a piece of data.  This will always have additional data.*/
    protected static final byte _DATA = 0x0;
    /*End Data Control Keys*/
    
    /*Begin State Flags*/
    /**Indicates a state in which the object is waiting for */
    public static final int AWAITING_SERVER_CONNECT_FLAG = 0x1;
    /*End State Flags*/
    
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
		public void onDataReceived(SimpleClientSocket socket,Object data);
	}
	
	/**
	 * Distributes the data to this objects {@link com.strixa.net.SimpleClientSocket.DataReceivedListener}s.
	 */
	private class DataReceivedDistributor implements Runnable{
		private Object __data;
		private DataReceivedListener __listener;
		
		
		/*Begin Constructors*/
		/**
		 * Constructs this object.
		 * 
		 * @param listener Listener to be notified of data received.
		 * @param data Data which was received.
		 */
		public DataReceivedDistributor(DataReceivedListener listener,Object data){
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
	
	private class MessageReceiver implements Runnable{
	    /**
	     * Created because of the need for a non-blocking stream check.
	     */
	    private class MessageScanner implements Runnable{
	        private boolean __run;
	        
	        
	        /*Begin Other Methods*/
	        public void run(){
	            this.__run = true;
	            while(this.__run){
	                
	            }
	        }
	    }
	    
	    private final  ArrayList<Object> __message_buffer = new ArrayList<Object>();
	    
	    private boolean __run;
	    
	    
	    /*Begin Other Methods*/	    
	    public void run(){
	        Object data = null;
	        
	        
	        this.__run = true;
	        while(this.__run){	            
	            /*Read the data and distribute it.*/                                
                try{
                    data = SimpleClientSocket.this.__input_stream.readObject();
	            }catch(SocketTimeoutException e){
	                if(SimpleClientSocket.this.__socket.isClosed()){
                        Log.logEvent(Log.Type.NOTICE,"The socket was closed.");
                        
                        this.stop();
                    }
	                
	                continue;
	            }catch(SocketException e){
	                if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.NOTICE,"Disconnecting from the server because of a SocketException.  Reason:  " + e.getMessage());
	                }
	                
	                SimpleClientSocket.this.disconnect();
	                
	                continue;
	            }catch(EOFException e){
	                if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.NOTICE,"Disconnecting from the server because of the end of the stream was reached.");
	                }
	                
	                SimpleClientSocket.this.disconnect();
	                
	                continue;
	            }catch(IOException e){              
	                if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to read the data from the stream.");
	                }
	                    
	                continue;
	            }catch(ClassNotFoundException e){
	                if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.WARNING,"A ClassNotFoundException occured while attempting to read the data from the stream.");
	                }
	                    
	                continue;
	            }
                
                /*Describe*/            
                if(data instanceof Message){
                    SimpleClientSocket.this._interpret((Message)data);
                }else{
                    if(SimpleClientSocket.this.__verbose){
                        Log.logEvent(Log.Type.NOTICE,"Data received that was not a message.  Discarding.");
                    }
                }
	        }
	    }
	    
	    public void stop(){
	        this.__run = false;
	    }
	}
	
	private class SendQueueProcessor implements Runnable{
	    private final ArrayList<Message>  __data_queue = new ArrayList<Message>();
	    
	    private boolean             __run;
	    	    
	    
	    /*Begin Getter/Setter Methods*/
	    /**
	     * Determines if the SendQueueProcessor has data that hasn't been processed yet.
	     * 
	     * @return Returns true if the SendQueueProcessor has data that hasn't been processed yet, and false, otherwise.
	     */
	    public boolean hasQueuedData(){
	        return !this.__data_queue.isEmpty();
	    }
	    /*End Getter/Setter Methods*/
	    
	    /*Begin Other Methods*/
	    /**
	     * Pushes the requested information on the the Send Queue Processor to be readied for transmission.
	     * 
	     * @param data_code Data code for what is being sent.
	     * @param data Data which is being sent.  This may be null if no message is required.
	     * @param to_front If this is an important message, this should be true to indicate that the data should be processed before anything else.
	     */
	    public void push(byte data_code,Object data,boolean to_front){
	        final Message message = new Message(data_code,data);
	        
	        
	        if(to_front){
	            this.__data_queue.add(0,message);
	        }else{
	            this.__data_queue.add(message);
	        }
	    }
	    
	    public void run(){
	        this.__run = true;
            while(this.__run){                
                if(this.__data_queue.isEmpty()){
                    try{
                        Thread.sleep(50);
                    }catch(InterruptedException e){
                        //TODO:  Nothing
                    }
                    
                    continue;
                }
                
                try{
                    SimpleClientSocket.this.__output_stream.writeObject(this.__data_queue.get(0));
                    SimpleClientSocket.this.__output_stream.flush();
                }catch(IOException e){
                    if(SimpleClientSocket.this.__verbose){
                        Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to send the data.");
                    }
                }
                this.__data_queue.remove(0);
            }
        }
	    
	    public void stop(){
	        this.__run = false;
	    }
	    /*End Other Methods*/
	}
	
	private final ArrayList<DataReceivedListener> __data_received_listeners = new ArrayList<DataReceivedListener>();
	private final MessageReceiver                 __message_receiver = new MessageReceiver();
	private final Thread                          __message_receiver_thread = new Thread(this.__message_receiver,"SimpleClientSocket MessageReceiver Thread");
	private final SendQueueProcessor              __send_queue_processor = new SendQueueProcessor();
	private final Thread                          __send_queue_processor_thread = new Thread(this.__send_queue_processor,"SimpleClientSocket SendQueueProcessor Thread");
	
	private InetAddress        __address;
	private int                __control;
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
	public SimpleClientSocket(String host,int port) throws UnknownHostException{
	    this(InetAddress.getByName(host),port);
	}
	
	public SimpleClientSocket(InetAddress address,int port){
	    this.__address = address;
	    
	    this.__control = SimpleClientSocket.AWAITING_SERVER_CONNECT_FLAG;
        this.__port = port;
        this.__verbose = false;
	}
	
	/**
	 * Constructs the client socket.
	 * 
	 * @param socket Should be an already initialized and connected socket.
	 */
	public SimpleClientSocket(Socket socket){
	    this.__verbose = false;
	    this._connect(socket);
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
	
	public Socket getRawSocket(){
	    return this.__socket;
	}
	
	protected boolean _hasKey(int key){
	    return (this.__control & key) == key;
	}
	
	/**
	 * Check to determine if this object is connected to the server or not.
	 * 
	 * @return Returns true if the connection has been made, and false, otherwise.
	 */
	public boolean isConnected(){
		if(this._hasKey(AWAITING_SERVER_CONNECT_FLAG)){
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
	    Socket socket = null;
	    
	    
		if(this.__verbose){
			Log.logEvent(Log.Type.NOTICE,"Attempting to connect to host='" + this.__address.getHostAddress() + "' and port='" + this.__port + "'.");
		}
		
		/*Create the connection*/
		try{
			socket = new Socket(this.__address,this.__port);
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
		
		return this._connect(socket);
	}
	
	protected boolean _connect(Socket socket){
	    try{
	        socket.setSoTimeout(1000);
	    }catch(IOException e){
	        if(this.__verbose){
                Log.logEvent(Log.Type.WARNING,"Could not set the stream timeout.");
            }
	    }
	    
	    
        /*Retrieve the output stream.*/
        try{
            this.__output_stream = new ObjectOutputStream(socket.getOutputStream());
        }catch(IOException e){
            if(this.__verbose){
                Log.logEvent(Log.Type.WARNING,"The output stream for the socket could not be retrieved.  The user will not be able to send data.");
            }
            
            this.__output_stream = null;
        }
        
        if(this.__output_stream != null){
            this.__send_queue_processor_thread.start();
        }
        
        /*Retrieve the input stream.*/
        try{
            this.__input_stream = new ObjectInputStream(socket.getInputStream());
        }catch(IOException e){
            if(this.__verbose){
                Log.logEvent(Log.Type.WARNING,"The input stream for the socket could not be retrieved.  The user will not be able to recieve incoming data.");
            }
            
            this.__input_stream = null;
        }
        
        /*Start the listener thread.*/
        if(this.__input_stream != null){
            this.__message_receiver_thread.start();
        }
        
        
        if(this.__verbose){
            Log.logEvent(Log.Type.NOTICE,"The client has successfully connected to the server.");
        }
                
        
        this.__socket = socket;
        
        return true;
	}
	
	/**
	 * Disconnects from the server.
	 */
	public void disconnect(){
	    if(this.__verbose){
	        Log.logEvent(Log.Type.NOTICE,"Disconnecting from server.");
	    }
	    
		try{						
			this.__message_receiver.stop();
			
			while(this.__send_queue_processor.hasQueuedData()){
		        try{
		            Thread.sleep(50);
		        }catch(InterruptedException e){
		            //TODO:  Nothing!
		        }
			}
			this.__send_queue_processor.stop();
			
			if(!this.__socket.isInputShutdown()){
			    this.__socket.shutdownInput();
			}
            if(!this.__socket.isOutputShutdown()){
                this.__socket.shutdownOutput();
            }
            if(!this.__socket.isClosed()){
                this.__socket.close();
            }
            this.__socket = null;
		}catch(IOException e){
			Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to disconnect the socket.");
		}
	}
	
	protected void _interpret(Message message){
	    switch(message.getMessageCode()){
	        case _DATA:
	            for(int index = 0,end_index = this.__data_received_listeners.size() - 1;index <= end_index;index++){
	                new Thread(new DataReceivedDistributor(this.__data_received_listeners.get(index),message.getMessage())).start();
	            }
	            
	            break;
	    }
	}
	
	/**
	 * Pushes data to the SendQueueProcessor to be sent.  Data sent using this method automatically assumes it is of data_code type {@link com.strixa.net.SimpleClientSocket#_DATA} with teh lowest priority.
	 * 
	 * @param data Data to be sent.
	 */
	protected void _pushDataToQueue(Object data){
	    this._pushDataToQueue(_DATA,data,true);
	}
	
	/**
     * Pushes data to the SendQueueProcessor to be sent.  Data sent using this method automatically assumes it is of data_code type {@link com.strixa.net.SimpleClientSocket#_DATA} with teh lowest priority.
     * 
     * @param data Data to be sent.
     */
    protected void _pushDataToQueue(byte data_code,Object data,boolean to_front){
        this.__send_queue_processor.push(data_code,data,to_front);
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
	
	/**
	 * Sends the requested data to the server.
	 * 
	 * @param data Data to be sent.
	 */
	public void send(Object data){
	    if(data == null){
            throw new NullPointerException("Argument 'data' must not be null.");
        }
	    
	    this._pushDataToQueue(data);
	}
	/*End Other Methods*/
}
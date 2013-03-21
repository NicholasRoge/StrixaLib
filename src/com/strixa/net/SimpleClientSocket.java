package com.strixa.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;

import com.strixa.util.Log;

/**
 * Simplifies the use of sockets by automating some of the concepts of socket creation such as listing for and sending data. 
 */
public class SimpleClientSocket{
	/**
	 * Contains keys that should be used in sending and receiving messages.
	 */
	protected static final class MessageKeys{
	    /**Indicates that the data contained in the message is a piece of data.  This will always have additional data.*/
	    public static final byte DATA = 0x0;
	    /**Indicates that the connection is being closed.  This will never have additional data.*/
	    public static final byte DISCONNECT_NOTICE = 0x1;
	}
    
	/**
	 * Contains any flag which will be used to manipulate this object.
	 */
    protected static final class ControlFlags{
	    /**Indicates a state in which the SimpleClientSocket is waiting to connect to the server.*/
	    public static final int AWAITING_SERVER_CONNECT_FLAG = 0x1;
	    /**If this flag is set, it requires that the SimpleClientSocket send a notice that they're disconnecting. */ 
	    public static final int DISCONNECT_NOTICE_FLAG = 0x2;
	    /**Indicates that the connection closed badly (was not requested by the client or server) and should attempt to be revived if requested.*/
	    public static final int BAD_DISONNECT = 0x4;
    }
    
	/**
	 * Notifies the listener of data received by a {@link com.strixa.net.SimpleClientSocket} object.
	 */
	public interface ClientEventListener{
		/**
		 * Called when data is received by the {@link com.strixa.net.SimpleClientSocket}. 
		 * 
		 * @param socket {@link com.strixa.net.SimpleClientSocket} which received the data.
		 * @param data Data which was received.
		 */
		public void onDataReceived(SimpleClientSocket socket,Object data);
		
		/**
		 * Called when the client disconnects from the server.
		 * 
		 * @param socket Socket which is disconnecting.
		 * 
		 * @return Implemented classes should return true if they would like the socket to attempt a reconnect on a bad disconnect (not requested by the client or server), and false, otherwise.
		 */
		public boolean onDisconnect(SimpleClientSocket socket);
	}
	
	/**
	 * Distributes the data to this objects {@link com.strixa.net.SimpleClientSocket.ClientEventListener}s.
	 */
	private class DataReceivedDistributor implements Runnable{
		private Object __data;
		private ClientEventListener __listener;
		
		
		/*Begin Constructors*/
		/**
		 * Constructs this object.
		 * 
		 * @param listener Listener to be notified of data received.
		 * @param data Data which was received.
		 */
		public DataReceivedDistributor(ClientEventListener listener,Object data){
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
	
	/**
	 * Listens to the input stream for messages to be read.
	 */
	private class MessageReceiver implements Runnable{
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
	                if(!SimpleClientSocket.this.isConnected()){
                        Log.logEvent(Log.Type.NOTICE,"The socket was closed.");
                        
                        this.stop();
                    }
	                
	                continue;
	            }catch(SocketException e){
                    if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.NOTICE,"Disconnecting from the server because of a SocketException.  Reason:  " + e.getMessage());
	                }
	                
	                SimpleClientSocket.this._setFlag(ControlFlags.BAD_DISONNECT,true);
	                SimpleClientSocket.this.disconnect();
	                
	                continue;
	            }catch(EOFException e){
	                if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.NOTICE,"Disconnecting from the server because of the end of the stream was reached.");
	                }
	                
	                SimpleClientSocket.this._setFlag(ControlFlags.BAD_DISONNECT,true);
                    SimpleClientSocket.this.disconnect();
	                
	                continue;
	            }catch(IOException e){
	                if(SimpleClientSocket.this.__verbose){
	                    Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to read the data from the stream.  Message:  " + e.getMessage());
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
	    
	    /**
	     * Causes the MessageReceiver to stop listening for new messages.
	     */
	    public void stop(){
	        this.__run = false;
	    }
	}
	
	/**
	 * Processes the data that was pushed to this object, allowing for fully asynchronous sending of data.
	 */
	private class SendQueueProcessor implements Runnable{
	    private final Vector<Message>  __data_queue = new Vector<Message>();
	    
	    private boolean __run;
	    	    
	    
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
	        Message message = null;
	        
	        
	        try{
	        	message = new Message(data_code,data);
	        }catch(NotSerializableException e){
	        	if(SimpleClientSocket.this.__verbose){
	        		Log.logEvent(Log.Type.WARNING,"Message not sent.  Any data which you would like to send must be serializable.");
	        	}
	        	
	        	return;
	        }
	        
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
	    
	    /**
	     * Stops the SimpleClientSocket from sending any more messages.
	     */
	    public void stop(){
	        this.__run = false;
	    }
	    /*End Other Methods*/
	}
	
	private final ArrayList<ClientEventListener> __client_event_listeners = new ArrayList<ClientEventListener>();
	private final MessageReceiver                __message_receiver = new MessageReceiver();
	private final Thread                         __message_receiver_thread = new Thread(this.__message_receiver,"SimpleClientSocket MessageReceiver Thread");
	private final SendQueueProcessor             __send_queue_processor = new SendQueueProcessor();
	private final Thread                         __send_queue_processor_thread = new Thread(this.__send_queue_processor,"SimpleClientSocket SendQueueProcessor Thread");
	
	private InetAddress        __address;
	private int                __control_flags;
	private ObjectInputStream  __input_stream;
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
	 * 
	 * @throws UnkownHostException Thrown if the requested host cannot be found.
	 */
	public SimpleClientSocket(String host,int port) throws UnknownHostException{
	    this(InetAddress.getByName(host),port);
	}
	
	/**
	 * Constructs the client socket.
	 * 
	 * @param address Initialized {@link java.net.InetAddress} object containing the information for the SimpleServerSocket you would like to connect to.
	 * @param port Port to which you would like to connect.
	 */
	public SimpleClientSocket(InetAddress address,int port){
	    this.__address = address;
	    
	    this.__control_flags = ControlFlags.DISCONNECT_NOTICE_FLAG | ControlFlags.AWAITING_SERVER_CONNECT_FLAG;
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
	 * Gets the raw socket object through which this object is connected.
	 * 
	 * @return The raw socket object through which this object is connected.
	 */
	public Socket getRawSocket(){
	    return this.__socket;
	}
	
	/**
	 * Gets the value for the requested flag.
	 * 
	 * @param flag Flag whose value is in question.  This should be one of the constants defined in {@link com.strixa.net.SimpleClientSocket.ControlFlags}.
	 * 
	 * @return Returns true, if this flag is set, and false, otherwise.
	 */
	protected boolean _getFlag(int flag){
	    return (this.__control_flags & flag) == flag;
	}
	
	/**
	 * Check to determine if this object is connected to the server or not.
	 * 
	 * @return Returns true if the connection has been made, and false, otherwise.
	 */
	public boolean isConnected(){
		if(this._getFlag(ControlFlags.AWAITING_SERVER_CONNECT_FLAG)){
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
	
	/**
	 * Sets the value for the requested flag.
	 * 
	 * @param flag Flag to check.  This should be one of the constants defined in {@link com.strixa.net.SimpleClientSocket.ControlFlags}.
	 * @param set Should be set to true, if the flag should be set, and false, otherwise.
	 */
	protected void _setFlag(int flag,boolean set){
		if(this._getFlag(flag) != set){
			this.__control_flags = this.__control_flags ^ flag;
		}
	}
	/*End Getter/Setter Methods*/
	
	/*Begin Other Methods*/
	/**
	 * Adds a listener to be notified of any client events.
	 * 
	 * @param listener Listener to be notified.
	 */
	public void addClientEventListener(ClientEventListener listener){
		if(listener == null){
			throw new NullPointerException("Argument 'listener' must not be null.");
		}
		
		if(this.__client_event_listeners.contains(listener)){
			if(this.__verbose){
				Log.logEvent(Log.Type.NOTICE,"You have already added the requsted listener to this object's list of DataReceivedListeners.  It will not be added again.");
			}
		}else{
			this.__client_event_listeners.add(listener);
		}
	}
	
	/**
	 * Broadcasts a disconnect to all listeners.
	 * 
	 * @return Returns true if the client should attempt a reconnect on bad (not requested by client or server) disconnect.
	 */
	private boolean __broadcastDisconnect(){
		boolean reconnect = false;
		
		
		for(int index = 0,end_index = this.__client_event_listeners.size() - 1;index <= end_index;index++){
			if(this.__client_event_listeners.get(index).onDisconnect(this)){
				reconnect = true;
			}
		}
		
		return reconnect;
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
        this._setFlag(SimpleClientSocket.ControlFlags.AWAITING_SERVER_CONNECT_FLAG,false);
        
        return true;
	}
	
	/**
	 * Disconnects from the server.
	 */
	public void disconnect(){
		byte attempt = 1;
		
		
	    if(this.__verbose){
	        Log.logEvent(Log.Type.NOTICE,"Disconnecting from server.");
	    }
	    
		try{
			if(this._getFlag(ControlFlags.DISCONNECT_NOTICE_FLAG)){
				this._pushDataToQueue(MessageKeys.DISCONNECT_NOTICE, null,false);
			}
			
			this.__message_receiver.stop();
			if(!Thread.currentThread().equals(this.__message_receiver_thread)){
			    while(this.__message_receiver_thread.isAlive()){
	                try{
	                    Thread.sleep(50);
	                }catch(InterruptedException e){
	                    //TODO:  Nothing!
	                }
	            }
			}
			
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
		}catch(IOException e){
			Log.logEvent(Log.Type.WARNING,"An IOException occured while attempting to disconnect the socket.");
		}
		
		if(this.__broadcastDisconnect() && this._getFlag(ControlFlags.BAD_DISONNECT)){
			while(!this.connect()){
				if(attempt > 3){
					Log.logEvent(Log.Type.NOTICE,"Maximum reconnect attempts reached.  Failed to reconnect to server.");
				}else{
					attempt++;
				}
				
				try{
					Thread.sleep(500);
				}catch(InterruptedException e){
					//TODO:  Nothing!
				}
			}
		}
	}
	
	protected void _interpret(Message message){
	    switch(message.getMessageCode()){
	        case MessageKeys.DATA:
	            for(int index = 0,end_index = this.__client_event_listeners.size() - 1;index <= end_index;index++){
	                new Thread(new DataReceivedDistributor(this.__client_event_listeners.get(index),message.getMessage())).start();
	            }
	            
	            break;
	        case MessageKeys.DISCONNECT_NOTICE:
	        	this._setFlag(ControlFlags.DISCONNECT_NOTICE_FLAG,false);
	        	this.disconnect();
	        	
	        	break;
	    }
	}
	
	/**
	 * Pushes data to the SendQueueProcessor to be sent.  Data sent using this method automatically assumes it is of data_code type {@link com.strixa.net.SimpleClientSocket#DATA} with teh lowest priority.
	 * 
	 * @param data Data to be sent.
	 */
	protected void _pushDataToQueue(Object data){
	    this._pushDataToQueue(MessageKeys.DATA,data,true);
	}
	
	/**
     * Pushes data to the SendQueueProcessor to be sent.  Data sent using this method automatically assumes it is of data_code type {@link com.strixa.net.SimpleClientSocket#DATA} with teh lowest priority.
     * 
     * @param data Data to be sent.
     */
    protected void _pushDataToQueue(byte data_code,Object data,boolean to_front){
        this.__send_queue_processor.push(data_code,data,to_front);
    }
	
	/**
	 * Removes the requested listener, stopping it from receiving future client events.
	 * 
	 * @param listener Listener to be removed.
	 */
	public void removeClientEventListener(ClientEventListener listener){
		if(listener == null){
			throw new NullPointerException("Argument 'listener' must not be null.");
		}
		
		if(!this.__client_event_listeners.contains(listener)){
			if(this.__verbose){
				Log.logEvent(Log.Type.NOTICE,"The requested listener was not in this object's list of DataReceievedListeners.");
			}
		}else{
			this.__client_event_listeners.remove(listener);
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
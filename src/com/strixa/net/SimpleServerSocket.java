package com.strixa.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.strixa.net.SimpleClientSocket.DataReceivedListener;
import com.strixa.util.Log;

public class SimpleServerSocket implements Runnable{
	public interface ClientConnectListener{
	    /**
	     * Called when a new client attempts to connect to the server.
	     * 
	     * @param client Client who wishes to connect.
	     * 
	     * @return Implementing classes should return true if the client should be accepted and allowed to communicate with the server, and false, otherwise.
	     */
		public boolean onClientConnect(SimpleClientSocket client);
	}
	
	private ArrayList<ClientConnectListener> __client_connect_listeners = new ArrayList<ClientConnectListener>();
	private ArrayList<SimpleClientSocket>    __clients = new ArrayList<SimpleClientSocket>();
	
	private Thread       __client_listener;
	private int          __port;
	private ServerSocket __socket;
	private boolean      __verbose;
	
	
	/*Begin Constructors*/
	/**
	 * Constructs the server socket.
	 * 
	 * @param port Port to start the server on.
	 */
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
	
	
	public void run(){
        SimpleClientSocket client = null;
        Socket             socket = null;
        
        
        while(true){
            if(this.__verbose){
                Log.logEvent(Log.Type.NOTICE,"Listening for incoming connection...");
            }
            
            try{
                socket = this.__socket.accept();
            }catch(IOException e){
                if(this.__verbose){
                    Log.logEvent(Log.Type.WARNING,"Could not accept requested client because of an IOException.");
                }
                
                continue;
            }
            
            client = new SimpleClientSocket(socket);client.send(new byte[]{0xD,0xE,0xA,0xD,0xB,0xE,0xE,0xF});
            this.__clients.add(client);
            for(int index = 0,end_index = this.__client_connect_listeners.size();index <= end_index;index++){
                if(!this.__client_connect_listeners.get(index).onClientConnect(client)){
                    client.disconnect();
                    this.__clients.remove(client);
                    
                    break;
                }
            }
        }
    }
	
	/**
	 * Starts the server running on the port requested upon this object's construction.
	 * 
	 * @return Returns true on a successful start, and false otherwise.
	 */
	public boolean start(){
		if(this.__verbose){
		    Log.logEvent(Log.Type.NOTICE,"Starting server on port='" + this.__port + "'");
		}
		
		try{
		    this.__socket = new ServerSocket(this.__port);
		}catch(IOException e){
		    Log.logEvent(Log.Type.WARNING,"Could not start server on the requested port.");
		    
		    return false;
		}catch(SecurityException e){
		    Log.logEvent(Log.Type.WARNING,"Could not start server because of a security exception.");
		    
		    return false;
		}
		
		this.__client_listener = new Thread(this,"SimpleServerClient Client Listener");
		this.__client_listener.start();
		
		if(this.__verbose){
		    Log.logEvent(Log.Type.NOTICE,"Server started successfully.");
		}
		
		
		return true;
	}
	
	/**
	 * Stops the server and sends a disconnect notice to all connected clients.
	 */
	public void stop(){
	    if(this.__verbose){
	        Log.logEvent(Log.Type.NOTICE,"Stopping server.");
	    }
	    
	    for(int index = 0,end_index = this.__clients.size();index <= end_index;index++){
	        this.__clients.get(index).disconnect();
	    }
	    this.__clients.clear();
	    
	    this.__client_listener.interrupt();
	    this.__client_listener = null;
	    
	    this.__socket = null; 
	    
	    if(this.__verbose){
            Log.logEvent(Log.Type.NOTICE,"Server stopped successfully.");
        }
	}
	/*End Other Methods*/
}

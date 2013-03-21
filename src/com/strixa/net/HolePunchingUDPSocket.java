/**
 * File:  HolePunchingUDPSocket.java
 * Date of Creation:  Feb 17, 2013
 */
package com.strixa.net;

import com.strixa.net.SimpleUDPSocket.UDPDataReceivedListener;
import com.strixa.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;


/**
 * Provides an implementation of the SimpleUDPSocket which allows the client to connect to another client who is behind a NAT firewall.
 */
public class HolePunchingUDPSocket extends SimpleUDPSocket implements UDPDataReceivedListener{
    /**
     * Class which represents any aspects of a client required for creating a connection between two clients.
     */
    public static class Client implements Serializable{
        private static final long            serialVersionUID = -5380851407445209762L;
        
        private InetAddress __address;
        private long        __description;
        private int         __port;
        
        
        /*Begin Constructors*/
        /**
         * Creates an 
         * 
         * @param host
         * @param port
         * @throws UnknownHostException
         */
        public Client(String host,int port,long description) throws UnknownHostException{
            this(InetAddress.getByName(host),port,description);
        }
        
        public Client(InetAddress address,int port,long description){
            final Random random = new Random();
            
            
            this.__address = address;
            this.__description = description;
            this.__port = port;
        }
        /*End Constructors*/
        
        /*Begin Getter/Setter Methods*/
        public InetAddress getAddress(){
            return this.__address;
        }
        
        public long getDescription(){
            return this.__description;
        }
        
        public int getPort(){
            return this.__port;
        }
        /*End Getter/Setter Methods*/
    }
    
    public static enum ClientStatus{
        AWAITING_CONNECTION,
        CONNECTION_ERROR,
        CONNECTED,
        DISCONNECTED;
    }
    
    public interface ClientStateChangeListener{
        public void onClientStateChange(Client client,ClientStatus status);
    }
    
    private class HolePuncher implements Runnable{
        public static final int PHASE_SEND_KEEP_ALIVE = 2;
        public static final int PHASE_SEND_REQUEST = 0;
        public static final int PHASE_SEND_REQUEST_RESPONSE = 1;
        public static final int PHASE_STOPPED = -1;
        
        private int     __attempt;
        private Client  __client;
        private byte[]  __data;
        private boolean __paused;
        private int     __phase;
        private boolean __run;
        private Thread  __run_thread;
        
        
        /*Begin Constructors*/
        public HolePuncher(Client client){
            ByteBuffer buffer;
            
            
            this.__client = client;
            this.__phase = HolePuncher.PHASE_STOPPED;
            
            this.__data = new byte[(Long.SIZE / 8)];
            buffer = ByteBuffer.wrap(this.__data);
            buffer.putLong(HolePunchingUDPSocket.this.getDescription());
        }
        /*End Constructors*/
        
        /*Begin Other Methods*/
        public void run(){     
            HolePunchingUDPSocket socket = HolePunchingUDPSocket.this;
            
            
            this.__run_thread = Thread.currentThread();
            this.setPhase(HolePuncher.PHASE_SEND_REQUEST);
            
            this.__paused = false;
            this.__run = true;
            while(this.__run){
                try{
                    switch(this.__phase){
                        case HolePuncher.PHASE_SEND_REQUEST_RESPONSE:
                            if(this.__attempt > 5){  //This phase is allowed to broadcast for 60 seconds.
                                HolePunchingUDPSocket.this._onHolePunchFailure(this.__client,this.__phase);
                                
                                return;
                            }
                            
                            socket.send(HolePunchingUDPSocket.CONTACT_RESPONSE,this.__data,this.__client);
                            
                            Thread.sleep(200);
                            break;
                        case HolePuncher.PHASE_SEND_REQUEST:
                            if(this.__attempt > 120){  //This phase is allowed to broadcast for 60 seconds.
                                HolePunchingUDPSocket.this._onHolePunchFailure(this.__client,this.__phase);
                                
                                return;
                            }
                            
                            socket.send(HolePunchingUDPSocket.CONTACT_RESPONSE,this.__data,this.__client);
                            socket.send(HolePunchingUDPSocket.CONTACT_REQUEST,this.__data,this.__client);
    
                            Thread.sleep(500);
                            break;
                        case HolePuncher.PHASE_SEND_KEEP_ALIVE:                        
                            if(this.__attempt > 5){
                                HolePunchingUDPSocket.this._onHolePunchFailure(this.__client,this.__phase);
                                
                                return;
                            }
    
                            socket.send(HolePunchingUDPSocket.KEEP_ALIVE,this.__data,this.__client);
                            
                            Thread.sleep(1000);
                            break;
                    }
                    this.__attempt++;
                        
                    
                    while(this.__paused){  //This will occur when a phase change is occurring
                        Thread.sleep(50);
                    }
                }catch(InterruptedException e){
                    //TODO:  Nothing!  
                }
            }
            
            this.__run_thread = null;
        }
        
        synchronized public void resetAttempts(){
            this.__attempt = 0;
        }
        
        synchronized public void setPhase(int phase){           
            this.__paused = true;
            
            if(phase <0 || phase > 2){
                throw new IllegalArgumentException("Invalid mode identifier.");
            }
            
            if(phase == this.__phase){
                return;
            }else{
                this.__phase = phase;
            }
            
            this.__attempt = 0;
            
            this.__paused = false;
        }
        
        private int getPhase(){
            return this.__phase;
        }
        
        public void stop(){
            this.__run = false;
        }
        /*End Other Methods*/
    }
    
    private static final int CONTACT_REQUEST = 1;
    private static final int CONTACT_RESPONSE = 2;
    private static final int KEEP_ALIVE = 3;
    private static final int KEEP_ALIVE_ACK = 4;
    
    private static final ArrayList<Long> __used_keys = new ArrayList<Long>();
        
    private final ArrayList<Client>                         __clients = new ArrayList<Client>();
    private final HashMap<Long,Client>                      __client_by_description = new HashMap<Long,Client>();
    private final HashMap<Client,ClientStateChangeListener> __client_state_change_listener_by_client = new HashMap<Client,ClientStateChangeListener>();
    private final HashMap<Client,ClientStatus>              __client_status_by_client = new HashMap<Client,ClientStatus>();
    private final HashMap<Client,HolePuncher>               __hole_puncher_by_client = new HashMap<Client,HolePuncher>();
    
    private long __description;
    
    
    /*Begin Constructors*/
    public HolePunchingUDPSocket(){
        super();
        
        final Random random = new Random();
        
        
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_REQUEST,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_RESPONSE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE_ACK,this);
        
        do{
            this.__description = random.nextLong();
        }while(HolePunchingUDPSocket.__used_keys.contains(this.__description));
        HolePunchingUDPSocket.__used_keys.add(this.__description);
    }
    
    public HolePunchingUDPSocket(int port){
        super(port);
        
        final Random random = new Random();
        
        
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_REQUEST,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_RESPONSE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE_ACK,this);
        
        do{
            this.__description = random.nextLong();
        }while(HolePunchingUDPSocket.__used_keys.contains(this.__description));
        HolePunchingUDPSocket.__used_keys.add(this.__description);
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    public ArrayList<Client> getClients(){
        return new ArrayList<Client>(this.__clients);
    }
    
    protected ArrayList<Client> _getClients(){
        return this.__clients;
    }
    
    public ClientStatus getClientStatus(Client client){
        return this.__client_status_by_client.get(client);
    }
    
    public long getDescription(){
        return this.__description;
    }
    
    protected void _setClientStatus(Client client,ClientStatus status){
        final ClientStateChangeListener listener = this.__client_state_change_listener_by_client.get(client);
        
        this.__client_status_by_client.put(client,status);
        
        if(listener != null){
            listener.onClientStateChange(client,status);
        }
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    public void addClient(Client client){
        this.addClient(client,true,null);
    }
    
    /**
     * Adds the specified client to the list of clients who have a connection with this socket.  If the client has already been added, but failed to connect, this method will attempt to restart the client's connection.
     * 
     * @param client Client to add.
     * @param blocking_mode This method will block while the client's connection is being confirmed if this argument is set to true.
     * @param listener The ClientStateChangeListener that should be notified when the client whose connection we're trying to create changes.
     *   
     * @return Return false indicating a connection failure in the following situations:
     * <ul>
     *     <li>The Client has already been added and does not have its status set to {@link ClientStatus#CONNECTION_ERROR}.</li>
     *     <li>If an error occurred while attempting to connect to the client.</li>
     *     <li>Or argument 'blocking_mode was set to false, as this method cannot reliably determine whether a connection was formed or not.</li>
     * </ul>
     * otherwise this method will return true.
     */
    public boolean addClient(Client client,boolean blocking_mode,ClientStateChangeListener listener){
        HolePuncher hole_puncher;
        ClientStatus status;
        
        
        if(!this.__clients.contains(client)){
            this.__clients.add(client);
            
            hole_puncher = new HolePuncher(client);
        }else if(this.getClientStatus(client) == ClientStatus.CONNECTION_ERROR){
            hole_puncher = this.__hole_puncher_by_client.get(client);
        }else{
            return false;
        }
        
        this.__client_by_description.put(client.getDescription(),client);
        this.__hole_puncher_by_client.put(client,hole_puncher);
        this.__client_state_change_listener_by_client.put(client,listener);
        this._setClientStatus(client,ClientStatus.AWAITING_CONNECTION);
        new Thread(hole_puncher,"HolePuncher for " + client.getDescription()).start();
        
        if(blocking_mode){
            while(this.getClientStatus(client) == ClientStatus.AWAITING_CONNECTION){
                try{
                    Thread.sleep(50);
                }catch(InterruptedException e){
                    //TODO:  Nothing
                }
            }
            
            status = this.getClientStatus(client);
            if(status == ClientStatus.CONNECTION_ERROR){
                Log.n("There was an error connecting to the requested client.");
                
                return false;
            }else{
                return true;
            }
        }
        
        return false;
    }
    
    private long __generateClientKey(){
        final Random random = new Random();
        
        long key;
        
        
        do{
            key = random.nextLong();
        }while(this.__client_by_description.containsKey(key));
        
        return key;
    }
    
    @Override public void onUDPDataReceived(int type_id,byte[] data,InetAddress source_address,int source_port){
        ByteBuffer  buffer;
        Client      client;
        HolePuncher hole_puncher;
        
        
        switch(type_id){
            case HolePunchingUDPSocket.CONTACT_REQUEST:
            case HolePunchingUDPSocket.CONTACT_RESPONSE:
            case HolePunchingUDPSocket.KEEP_ALIVE:
            case HolePunchingUDPSocket.KEEP_ALIVE_ACK:
                buffer = ByteBuffer.wrap(data);
                long test = buffer.getLong();
                client = this.__client_by_description.get(test);
                if(client == null){
                    Log.n("An unknown client attempted to send data to this socket.");
                    
                    return;
                }
                
                hole_puncher = this.__hole_puncher_by_client.get(client);
                if(hole_puncher == null){
                    Log.w("Could not find the hole puncher for the requested client.");
                    
                    return;
                }
                
                break;
            default:
                return;  //Not the correct type id for this class.
        }
        
        switch(type_id){
            case HolePunchingUDPSocket.CONTACT_REQUEST:
                if(hole_puncher.getPhase() < HolePuncher.PHASE_SEND_REQUEST_RESPONSE){
                    hole_puncher.setPhase(HolePuncher.PHASE_SEND_REQUEST_RESPONSE);
                }
                
                break;
            case HolePunchingUDPSocket.CONTACT_RESPONSE:
                if(hole_puncher.getPhase() < HolePuncher.PHASE_SEND_KEEP_ALIVE){
                    hole_puncher.setPhase(HolePuncher.PHASE_SEND_KEEP_ALIVE);
                    
                    this._setClientStatus(client,ClientStatus.CONNECTED);
                }
                
                break;
            case HolePunchingUDPSocket.KEEP_ALIVE:
                if(hole_puncher.getPhase() != HolePuncher.PHASE_SEND_KEEP_ALIVE){
                    hole_puncher.setPhase(HolePuncher.PHASE_SEND_KEEP_ALIVE);
                    
                    this._setClientStatus(client,ClientStatus.CONNECTED);
                }
                
                buffer.rewind();
                buffer.putLong(this.getDescription());
                
                this.send(HolePunchingUDPSocket.KEEP_ALIVE_ACK,data,client);
                
                break;
            case HolePunchingUDPSocket.KEEP_ALIVE_ACK:
                hole_puncher.resetAttempts();
                
                break;
        }
    }
    
    protected void _onHolePunchFailure(Client client,int phase_on_failure){
        switch(phase_on_failure){
            case HolePuncher.PHASE_SEND_REQUEST:
                Log.n("Client failed to connect on the request phase.");
                this._setClientStatus(client,ClientStatus.CONNECTION_ERROR);
                
                break;
            case HolePuncher.PHASE_SEND_REQUEST_RESPONSE:
                Log.n("Client failed to connect on the request response phase.");
                this._setClientStatus(client,ClientStatus.CONNECTION_ERROR);
                
                break;
            case HolePuncher.PHASE_SEND_KEEP_ALIVE:
              //TODO:  Assume client disconnected and let any listeners know
                Log.n("We're assuming a client disconnected!");
                this._setClientStatus(client,ClientStatus.DISCONNECTED);
                
                this.__hole_puncher_by_client.get(client).stop();
                break;
        }
    }
    
    public void removeClient(Client client){
        if(this.__clients.remove(client)){
            this.__hole_puncher_by_client.remove(client).stop();
            
            if(this.__client_status_by_client.get(client) == ClientStatus.CONNECTED){
                this._setClientStatus(client,ClientStatus.DISCONNECTED);
            }
        }
    }
    
    public void removeAllClients(){
        for(int index = this.__clients.size() - 1;index > -1;index--){
            this.removeClient(this.__clients.get(index));
        }
    }
    
    public boolean send(byte[] data,Client client){
        return this.send(SimpleUDPSocket.DEFAULT_TYPE_ID,data,client);
    }
    
    public boolean send(int type_id,byte[] data,Client client){
        if(!this.__clients.contains(client)){
            Log.w("Client requested could not be found in client list.  Data will not be sent.");
            
            return false;
        }
        
        return super.send(type_id,data,client.getAddress(),client.getPort());
    }
    
    @Deprecated @Override public boolean send(byte[] data,InetAddress recipient_address,int recipient_port){
        return super.send(data,recipient_address,recipient_port);
    }
    
    @Deprecated @Override public boolean send(int type_id,byte[] data,InetAddress recipient_address,int recipient_port){
        return super.send(type_id,data,recipient_address,recipient_port);
    }
    
    @Deprecated @Override public boolean send(byte[] data,int begin,InetAddress recipient_address,int recipient_port){
        return super.send(data,begin,recipient_address,recipient_port);
    }
    
    @Deprecated @Override public boolean send(int type_id,byte[] data,int begin,InetAddress recipient_address,int recipient_port){
        return super.send(type_id,data,begin,recipient_address,recipient_port);
    }
    
    @Deprecated @Override public boolean send(int type_id,byte[] data,int begin,int end,InetAddress recipient_address,int recipient_port){
        return super.send(type_id,data,begin,end,recipient_address,recipient_port);
    }
    
    @Override public void stop(){
        this.removeAllClients();
        
        super.stop();
    }
    /*End Other Methods*/
}

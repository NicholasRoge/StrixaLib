/**
 * File:  HolePunchingUDPSocket.java
 * Date of Creation:  Feb 17, 2013
 */
package com.strixa.net;

import com.strixa.net.SimpleUDPSocket.PacketReceivedListener;
import com.strixa.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class HolePunchingUDPSocket extends SimpleUDPSocket implements PacketReceivedListener{
    public static class Client{
        private int         __ack_requests_counter;
        private InetAddress __address;
        private boolean     __connection_confirmed;
        private int         __port;
        
        
        /*Begin Constructors*/
        public Client(String host,int port) throws UnknownHostException{
            this(InetAddress.getByName(host),port);
        }
        
        public Client(InetAddress address,int port){
            this.__ack_requests_counter = 0;
            this.__address = address;
            this.__connection_confirmed = false;
            this.__port = port;
        }
        /*End Constructors*/
        
        /*Begin Getter/Setter Methods*/
        synchronized protected int _getAckRequestCount(){
            return this.__ack_requests_counter;
        }
        
        public InetAddress getAddress(){
            return this.__address;
        }
        
        public int getPort(){
            return this.__port;
        }
        
        public boolean isConnectionConfirmed(){
            return this.__connection_confirmed;
        }
        
        protected void _setConnectionConfirmed(boolean confirmed){
            this.__connection_confirmed = confirmed;
        }
        /*End Getter/Setter Methods*/
        
        /*Begin Other Methods*/
        synchronized protected void _resetAckRequestCounter(){
            this.__ack_requests_counter = 0;
        }
        
        synchronized protected void _incrementAckRequestCount(){
            this.__ack_requests_counter++;
        }
        /*End Other Methods*/
    }
    
    private class HolePuncher implements Runnable{
        public static final int PHASE_SEND_KEEP_ALIVE = 2;
        public static final int PHASE_SEND_REQUEST = 0;
        public static final int PHASE_SEND_REQUEST_RESPONSE = 1;
        public static final int PHASE_STOPPED = -1;
        
        private Client  __client;
        private int     __client_index;
        private long    __send_cooldown;
        private byte[]  __data;
        private boolean __paused;
        private int     __phase;
        private boolean __run;
        private Thread  __run_thread;
        
        
        /*Begin Constructors*/
        public HolePuncher(Client client,int client_index){
            this.__client = client;
            this.__client_index = client_index;
            this.__phase = HolePuncher.PHASE_STOPPED;
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
                switch(this.__phase){
                    case HolePuncher.PHASE_SEND_REQUEST_RESPONSE:
                        socket.send(HolePunchingUDPSocket.CONTACT_RESPONSE,this.__data,this.__client);

                    case HolePuncher.PHASE_SEND_REQUEST:
                        socket.send(HolePunchingUDPSocket.CONTACT_REQUEST,this.__data,this.__client);

                        break;
                    case HolePuncher.PHASE_SEND_KEEP_ALIVE:
                        socket.send(HolePunchingUDPSocket.KEEP_ALIVE,this.__data,this.__client);
                        
                        this.__client._incrementAckRequestCount();
                        if(this.__client._getAckRequestCount() > 5){
                            //TODO:  Assume client disconnected and let any listeners know
                            Log.n("We're assuming a client disconnected!");
                            
                            HolePunchingUDPSocket.this.removeClient(this.__client);
                        }

                        break;
                }

                try{
                    Thread.sleep(this.__send_cooldown);//this.__send_cooldown);
                }catch(InterruptedException e){
                    //TODO:  Nothing!  
                }
                
                while(this.__paused){  //This will occur when a phase change is occurring
                    try{
                        Thread.sleep(50);
                    }catch(InterruptedException e){
                        //TODO:  Nothing!
                    }
                }
            }
        }
        
        synchronized public void setPhase(int phase){
            ByteBuffer buffer;
            
            
            if(phase <0 || phase > 2){
                throw new IllegalArgumentException("Invalid mode identifier.");
            }
            
            if(phase == this.__phase){
                return;
            }else{
                this.__phase = phase;
            }
            this.__paused = true;
            
            switch(phase){
                case HolePuncher.PHASE_SEND_REQUEST:
                    this.__send_cooldown = 250;
                    
                    this.__data = new byte[(Integer.SIZE / 8)];
                    buffer = ByteBuffer.wrap(this.__data);
                    buffer.putInt(this.__client_index);
                    
                    this.__run_thread.setName("HolePuncher ContactRequest");
                    break;
                case HolePuncher.PHASE_SEND_REQUEST_RESPONSE:
                    this.__send_cooldown = 250;
                    
                    this.__data = new byte[(Integer.SIZE / 8)];
                    buffer = ByteBuffer.wrap(this.__data);
                    buffer.putInt(this.__client_index);
                    
                    this.__run_thread.setName("HolePuncher ContactRequestResponse");
                    break;
                case HolePuncher.PHASE_SEND_KEEP_ALIVE:
                    this.__send_cooldown = 1000;
                    
                    this.__data = new byte[(Integer.SIZE / 8)];
                    buffer = ByteBuffer.wrap(this.__data);
                    buffer.putInt(this.__client_index);
                    
                    this.__run_thread.setName("HolePuncher KeepAlive");
                    break;
            }
            
            this.__paused = false;
        }
        
        private int getPhase(){
            return this.__phase;
        }
        
        public void stop(){
            this.__run = false;
            this.__run_thread.interrupt();
        }
        /*End Other Methods*/
    }
    
    private static final int CONTACT_REQUEST = 1;
    private static final int CONTACT_RESPONSE = 2;
    private static final int KEEP_ALIVE = 3;
    private static final int KEEP_ALIVE_ACK = 4;
        
    private final ArrayList<Client>           __clients = new ArrayList<Client>();
    private final HashMap<Client,HolePuncher> __hole_puncher_by_client = new HashMap<Client,HolePuncher>();
     
    private SimpleUDPSocket __socket;
    
    
    /*Begin Constructors*/
    public HolePunchingUDPSocket(){
        super();
        
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_REQUEST,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_RESPONSE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE_ACK,this);
    }
    
    public HolePunchingUDPSocket(int port){
        super(port);
        
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_REQUEST,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.CONTACT_RESPONSE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE,this);
        this.addPacketReceivedListener(HolePunchingUDPSocket.KEEP_ALIVE_ACK,this);
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    public ArrayList<Client> getClients(){
        return new ArrayList<Client>(this.__clients);
    }
    
    protected ArrayList<Client> _getClients(){
        return this.__clients;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    public void addClient(Client client){
        this.addClient(client,true);
    }
    
    public void addClient(Client client,boolean blocking_mode){
        HolePuncher hole_puncher;
        
        
        if(!this.__clients.contains(client)){
            this.__clients.add(client);
            
            hole_puncher = new HolePuncher(client,this.__clients.indexOf(client));
            this.__hole_puncher_by_client.put(client,hole_puncher);
            
            new Thread(hole_puncher).start();
        }
        
        if(blocking_mode){
            while(!client.isConnectionConfirmed()){
                try{
                    Thread.sleep(50);
                }catch(InterruptedException e){
                    //TODO:  Nothing
                }
            }
        }
    }
    
    @Override public void onDataReceived(int header,byte[] data){
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        
        int         client_index;
        Client      client;
        HolePuncher hole_puncher;
                
        
        client_index = buffer.getInt();
        if(client_index >= this.__clients.size()){
            return;
        }
        
        client = this.__clients.get(client_index);
        hole_puncher = this.__hole_puncher_by_client.get(client);
        
        switch(header){
            case HolePunchingUDPSocket.CONTACT_REQUEST:
                if(hole_puncher.getPhase() < HolePuncher.PHASE_SEND_REQUEST_RESPONSE){
                    hole_puncher.setPhase(HolePuncher.PHASE_SEND_REQUEST_RESPONSE);
                }
                
                break;
            case HolePunchingUDPSocket.CONTACT_RESPONSE:
                if(hole_puncher.getPhase() < HolePuncher.PHASE_SEND_KEEP_ALIVE){
                    this.__hole_puncher_by_client.get(client).setPhase(HolePuncher.PHASE_SEND_KEEP_ALIVE);
                    
                    client._setConnectionConfirmed(true);
                }
                
                break;
            case HolePunchingUDPSocket.KEEP_ALIVE:
                if(hole_puncher.getPhase() != HolePuncher.PHASE_SEND_KEEP_ALIVE){
                    this.__hole_puncher_by_client.get(client).setPhase(HolePuncher.PHASE_SEND_KEEP_ALIVE);
                    
                    client._setConnectionConfirmed(true);
                }
                
                this.send(HolePunchingUDPSocket.KEEP_ALIVE_ACK,data,client);
                
                break;
            case HolePunchingUDPSocket.KEEP_ALIVE_ACK:
                client._resetAckRequestCounter();
                
                break;
        }
    }
    
    public void removeClient(Client client){
        if(this.__clients.remove(client)){
            this.__hole_puncher_by_client.remove(client).stop();
        }
    }
    
    public void removeAllClients(){
        for(int index = 0,end = this.__clients.size();index < end;index++){
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
    /*End Other Methods*/
}

/**
 * File:  SimpleUDPSocket.java
 * Date of Creation:  Feb 16, 2013
 */
package com.strixa.net;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.strixa.util.Log;

/**
 * TODO:  Write Class Description
 */
public class SimpleUDPSocket{
    private class DataDeliverer implements Runnable{
        private byte[]                  __data;
        private int                     __header;
        private UDPDataReceivedListener __listener;
        private InetAddress             __source_address;
        private int                     __source_port;
        
        
        /*Begin Constructors*/
        public DataDeliverer(UDPDataReceivedListener listener,int header,byte[] data,InetAddress source_address,int source_port){           
            this.__data = data;
            this.__header = header;
            this.__listener =  listener;
            this.__source_address = source_address;
            this.__source_port = source_port;
        }
        /*End Constructors*/
        
        /*Begin Constructors*/
        public void run(){
            this.__listener.onUDPDataReceived(this.__header,this.__data,this.__source_address,this.__source_port);
        }
        /*Begin Constructors*/
    }
    
    public interface UDPDataReceivedListener{
        public void onUDPDataReceived(int header,byte[] data,InetAddress source_address,int source_port);
    }
    
    private class PacketReceiver implements Runnable{
        private boolean __run;
        
        
        public void run(){
            DatagramPacket packet;

            
            this.__run = true;
            while(this.__run){
                packet = new DatagramPacket(new byte[SimpleUDPSocket.HEADER_LENGTH + SimpleUDPSocket.MAX_PACKET_SIZE],SimpleUDPSocket.HEADER_LENGTH + SimpleUDPSocket.MAX_PACKET_SIZE);
                
                try{
                    SimpleUDPSocket.this.__communication_socket.receive(packet);
                }catch(SocketTimeoutException e){
                    continue;
                }catch(IOException e){
                    if(SimpleUDPSocket.this.__communication_socket.isConnected()){  //If the connection is closed, don't display any error, as its likely this happened because the user closed teh connection.
                        Log.w("An error occured while attempting to receive data from the communication socket.");
                    }
                    
                    continue;
                }
                
                SimpleUDPSocket.this._onDatagramReceived(packet);
            }
        }
        
        public void stop(){
            this.__run = false;
        }
    }
    
    /**Default Type ID if one is not supplied to any of the methods which request it.*/
    public static final int DEFAULT_TYPE_ID = 0x0;
    /**Length of the header (in bytes)*/
    public static final int HEADER_LENGTH = SimpleUDPSocket.__INT_SIZE * 2; 
    /**Maximum size (in bytes) the data sent through this object can be.*/
    public static final int MAX_PACKET_SIZE = 1024;
    
    private static final int __INT_SIZE = Integer.SIZE / 8;
    
    private HashMap<Integer,ArrayList<UDPDataReceivedListener>> __packet_received_listeners_by_type_id = new HashMap<Integer,ArrayList<UDPDataReceivedListener>>();
    
    private DatagramSocket __communication_socket;
    private PacketReceiver __packet_receiver;
    private Thread         __packet_receiver_thread;
    private int            __preferred_port;
    
    
    /*Begin Constructors*/
    public SimpleUDPSocket(){
        this.__preferred_port = -1;
    }
    
    public SimpleUDPSocket(int port){
        this.__preferred_port = port;
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    public DatagramSocket getRawSocket(){
        return this.__communication_socket;
    }
    
    public boolean isStarted(){
        return this.__communication_socket != null;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    public void addPacketReceivedListener(UDPDataReceivedListener listener){
        this.addPacketReceivedListener(SimpleUDPSocket.DEFAULT_TYPE_ID,listener);
    }
    
    public void addPacketReceivedListener(int type_id,UDPDataReceivedListener listener){
        ArrayList<UDPDataReceivedListener> listeners;
        
        
        listeners = this.__packet_received_listeners_by_type_id.get(type_id);
        if(listeners == null){
            listeners = new ArrayList<UDPDataReceivedListener>();
            this.__packet_received_listeners_by_type_id.put(type_id,listeners);
        }
        
        listeners.add(listener);
    }
    
    protected byte[] _getDataFromPacketData(byte[] data){
        final int data_start = SimpleUDPSocket.HEADER_LENGTH;
        final int data_length = ByteBuffer.wrap(data).getInt(SimpleUDPSocket.HEADER_LENGTH / 2);
        
        
        if(data_length == 0){
            return null;
        }else{
            return Arrays.copyOfRange(data,data_start,data_start + data_length);
        }
    }
    
    protected int _getTypeIdFromPacketData(byte[] data){
        return ByteBuffer.wrap(data).getInt();
    }
    
    protected void _onDatagramReceived(DatagramPacket packet){        
        final byte[]      data = this._getDataFromPacketData(packet.getData());
        final InetAddress source_address = packet.getAddress();
        final int         source_port = packet.getPort();
        final int         type_id = this._getTypeIdFromPacketData(packet.getData());
        
        ArrayList<UDPDataReceivedListener> listeners;
        
        
        listeners = this.__packet_received_listeners_by_type_id.get(type_id);
        if(listeners != null){
            for(int index = 0,end = listeners.size();index < end;index++){
                new Thread(new DataDeliverer(listeners.get(index),type_id,data,source_address,source_port)).start();
            }
        }   
    }
    
    public void removePacketReceivedListener(UDPDataReceivedListener listener){
        this.removePacketReceivedListener(SimpleUDPSocket.DEFAULT_TYPE_ID,listener);
    }
    
    public void removePacketReceivedListener(int type_id,UDPDataReceivedListener listener){
        ArrayList<UDPDataReceivedListener> listeners;
        
        
        listeners = this.__packet_received_listeners_by_type_id.get(type_id);
        if(listeners != null){
            listeners.remove(listener);
        }
    }    
    
    public boolean send(byte[] data,InetAddress recipient_address,int recipient_port){
        return this.send(SimpleUDPSocket.DEFAULT_TYPE_ID,data,0,data.length,recipient_address,recipient_port);
    }
    
    public boolean send(int type_id,byte[] data,InetAddress recipient_address,int recipient_port){
        return this.send(type_id,data,0,data.length,recipient_address,recipient_port);
    }
    
    public boolean send(byte[] data,int begin,InetAddress recipient_address,int recipient_port){
        return this.send(SimpleUDPSocket.DEFAULT_TYPE_ID,data,begin,data.length,recipient_address,recipient_port);
    }
    
    public boolean send(int type_id,byte[] data,int begin,InetAddress recipient_address,int recipient_port){
        return this.send(type_id,data,begin,data.length,recipient_address,recipient_port);
    }
    
    /**
     * Sends the subset of the data identified by the given parameters.
     * 
     * @param data Data to be sent.
     * @param begin Inclusive; Beginning offset to start the data subset from.
     * @param end Exclusive; End offset to end the data subset at.
     * 
     * @return Returns true if the data was sent, and false, otherwise.
     */
    public boolean send(int type_id,byte[] data,int begin,int end,InetAddress recipient_address,int recipient_port){
        final int    data_length = end - begin;
        final byte[] packet_data = new byte[SimpleUDPSocket.HEADER_LENGTH + (end - begin)];
        
        ByteBuffer buffer;
        DatagramPacket packet;
        
        
        buffer = ByteBuffer.wrap(packet_data);
        buffer.putInt(type_id);
        
        if(data == null){
            buffer.putInt(0);
        }else{
            buffer.putInt(data_length);
        
            if(data_length > SimpleUDPSocket.MAX_PACKET_SIZE){
                Log.e("The amount of data requested to be sent was too large.");
                
                return false;
            }else if(data_length > data.length){
                Log.e("The amount of data specified by 'end - begin' was larger than the data that exists within argument 'data'.");
                
                return false;
            }
            
            buffer.put(Arrays.copyOfRange(data,begin,end));
        }
        
        packet = new DatagramPacket(packet_data,packet_data.length,recipient_address,recipient_port);
        try{
            this.__communication_socket.send(packet);
        }catch(IOException e){
            Log.e("Could not send data because an IOException occured.  Message:  " + e.getMessage());
            
            return false;
        }
        
        return true;
    }
    
    public boolean start(){
        try{
            if(this.__preferred_port == -1){
                this.__communication_socket = new DatagramSocket();
            }else{
                this.__communication_socket = new DatagramSocket(this.__preferred_port);
            }
            
            this.__communication_socket.setSoTimeout(1000);
        }catch(SocketException e){
            Log.e("Could not start the SimpleUDPSocket because of a socket exception.  Message:  " + e.getMessage());
            
            return false;
        }catch(SecurityException e){
            Log.e("Could not start the SimpleUDPSocket because a security manager that is in place wouldn't allow it.");
            
            return false;
        }
        
        this.__packet_receiver = new PacketReceiver();
        this.__packet_receiver_thread = new Thread(this.__packet_receiver,"Packet Receiver");
        this.__packet_receiver_thread.start();
        
        return true;
    }
    
    public void stop(){
        this.__packet_receiver.stop();
        
        this.__communication_socket.close();
    }
    /*End Other Methods*/
}

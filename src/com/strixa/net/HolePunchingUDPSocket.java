/**
 * File:  HolePunchingUDPSocket.java
 * Date of Creation:  Feb 17, 2013
 */
package com.strixa.net;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class HolePunchingUDPSocket extends SimpleUDPSocket{
    private enum DatagramHeader{     
        CONNECT_REQUEST(new byte[]{0x12,0x0B,-0x30,0x0B}),
        DATA(new byte[]{0x04,0x77,-0x63,-0x05}),
        KEEP_ALIVE(new byte[]{-0x21,0x4,-0x55,0x16}),
        UNKNOWN(new byte[]{0x00,0x00,0x00,0x00});
        
        private static final HashMap<Integer,DatagramHeader> datagramheader_by_header = new HashMap<Integer,DatagramHeader>();
        public static final int                              HEADER_LENGTH = 4;
        
        private byte[] __header;
        
        /*Begin Initializers*/
        static{
            final ByteBuffer buffer = ByteBuffer.allocate(DatagramHeader.HEADER_LENGTH);
            
            
            for(DatagramHeader datagram_header : DatagramHeader.values()){
                buffer.put(datagram_header.toByteArray());
                buffer.rewind();
                
                DatagramHeader.datagramheader_by_header.put(buffer.getInt(),datagram_header);
                
                buffer.clear();
            }
        }
        /*End Initializers*/
        
        /*Begin Constructors*/
        private DatagramHeader(byte[] header){
            if(header.length != HEADER_LENGTH){
                throw new RuntimeException("Invalid header length for '" + this.name() + "'.");
            }
            
            this.__header = header;
        }
        /*End Constructors*/
        
        /*Begin Other Methods*/
        public byte[] toByteArray(){
            return this.__header;
        }
        /*End Other Methods*/
        
        /*Begin Static Methods*/
        public static DatagramHeader fromByteArray(byte[] header){
            final ByteBuffer buffer = ByteBuffer.allocate(DatagramHeader.HEADER_LENGTH);
            DatagramHeader datagram_header;
            
            
            if(header.length > DatagramHeader.HEADER_LENGTH){
                return DatagramHeader.UNKNOWN;
            }
            
            buffer.put(header);
            buffer.rewind();
            
            datagram_header = DatagramHeader.datagramheader_by_header.get(buffer.getInt());
            return (datagram_header == null?DatagramHeader.UNKNOWN:datagram_header);
        }
        /*End Static Methods*/
    }
}

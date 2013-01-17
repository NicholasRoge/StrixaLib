/**
 * File:  Message.java
 * Date of Creation:  Jan 15, 2013
 */
package com.strixa.net;

import java.io.NotSerializableException;
import java.io.Serializable;

/**
 * Provides an immutable object to be transferred over a network connection.
 */
public class Message implements Serializable{
    private static final long serialVersionUID = -1995005965146008490L;
    
    private Object __message;
    private byte   __message_code;
    
    
    /*Begin Constructors*/
    /**
     * Constructs the Message.
     * 
     * @param message_code Message code to instruct the receiving socket on how to interpret the data.
     * @param message Message to be sent to the receiving socket.  This object MUST be serializable.
     * 
     * @throws NotSerializableException Thrown if argument 'message' has not implemented the Serializable interface.
     */
    public Message(byte message_code,Object message) throws NotSerializableException{
    	if(!(message instanceof Serializable)){
    		throw new NotSerializableException();
    	}
    	
        this.__message = message;
        this.__message_code = message_code;
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets the contents of this Message.
     * 
     * @return The contents of this message.
     */
    public Object getMessage(){
        return this.__message;
    }
    
    /**
     * Gets this Message's message code, allowing the receiving socket to interpret the data stored within.
     * 
     * @return This Message's message code.
     */
    public byte getMessageCode(){
        return this.__message_code;
    }
    /*End Getter/Setter Methods*/
}

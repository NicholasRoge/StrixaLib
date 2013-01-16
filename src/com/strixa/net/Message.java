/**
 * File:  Message.java
 * Date of Creation:  Jan 15, 2013
 */
package com.strixa.net;

import java.io.Serializable;

/**
 * TODO:  Write Class Description
 *
 * @author Nicholas Rogé
 */
public class Message implements Serializable{
    private static final long serialVersionUID = -1995005965146008490L;
    
    private Object __message;
    private byte   __message_code;
    
    
    /*Begin Constructors*/
    public Message(byte message_code,Object message){
        this.__message = message;
        this.__message_code = message_code;
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    public Object getMessage(){
        return this.__message;
    }
    
    public byte getMessageCode(){
        return this.__message_code;
    }
}

/**
 * File:  Log.java
 * Date of Creation:  Nov 6, 2012
 */
package com.strixa.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.swing.SwingUtilities;

/**
 * Class function is to provide a simplified and uniform means of logging application status.
 *
 * @author Nicholas Rogé
 */
public class Log{
    private static class Message{
        public String  message;
        public long    timestamp;
        public Type type;
        
        
        /*Begin Constructors*/
        public Message(Type type,long timestamp,String message){  //TODO_HIGH:  Privatize all access to this object's members, making this a read-only structure.
            this.message = message;
            this.timestamp = timestamp;
            this.type = type;
        }
    }
    
    private static class MessageRunner implements Runnable{
        private Message __message;
        
        
        /*Begin Constructors*/
        public MessageRunner(Message message){
            this.__message = message;
        }
        /*End Constructors*/
        
        /*Begin Other Methods*/
        public void run(){
            Log.__logEvent(this.__message,0);
        }
        /*End Other Methods*/
    }
    
    /**
     * Type of log that should be posted to the log stream.
     *
     * @author Nicholas Rogé
     */
    public enum Type{
        /** Describes a status in which a section of the application has failed and could not recover. */
        ERROR("Error"),
        /** Posts a message to the log stream. */
        NOTICE("Notice"),
        /** Describes a status in which a section of the application failed, but successfully recovered. */
        WARNING("Warning");
        
        private String __as_string;
        
        
        private Type(String as_string){
            if(as_string == null){
                throw new NullPointerException("Argument 'as_string' must not be null.");
            }
            
            this.__as_string = as_string;
        }
        
        public String toString(){System.out.println();
            return this.__as_string;
        }
    }
    
    private static OutputStream __output_stream = System.out;
    
    
    /*Begin Static Methods*/
    private static void __addMessage(final Type type,long timestamp,String message){
        SwingUtilities.invokeLater(new MessageRunner(new Message(type,timestamp,message)));
    }
    
    /**
     * Logs an event to the requested output stream.
     * 
     * @param type Type of message to log.
     * @param message Message to add to the log.
     */
    public static void logEvent(Type type,String message){
        Log.__addMessage(type,System.currentTimeMillis(),message);
    }
    
    private static void __logEvent(Message message,int attempt){
        StringBuffer buffer = null;
        Date         date   = null;
        
        
        date = new Date(message.timestamp);
        
        buffer = new StringBuffer();
        buffer.append('[');
        buffer.append(message.type);
        buffer.append("][");
        buffer.append(date.getHours());
        buffer.append(':');
        buffer.append(date.getMinutes());
        buffer.append(':');
        buffer.append(date.getSeconds());
        buffer.append("]:  ");
        buffer.append(message.message);
        buffer.append('\n');
        
        try{
            Log.__output_stream.write(buffer.toString().getBytes());
        }catch(IOException e){
            if(attempt < 3){  //Try to write the message 3 times.
                try{
                    Thread.sleep(1);
                }catch(InterruptedException exception){
                    //Fail silently.
                }
                
                Log.__logEvent(message,attempt + 1);
            }
        }
    }
}

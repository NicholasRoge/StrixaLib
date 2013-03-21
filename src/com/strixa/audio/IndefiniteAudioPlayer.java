/**
 * File:  LocalMediaPlayer.java
 * Date of Creation:  Mar 18, 2013
 */
package com.strixa.audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.strixa.util.Log;

/**
 * TODO:  Write Class Description
 *
 * @author Nicho_000
 */
public class IndefiniteAudioPlayer extends AudioPlayer implements Runnable{
    private byte[][]              __buffer;
    private int                   __buffer_end;
    private int                   __buffer_fill;
    private int                   __buffer_length;  //Number of bytes in 50ms of audio.
    private int                   __buffer_size;    //Second dimension of the array.
    private int                   __buffer_start;
    private AudioFormat           __format;
    private DataLine.Info         __info;
    private SourceDataLine        __player;
    private Thread                __player_thread;
    
    
    /*Begin Constructor*/
    public IndefiniteAudioPlayer(){
    }
    /*End Constructor*/
    
    /*Begin Getter/Setter Methods*/
    public boolean hasData(){
        return this.__buffer_start == this.__buffer_end;  //Though yes, there could be data still being put into the current buffer, for all intents and purposes, we will consider this a dataless situation.
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    private void __advanceBufferEnd(){
        this.__buffer_end = (this.__buffer_end + 1) % this.__buffer_size;
    }
    
    private void __advanceBufferStart(){
        this.__buffer_start = (this.__buffer_start + 1) % this.__buffer_size;
    }
    
    protected boolean _open(){
        try{
            this.__player = (SourceDataLine)AudioSystem.getLine(this.__info);
            this.__player.open(this.__format);
        }catch(LineUnavailableException e){
            Log.e("Could not retrieve an audio output device for the given audio settings.");
            
            return false;
        }
        
        return true;
    }
    
    public boolean play(){
        if(this.isPlaying()){
            Log.w("The audio player is already playing.  Calling this method will not have any effect.");
            
            return false;
        }
        
        this.__format = new AudioFormat(8000,16,1,true,true);
        
        this.__buffer_length = (int)(this.__format.getSampleRate() / 20) * (this.__format.getSampleSizeInBits() / 8);
        this.__buffer_size = 100;
        this.__buffer = new byte[this.__buffer_size][this.__buffer_length];  //Allow for the ability to store enough data to buffer up to one second
        this.__buffer_start = 0;
        this.__buffer_end = 0;
        this.__buffer_fill = 0;
        
        this.__info = new DataLine.Info(SourceDataLine.class,this.__format,this.__buffer_length);
        
        if(this._open()){
            this.__player.start();
            
            this._setIsPlaying(true);
            
            this.__player_thread = new Thread(this,"IndefiniteAudioPlayer PlayerLoop");
            this.__player_thread.start();
            
            return true;
        }else{
            return false;
        }
    }
    
    protected boolean _restart(){
        if(this.stop() && this.play()){
            return true;
        }else{
            return false;
        }
    }
    
    public void run(){
        while(this.isPlaying()){
            if(this.__buffer_start == this.__buffer_end){
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){}
                
                continue;
            }
            
            synchronized(this.__buffer){
                this.__player.write(this.__buffer[this.__buffer_start],0,this.__buffer_length);
            
                this.__advanceBufferStart();
            }
        }
        this.__player_thread = null;
    }
    
    public boolean stop(){
        if(this.isStopped()){
            Log.w("The audio player must be started prior to an attempted stop.");
            
            return false;
        }
        
        /*Stop all the threads*/
        this._setIsPlaying(false);
        if(this.__player_thread != Thread.currentThread()){  //This will be true if there was an error in the player loop, and it was required to stop.
            while(this.__player_thread != null){
                try{
                    Thread.sleep(50);
                }catch(InterruptedException e){}
            }
        }
        
        /*Clean up the buffer*/
        this.__buffer = null;
        
        /*Close the player*/
        if(this.__player.isOpen()){
            this.__player.close();
        }
        
        return false;
    }
    
    /**
     * This method will block until a full buffer's length has been written.  It will also block if no additional buffer space is available.
     */
    public synchronized boolean putData(byte[] data){        
        int data_remainder;
        int buffer_remainder;
        int fill_remainder;
        int length;
        int offset;
        
                
        data_remainder = data.length;
        fill_remainder = this.__buffer_length - this.__buffer_fill;
        
        /*Calculate the amount of space available in the buffer*/
        if(this.__buffer_start == this.__buffer_end){
            buffer_remainder = (this.__buffer_size * this.__buffer_length) + fill_remainder;
        }else if(this.__buffer_end > this.__buffer_start){
            buffer_remainder = ((this.__buffer_size - (this.__buffer_end - this.__buffer_start)) * this.__buffer_length) + fill_remainder;
        }else{
            buffer_remainder = ((this.__buffer_end - this.__buffer_start) * this.__buffer_length) + fill_remainder;
        }
        buffer_remainder -= this.__buffer_length;
        
        if(data_remainder > buffer_remainder){
            return false;
        }
        
        /*Put the data in the buffer*/
        if(data_remainder < fill_remainder){
            System.arraycopy(data,0,this.__buffer[this.__buffer_end],this.__buffer_fill,data.length);
            this.__buffer_fill += data.length;
        }else{
            System.arraycopy(data,0,this.__buffer[this.__buffer_end],this.__buffer_fill,fill_remainder);
            data_remainder -= fill_remainder;
            offset = fill_remainder;
            
            while((length = (data_remainder / this.__buffer_length)) > 0){
                this.__advanceBufferEnd();
                
                System.arraycopy(data,offset,this.__buffer[this.__buffer_end],0,length);
                offset += length;
                data_remainder -= length;
            }
            
            this.__advanceBufferEnd();
            
            System.arraycopy(data,offset,this.__buffer[this.__buffer_end],0,data_remainder);
            this.__buffer_fill += data_remainder;
        }
        
        return true;
    }
    /*End Other Methods*/
}

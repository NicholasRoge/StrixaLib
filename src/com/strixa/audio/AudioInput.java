/**
 * File:  AudioInput.java
 * Date of Creation:  Mar 18, 2013
 */
package com.strixa.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.strixa.util.Log;


public class AudioInput implements Runnable{
    public interface AudioInputReceivedListener{
        public void onAudioInputReceived(byte[] data);
    }
    
    private class DataDeliverer implements Runnable{
        private byte[]                     __data;
        private AudioInputReceivedListener __listener;
        
        
        /*Begin Constructors*/
        public DataDeliverer(AudioInputReceivedListener listener,byte[] data){
            this.__listener = listener;
            this.__data = data;
        }
        
        public void run(){
            this.__listener.onAudioInputReceived(this.__data);
        }
    }
    
    private final ArrayList<AudioInputReceivedListener> __audio_input_received_listeners = new ArrayList<AudioInputReceivedListener>();
    private final ArrayList<TargetDataLine>             __lines = new ArrayList<TargetDataLine>();
    
    private int              __buffer_length;
    private AudioFormat      __format;
    private Line.Info        __info;
    private TargetDataLine   __input;
    private AudioInputStream __input_stream;
    private boolean          __started;
    
    
    /*Begin Initializer*/
    {
        this.__started = false;
    }
    /*End Initializer*/
    
    /*Begin Constructors*/
    public boolean isStarted(){
        return this.__started;
    }
    
    public boolean isStopped(){
        return !this.__started;
    }
    
    protected void _setIsStarted(boolean started){
        this.__started = started;
    }
    /*End Constructors*/
    
    /*Begin Other Methods*/
    public void addAudioInputReceivedListener(AudioInputReceivedListener listener){
        if(!this.__audio_input_received_listeners.contains(listener)){
            this.__audio_input_received_listeners.add(listener);
        }
    }
    
    protected void _broadcastAudioInput(byte[] data){
        for(int index = 0,end = this.__audio_input_received_listeners.size();index < end;index++){
            new Thread(new DataDeliverer(this.__audio_input_received_listeners.get(index),Arrays.copyOf(data,data.length)),"AudioInput DataDeliverer").start();
        }
    }
    
    public void removeAudioInputReceivedListener(AudioInputReceivedListener listener){
        this.__audio_input_received_listeners.remove(listener);
    }
    
    public void run(){
        final byte[] data = new byte[this.__buffer_length]; 
        
        
        try{
            while(this.isStarted()){
                this.__input_stream.read(data);
                this._broadcastAudioInput(data);
            }
        }catch(IOException e){
            //TODO:  Error mesage
            
            this.stop();
        }
    }
    
    public boolean start(){        
        this.__format = new AudioFormat(8000,16,1,true,true);
        this.__info = new DataLine.Info(TargetDataLine.class,this.__format);        
        if(!AudioSystem.isLineSupported(this.__info)){
            Log.e("There are no devices on this computer which support audio input.");
            
            return false;
        }
        
        this.__buffer_length = (int)(this.__format.getSampleRate() / 20) * (this.__format.getSampleSizeInBits() / 8);
        
        try{
            this.__input = (TargetDataLine)AudioSystem.getLine(this.__info);
            this.__input.open(this.__format,this.__buffer_length * 4);
        }catch(LineUnavailableException e){
            Log.e("Could not get a handle to the requested input.");
            
            return false;
        }
        
        this.__input_stream = new AudioInputStream(this.__input);
        
        this.__input.start();
        this._setIsStarted(true);
        new Thread(this,"AudioInput ReceivingLoop").start();
        
        return true;
    }
    
    public boolean stop(){
        this._setIsStarted(false);
        
        this.__input.close();
        this.__input.stop();
        
        return true;
    }
    /*End Other Methods*/
}

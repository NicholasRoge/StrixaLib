/**
 * File:  MediaPlayer.java
 * Date of Creation:  Mar 18, 2013
 */
package com.strixa.audio;


public abstract class AudioPlayer{
    private boolean __playing;
    
    
    /*Begin Initializer*/
    {
        this.__playing = false;
    }
    /*End Initializer*/
    
    /*Begin Getter/Setter Methods*/
    public boolean isPlaying(){
        return this.__playing;
    }
    
    public boolean isStopped(){
        return !this.__playing;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    protected void _setIsPlaying(boolean playing){
        this.__playing = playing;
    }
    /*End Other Methods*/
    
    /*Begin Abstract Methods*/
    public abstract boolean play();
    
    public abstract boolean stop();
    /*End Abstract Methods*/
}

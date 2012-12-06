/**
 * File:  FileReader.java
 * Date of Creation:  Nov 28, 2012
 */
package com.strixa.fileio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.strixa.gl.util.PercentLoadedUpdateListener;
import com.strixa.util.FileIO;
import com.strixa.util.Log;

/**
 * Reads a file line by line and presents it in an easy to digest for to any inheriting classes.
 *
 * @author Nicholas Rogé
 */
public abstract class AsciiFileReader implements Runnable{
    private final Thread                            __file_read_thread = new Thread(this,"File Read Thread");
    private final List<PercentLoadedUpdateListener> __percent_loaded_listeners = new ArrayList<PercentLoadedUpdateListener>();

    private String  __filename;
    private boolean __reading;
    
    
    /*Begin Constructor*/
    /**
     * Constructs a new AsciiFileReader object and prepares it to be read.
     * 
     * @param filename Path to file being read.  This may be absolute or relative.
     * 
     * @throws FileNotFoundException Thrown if the requested file does not exist.
     * @throws IllegalArgumentException Thrown if filename is null or empty.
     */
    public AsciiFileReader(String filename) throws FileNotFoundException,IllegalArgumentException{
        if(filename == null || filename.isEmpty()){
            throw new IllegalArgumentException();
        }
        
        if(!(new File(filename)).exists()){
            throw new FileNotFoundException();
        }
        
        this.__filename = (new File(filename).getAbsolutePath());
        this.__reading = false;
    }
    /*End Constructor*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets the file that this object will be reading from.
     * 
     * @return The path to the file and filename of the file.
     */
    public String getFilename(){
        return this.__filename;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    /**
     * Adds a {@link PercentLoadedUpdateListener} be notified when this object needs to send out updates.
     * 
     * @param listener Listener requesting to be notified.
     */
    public void addPercentLoadedUpdateListener(PercentLoadedUpdateListener listener){
        if(!this.__percent_loaded_listeners.contains(listener)){            
            this.__percent_loaded_listeners.add(listener);
        }else{
            Log.logEvent(Log.Type.NOTICE,"You may not add a PercentLoadedUpdateListener to this object more than once.");
        }
    }
    
    /**
     * Alerts all the percent updated listeners that more the file has been loaded.
     * 
     * @param amount_loaded Amount of the file that has been loaded.
     */
    protected void _alertPercentLoadedUpdateListeners(double amount_loaded){
        for(int index = 0;index < this.__percent_loaded_listeners.size();index++){
            this.__percent_loaded_listeners.get(index).onPercentLoadedUpdate(amount_loaded);
        }
    }
    
    /**
     * Returns whether the file read thread is currently reading or not.
     * 
     * @return Returns true if the file is being read, and false, otherwise.
     */
    public boolean isReading(){
        return this.__reading;
    }
    
    /**
     * Starts reading the file.
     */
    public void read(){
        if(this.isReading()){
            Log.logEvent(Log.Type.NOTICE,"The file is already being read.");
            
            return;
        }
        
        this.__reading = true;
        this.__file_read_thread.start();
    }
    
    /**
     * Removes the given listener from the update list.
     * 
     * @param listener Listener to be removed.
     */
    public void removePercentLoadedUpdateListener(PercentLoadedUpdateListener listener){
        if(this.__percent_loaded_listeners.contains(listener)){
            this.__percent_loaded_listeners.remove(listener);
        }else{
            Log.logEvent(Log.Type.NOTICE,"This object does not have the given PercentLoadedUpdateListener registered to it.");
        }
    }
    
    @Override public void run(){        
        int               file_stream_size = 0;
        String            line = null;
        ArrayList<String> lines = null;
        double            percent_loaded = 0;
        FileInputStream   stream = null;
        
        
        //First read in all the lines in the file.
        try{
            stream = new FileInputStream(this.__filename);
            lines = new ArrayList<String>();
            
            this._alertPercentLoadedUpdateListeners(0);
            
            try{
                file_stream_size = stream.available();
            }catch(IOException e){
                Log.logEvent(Log.Type.NOTICE,"Total file size could not be retrieved.  Percent loaded will not be updated until read is finished.");
            }
            
            while((line = FileIO.readLine(stream)) != null){  
                lines.add(line);
                
                try{
                    this._alertPercentLoadedUpdateListeners(((double)(file_stream_size - stream.available())/(double)file_stream_size) / .02);  //Reading in the lines is only the first half.  The send bit is processing them.
                }catch(IOException e){
                    Log.logEvent(Log.Type.NOTICE,"Amount of file read could not be retrieved.  Percent loaded will not be updated until the next line has been read.");
                }
            }
        }catch(IOException e){
            Log.logEvent(Log.Type.ERROR,"File read fail.  Exception message:  " + e.getMessage());
            
            try{
                stream.close();
            }catch(IOException closing_e){
                Log.logEvent(Log.Type.WARNING,"File stream could not be closed.  Excpetion message:  " + closing_e.getMessage());
            }
            
            this._alertPercentLoadedUpdateListeners(-1);
        }
        
        try{
            stream.close();
        }catch(IOException e){
            Log.logEvent(Log.Type.WARNING,"File read completed normally, but file stream could not be closed.  Excpetion message:  " + e.getMessage());
        }
        
        //Then process each of the lines
        for(int line_index = 0,line_end_index = lines.size() - 1;line_index <= line_end_index;line_index++){
            if(lines.get(line_index).isEmpty()){
                if(!this._processLine(line_index + 1,new String[0])){
                    this._alertPercentLoadedUpdateListeners(-1);
                    
                    return;
                }
            }else{
                if(!this._processLine(line_index + 1,lines.get(line_index).split(" "))){
                    this._alertPercentLoadedUpdateListeners(-1);
                    
                    return;
                }
            }
            
            percent_loaded = 50 + (((double)(line_index + 1)/(double)lines.size()) / .02);
            if(percent_loaded < 100){
                this._alertPercentLoadedUpdateListeners(percent_loaded);
            }
        }
        
        this.__reading = false;
        this._alertPercentLoadedUpdateListeners(100);
    }
    
    /*Begin Abstract Methods*/
    /**
     * Provides information about a line that was just read from the user requested file.
     * 
     * @param line_number Provides the current line in the file.
     * @param arguments This will be a space delimited list of the arguments on the line to be processed.
     * 
     * @return True should be returned if processing can continue, and false should be returned if processing should not continue, and a progress update of -1 (failure) should be sent to the progress update listeners.
     */
    protected abstract boolean _processLine(int line_number,String[] arguments);
    /*End Abstract Methods*/
}

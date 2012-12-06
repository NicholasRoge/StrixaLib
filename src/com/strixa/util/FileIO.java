package com.strixa.util;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Nicholas Roge
 *
 * Contains methods that aid in working with files.
 */
public class FileIO{
	/*Begin Static Methods*/
	/**
	 * Reads a line in from the given file stream.
	 * 
	 * @param file File stream to read from.
	 * 
	 * @return Returns the line read from the file, or an empty string if the stream contains no more input.
	 *  
	 * @throws IOException Thrown if there is an error reading from the file stream.
	 */
	public static String readLine(FileInputStream file) throws IOException{
        String line = null;
        int character = 0x00;
        
        
        if(file == null){
            throw new IllegalArgumentException("Argument 'file' must not be null.");
        }
        
        line = new String();
        do{
            character = file.read();
            
            line += (char)character;
        }while(character != '\n' && character != -1);
        
        if(line.charAt(line.length() - 1) == (char)-1){
            line = line.substring(0,line.length()-1);  //This kills the end character
            if(line.isEmpty()){
                line = null;
            }
        }else if(line.charAt(line.length() - 1) == '\n'){
            line = line.substring(0,line.length() - 1);  //This kills the newline
        }
        
        return line;
    }
	/*End Static Methods*/
}

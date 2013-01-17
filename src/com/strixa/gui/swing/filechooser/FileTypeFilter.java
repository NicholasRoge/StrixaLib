/**
 * File:  FileTypeFilter.java
 * Date of Creation:  Dec 4, 2012
 */
package com.strixa.gui.swing.filechooser;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filters files based on their file extension.
 *
 * @author Nicholas Rogé
 */
public class FileTypeFilter extends FileFilter{
    private String[] __file_types;
    
    
    /*Begin Constructors*/
    /**
     * Sets up a FileFilter which filters based on the file's extension.
     * 
     * @param file_types File extensions to keep.
     * 
     * @throws NullPointerException Thrown if any of the given arguments are null.
     */
    public FileTypeFilter(String... file_types){
        this.__file_types = new String[file_types.length];
        
        
        for(int index = 0,end_index = file_types.length - 1;index <= end_index;index++){
            if(file_types[index] == null){
                throw new NullPointerException();
            }else{
                this.__file_types[index] = new String(file_types[index]);
            }
        }
    }
    /*End Constructors*/
    
    /*Begin Other Methods*/
    @Override public boolean accept(File file){        
        String extension = null;
        
        
        if(file.isDirectory() || this.__file_types.length == 0){
            return true;
        }
        
        extension = this._getExtension(file);
        for(int index = 0,end_index = this.__file_types.length - 1;index <= end_index;index++){
            if(this.__file_types[index].toLowerCase().equals(extension)){
                return true;
            }
        }
        
        return false;
    }

    @Override public String getDescription(){
        StringBuffer description = null;
        
        
        description = new StringBuffer();
        
        switch(this.__file_types.length){
            case 0:
                description.append("ALL");
                
                break;
            case 1:
                description.append(this.__file_types[0].toUpperCase());
                
                break;
            case 2:
                description.append(this.__file_types[0].toUpperCase());
                description.append(" and ");
                description.append(this.__file_types[1].toUpperCase());
                
                break;
           default:
               for(int index = 0,end_index = this.__file_types.length - 1;index <= end_index;index++){
                   if(index == end_index){
                       description.append(" and ");
                   }else{
                       description.append(" ");
                   }
                   
                   description.append(this.__file_types[index].toUpperCase());
                   
                   if(index < end_index){
                       description.append(",");
                   }
               }
               
               break;
        }
        
        description.append(" Files");
        return description.toString();
    }

    protected String _getExtension(File file){
        String extension = null;
        int    period_index = 0;
        
        
        
        extension = file.getName();
        period_index = extension.indexOf('.');
        if(period_index == -1){
            return "";
        }
        extension = extension.substring(period_index + 1).toLowerCase();
        
        return extension;
    }
    /*End Other Methods*/
}

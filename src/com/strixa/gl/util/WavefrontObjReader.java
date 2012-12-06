/**
 * File:  BlendReader.java
 * Date of Creation:  Sep 24, 2012
 */
package com.strixa.gl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.strixa.gl.Strixa3DElement;
import com.strixa.gl.StrixaMaterial;
import com.strixa.gl.StrixaPolygon;
import com.strixa.util.FileIO;
import com.strixa.util.Log;
import com.strixa.util.Point3D;


/**
 * Reads in a wavefront .obj file.
 *
 * @author Nicholas Rogé
 */
public class WavefrontObjReader implements Runnable{
    /**
     * Defines commands to be interpreted in a .obj or .mtl file.
     *
     * @author Nicholas Rogé
     */
	private enum Command{
	    DEFINE_AMBIENT_COLOR("Ka"),
	    DEFINE_AMBIENT_TEXTURE("map_Ka"),
	    DEFINE_DIFFUSE_COLOR("Kd"),
	    DEFINE_DIFFUSE_TEXTURE("map_Kd"),
	    DEFINE_FACE("f"),
	    DEFINE_ILLUMINATION_MODEL("illum"),
	    DEFINE_MATERIAL("newmtl"),
		DEFINE_NORMAL_VERTEX("vn"),
		DEFINE_OBJECT("o"),
		DEFINE_SPECULAR_COEFFICIENT("Ns"),
		DEFINE_SPECULAR_COEFFICIENT_TEXTURE("map_Ns"),
		DEFINE_SPECULAR_COLOR("Ks"),
		DEFINE_SPECULAR_TEXTURE("map_Ks"),
		DEFINE_TEXTURE_TRANSPARENCY("map_d"),
		DEFINE_TEXTURE_VERTEX("vt"),
		DEFINE_TRANSPARENCY("d"),
		DEFINE_VERTEX("v"),
		READ_MATERIAL_LIBRARY("mtllib"),
		USE_MATERIAL("usemtl");
		
		
	    /**
	     * Contains a valid {@link Command} and a set of related parameters.
	     *
	     * @author Nicholas Rogé
	     */
		private static class CommandObject{
			private Command   __command;
			private String[]  __parameters;
			private int       __parameter_count;
			
			
			/*Begin Constructors*/
			public CommandObject(Command command,String[] parameters){
				this.__command = command;
				this.__parameters = parameters;
				
				if(parameters == null){
				    this.__parameter_count = 0;
				}else{
				    this.__parameter_count = parameters.length;
				}
			}
			/*End Constructors*/
			
			/*Begin Getter/Setter Methods*/
			public Command getCommand(){
				return this.__command;
			}
			
			public String[] getParameters(){
				return this.__parameters;
			}
			
			public int getParameterCount(){
			    return this.__parameter_count;
			}
			/*End Getter/Setter Methods*/
		}
		
		private String __command_name;
		
		/*Begin Constructors*/
		private Command(String command_name){
			if(command_name == null || command_name.isEmpty()){
				throw new IllegalArgumentException();
			}
			
			this.__command_name = command_name;
		}
		/*End Constructors*/
		
		/*Begin Getter/Setter Methods*/
		public String getName(){
			return this.__command_name;
		}
		/*End Getter/Setter Methods*/
		
		/*Begin Static Methods*/
		/**
		 * Gets a {@link CommandObject} from the specified string.
		 * 
		 * @param command_string String containing a command.
		 * 
		 * @return Returns an instantiated {@link CommandObject} if a successful command is found, and null, otherwise.
		 */
		public static CommandObject getCOFromString(String command_string){
			final Command[]     values = Command.values();
			
			Command           command = null;
			ArrayList<String> command_parameter_buffer = null;
			String[]          command_parameters = null;
			CommandObject     command_object = null;
			String            name = null;
			
			
			command_string = command_string.trim();
			if(!command_string.isEmpty()){
				if(command_string.indexOf(' ') == -1){
					name = command_string;
					
					command_parameters = null;
				}else{
					name = command_string.substring(0,command_string.indexOf(' '));
					
					command_parameters = command_string.substring(command_string.indexOf(' ') + 1,command_string.length()).split(" ");
					if(command_parameters.length > 0){
					    command_parameter_buffer = new ArrayList<String>();
					    for(int parameter_index = 0;parameter_index < command_parameters.length;parameter_index++){
	                        if(!command_parameters[parameter_index].trim().isEmpty()){
	                            command_parameter_buffer.add(command_parameters[parameter_index]); 
	                        }
	                    }
					    
					    if(command_parameter_buffer.size() == 0){
					        command_parameters = null;
					    }else{
					        command_parameters = command_parameter_buffer.toArray(new String[command_parameter_buffer.size()]);
					    }
					}else{
					    command_parameters = null;
					}
				}
				
				//Find teh correct command
				for(int value_index = 0,end_index = values.length - 1;value_index <= end_index;value_index++){
					if(name.equals(values[value_index].getName())){
						command = values[value_index];
						
						break;
					}
				}
				
				if(command != null){
					command_object = new CommandObject(command,command_parameters);
				}
			}
			
			return command_object;
		}
		/*End Static Methods*/
	}
	
    private final List<PercentLoadedUpdateListener> __percent_loaded_listeners = new ArrayList<PercentLoadedUpdateListener>();
    private final Thread                            __read_thread = new Thread(this,"WavefrontReader_read_thread");
    
    private String                __file_location;
    private boolean               __file_read;
    private List<Strixa3DElement> __objects;
    private double                __update_step;
    
    
    /*Begin Constructor*/
    /**
     * Constructs an object to read in the given file.
     * 
     * @param file_location Location where the Wavefront OBJ file is stored.
     */
    public WavefrontObjReader(String file_location){
        this(file_location,1);
    }
    
    /**
     * Constructs an object to read in the given file.
     * 
     * @param file_location Location where the Wavefront OBJ file is stored.
     * @param update_step Amount of points the file load percentage must increase for this object's listeners to be updated again.
     */
    public WavefrontObjReader(String file_location,double update_step){
        if(file_location == null || file_location.equals("")){
            throw new IllegalArgumentException("Argument 'file_location' must not be null or empty.");
        }
        
        this.__file_location = file_location;
        this.__file_read = false;
        this.__update_step = update_step;
    }
    /*End Constructor*/
    
    /*Begin Other Essential Methods*/
    /**
     * Adds a {@link PercentLoadedUpdateListener} be notified when this object needs to send out updates.
     * 
     * @param listener Listener requesting to be notified.
     */
    public void addPercentLoadedUpdateListener(PercentLoadedUpdateListener listener){
        if(!this.__percent_loaded_listeners.contains(listener)){
            this.__percent_loaded_listeners.add(listener);
        }
    }
    
    protected void _alertPercentLoadedUpdateListeners(double amount_loaded){
        for(int index = 0;index < this.__percent_loaded_listeners.size();index++){
            this.__percent_loaded_listeners.get(index).onPercentLoadedUpdate(amount_loaded);
        }
    }
    
    /**
     * Notifies this object that it should start reading from the requested file.<br />
     * <strong>Note:</strong>  This is a threaded, nonblocking method.
     */
    public void read(){
        this.__read_thread.start();
    }
    
    protected void _readMtl(String file_location){
        Command.CommandObject command = null;
        FileInputStream       file_stream = null;
        String                line = null;
        int                   line_number = 0;
        StrixaMaterial        current_material = null;
        File                  mtl_file_handle = null;
        
        
        try{
            mtl_file_handle = new File(file_location);
            file_stream = new FileInputStream(mtl_file_handle);
            
            
            while((line = FileIO.readLine(file_stream)) != null){
                line_number++;
                
                if(line.isEmpty() || line.charAt(0) == '#'){
                    continue;  //Skip empty lines and comments
                }
                
                command = Command.getCOFromString(line);
                if(command == null){
                    System.out.println("Cannot handle input.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                    
                    continue;
                }
                
                //Begin processing the arguments
                switch(command.getCommand()){
                    case DEFINE_MATERIAL:
                        {
                            String material_name = null;
                            
                            
                            if(command.getParameterCount() > 1){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            if(command.getParameterCount() == 0){
                                material_name = "default";
                            }else{
                                material_name = command.getParameters()[0];
                            }
                            current_material = new StrixaMaterial(material_name);
                        }
                        break;
                    case DEFINE_AMBIENT_COLOR:
                        {
                            if(command.getParameterCount() != 3){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            current_material.setAmbientColor(new float[]{
                                Float.parseFloat(command.getParameters()[0]),
                                Float.parseFloat(command.getParameters()[1]),
                                Float.parseFloat(command.getParameters()[2])
                            });
                        }
                        break;
                    case DEFINE_DIFFUSE_COLOR:
                        {
                            if(command.getParameterCount() != 3){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            current_material.setDiffuseColor(new float[]{
                                Float.parseFloat(command.getParameters()[0]),
                                Float.parseFloat(command.getParameters()[1]),
                                Float.parseFloat(command.getParameters()[2])
                            });
                        }
                        break;
                    case DEFINE_SPECULAR_COLOR:
                        {
                            if(command.getParameterCount() != 3){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            current_material.setSpecularColor(new float[]{
                                Float.parseFloat(command.getParameters()[0]),
                                Float.parseFloat(command.getParameters()[1]),
                                Float.parseFloat(command.getParameters()[2])
                            });
                        }
                        break;
                    case DEFINE_SPECULAR_COEFFICIENT:
                        {
                            if(command.getParameterCount() != 1){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            current_material.setSpecularCoefficient(Float.parseFloat(command.getParameters()[0]));
                        }
                        break;
                    case DEFINE_TRANSPARENCY:
                        {
                            if(command.getParameterCount() != 1){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            current_material.setAlpha(Float.parseFloat(command.getParameters()[0]));
                        }
                        break;
                    case DEFINE_ILLUMINATION_MODEL:
                        if(command.getParameterCount() != 1){
                            throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                        }
                        
                        //material.setIllumnationType();  //TODO_HIGH:  Decide how to implement this.
                        break;
                    case DEFINE_DIFFUSE_TEXTURE:
                        {
                            File texture_file = null;
                            
                            
                            if(command.getParameterCount() != 1){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + mtl_file_handle.getAbsolutePath());
                            }
                            
                            
                            texture_file = new File(command.getParameters()[0]);
                            if(!texture_file.isAbsolute()){
                                texture_file = new File(mtl_file_handle.getParentFile(),command.getParameters()[0]);
                            }
                            current_material.setTexture(texture_file.getAbsolutePath());
                        }
                        break;
                    default:
                        System.out.println("That command is invalid in the current context.");
                        break;
                }
            }
        }catch(FileNotFoundException e){
            throw new RuntimeException("No such file was found in the given path:  "+this.__file_location);
        }catch(IOException e){
            RuntimeException exception = null; 
            
            
            exception = new RuntimeException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;  //We have to turn any IOExceptions into RuntimeExceptions 
        }finally{
            try{
                file_stream.close();
            }catch(IOException e){
                throw new RuntimeException("Could not close file properly.");
            }
        }
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
            throw new RuntimeException("This object does not have the given PercentLoadedUpdateListener registered to it.");
        }
    }
    
    public void run(){
        //Well this is just a clusterfuck of nasty.  TODO_HIGH:  Revamp this.
    	Command.CommandObject command = null;
    	Point3D<Double>       coordinates = null;
        Strixa3DElement       current_element = null;
        FileInputStream       file_stream = null;
        double                last_update = 0;
        String                line = null;
        int                   line_number = 0;
        List<Vertex>          normal_vertices = null;
        File                  obj_file_handle = null;
        double                percent_loaded = 0;
        List<Vertex>          texture_vertices = null;
        double                total_bytes = 0;
        List<Vertex>          vertices = null;
        
        
        try{
            obj_file_handle = new File(this.__file_location);
            file_stream = new FileInputStream(obj_file_handle);
            total_bytes = file_stream.available();
            
            vertices = new ArrayList<Vertex>(1000);
            normal_vertices = new ArrayList<Vertex>(1000);
            texture_vertices = new ArrayList<Vertex>(1000);
            this.__objects = new ArrayList<Strixa3DElement>(100);
            
            while((line = FileIO.readLine(file_stream)) != null){
                line_number++;
                
                if(line.trim().isEmpty() || line.charAt(0) == '#'){
                    continue;  //Skip empty lines and comments
                }
                
                
                command = Command.getCOFromString(line);
                if(command == null){
                	System.out.println("Cannot handle input.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                	
                	continue;
                }
                
                //Begin processing the arguments
                switch(command.getCommand()){
                	case READ_MATERIAL_LIBRARY:
                	    {
                	        File mtl_file_handle = null;
                	        
                	        
                    		if(command.getParameterCount() > 1){
                    		    throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                    		}
                    		
                            mtl_file_handle = new File(command.getParameters()[0]);
                            if(!mtl_file_handle.isAbsolute()){
                                mtl_file_handle = new File(obj_file_handle.getParentFile(),command.getParameters()[0]);
                            }
                            this._readMtl(mtl_file_handle.getAbsolutePath());
                	    }
                		break;
                	case DEFINE_OBJECT:
                	    {
                    	    if(command.getParameterCount() > 1){
                    	        throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                    	    }
                    	    
                    	    if(command.getParameterCount() == 1){
                    	        //TODO_HIGH:  current_object = new Strixa3DElement(command.getParameters()[0]);
                    	        current_element = new Strixa3DElement();
                    	    }else{
                    	        current_element = new Strixa3DElement();
                    	    }                    	    
                    	    this.__objects.add(current_element);
                    	    
                    	    coordinates = current_element.getCoordinates();
                	    }
                	    break;
                	case DEFINE_VERTEX:
                	    {
                	        boolean coordinates_changed = false;
                	        Vertex vertex = null;
                    	    double x = 0;
                    	    double y = 0;
                    	    double z = 0;
                    	    double w = 1;
                    	    double x_delta = 0;
                    	    double y_delta = 0;
                    	    double z_delta = 0;
                    	    
                    	    
                    	    if(command.getParameterCount() < 2 || command.getParameterCount() > 3){
                    	        throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                    	    }
                    	    
                    	    x = Double.parseDouble(command.getParameters()[0]);
                    	    y = Double.parseDouble(command.getParameters()[1]);
                    	    if(command.getParameterCount() > 2){
                    	        z = Double.parseDouble(command.getParameters()[2]);
                    	        if(command.getParameterCount() > 3){
                    	            w = Double.parseDouble(command.getParameters()[3]);
                    	        }
                    	    }
                    	    
                    	    if(vertices.isEmpty()){
                    	        coordinates.setPoint(x,y,z);
                    	        
                    	        x = 0;
                    	        y = 0;
                    	        z = 0;
                    	    }else{
                    	        if(x < coordinates.getX()){
                    	            x_delta = coordinates.getX() - x;
                    	            coordinates.setX(x);
                    	            x = 0;
                    	            
                    	            coordinates_changed = true;
                    	        }else{
                    	            x -= coordinates.getX();
                    	        }
                    	        if(y < coordinates.getY()){
                                    y_delta = coordinates.getY() - y;
                                    coordinates.setY(y);
                                    y = 0;
                                    
                                    coordinates_changed = true;
                                }else{
                                    y -= coordinates.getY();
                                }
                    	        if(z < coordinates.getZ()){
                                    z_delta = coordinates.getZ() - z;
                                    coordinates.setZ(z);
                                    z = 0;
                                    
                                    coordinates_changed = true;
                                }else{
                                    z -= coordinates.getZ();
                                }
                    	        
                    	        if(coordinates_changed){
                    	            for(int index = 0,end_index = vertices.size() - 1;index <= end_index;index++){
                    	                vertex = vertices.get(index);
                    	                vertex.setPoint(
                    	                    vertex.getX() + x_delta,
                    	                    vertex.getY() + y_delta,
                    	                    vertex.getZ() + z_delta
                    	                );
                    	            }
                    	        }
                    	    }
                    	    
                    	    vertices.add(new Vertex(x,y,z,w));
                	    }
                    	break;
                	case DEFINE_TEXTURE_VERTEX:
                	    {
                    	    double u = 0;  //lulz.  double u...  w...  Get it?  
                    	    double v = 0;
                    	    double w = 1;  //A double double u?  That's syntactically incorrect!
                    	    
                    	    
                    	    if(command.getParameterCount() < 1 || command.getParameterCount() > 3){
                    	        throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                    	    }
                    	    
                    	    u = Double.parseDouble(command.getParameters()[0]);
                    	    if(command.getParameterCount() > 1){
                    	        v = Double.parseDouble(command.getParameters()[1]);
                    	        if(command.getParameterCount() > 2){  //In other words 3...  
                    	            w = Double.parseDouble(command.getParameters()[2]);
                    	        }
                    	    }
                    	    
                    	    texture_vertices.add(new Vertex(u,v,0,w));
                	    }
                	    break;
                	case DEFINE_NORMAL_VERTEX:
                	    {
                    	    if(command.getParameterCount() != 3){
                    	        throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                    	    }
                    	    
                    	    normal_vertices.add(new Vertex(
                	            Double.parseDouble(command.getParameters()[0]),
                	            Double.parseDouble(command.getParameters()[1]),
                	            Double.parseDouble(command.getParameters()[2]),
                	            0
                    	    ));
                	    }
                	    break;
                	case USE_MATERIAL:
                	    {
                    	    StrixaMaterial material = null;
                    	    String         material_name = null;
                    	    
                    	    
                    	    if(command.getParameterCount() > 1){
                                throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                            }
                    	    
                    	    if(command.getParameterCount() == 0){
                                material_name = "default";
                            }else{
                                material_name = command.getParameters()[0];
                            }
                            
                            material = StrixaMaterial.getMaterialByName(material_name);
                            if(material == null){
                                System.out.println("Warning:  Material with name " + line + " could not be found.  Line number " + line_number + " in " + obj_file_handle.getAbsolutePath());
                            }else{
                                current_element.setMaterial(material);
                            }
                	    }
                        break;
                	case DEFINE_FACE:
                	    {
                    	    String[]      explosion = null;
                    	    StrixaPolygon polygon = null;
                    	    
                    	    
                    	    if(command.getParameterCount() < 3){  //A face must have at least 3 points
                    	        throw new IOException("Invalid number of arguments given.  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                            }
                    	    
                    	    polygon = new StrixaPolygon();
                    	    for(int parameter_index = 0,parameter_end_index = command.getParameterCount() - 1;parameter_index <= parameter_end_index;parameter_index++){
                    	        try{
                    	            explosion = command.getParameters()[parameter_index].split("/");
                    	            
                    	            polygon.addPoint(vertices.get(Integer.parseInt(explosion[0]) - 1));
                    	            if(explosion.length > 1){
                    	                if(!explosion[1].isEmpty()){
                    	                    polygon.addTexturePoint(texture_vertices.get(Integer.parseInt(explosion[1]) - 1));
                    	                }
                    	                
                    	                if(explosion.length > 2){
                    	                    if(!explosion[2].isEmpty()){
                                                polygon.addNormalPoint(normal_vertices.get(Integer.parseInt(explosion[2]) - 1));
                                            }
                    	                }
                    	            }
                    	        }catch(IndexOutOfBoundsException e){
                                    throw new RuntimeException("Given vertex was not found!  Line number:  " + line_number + " in " + obj_file_handle.getAbsolutePath());
                                }
                    	    }
                            
                    	    current_element.addComponent(polygon);
                	    }
                	    break;
                	default:
                	    System.out.println("That command is invalid in the current context.");
                	    break;
                }
                
                
                percent_loaded = ((total_bytes - file_stream.available()) / total_bytes) * 100;
                if((percent_loaded - last_update) > this.__update_step && percent_loaded < 100){  //We want to reserve the 100% loaded update for when this method completes its run
                    this._alertPercentLoadedUpdateListeners(percent_loaded);
                    
                    last_update = percent_loaded;
                }
            }
        }catch(FileNotFoundException e){
            Log.logEvent(Log.Type.ERROR,"No such file was found in the given path:  " + obj_file_handle.getAbsolutePath());
        }catch(IOException e){
            RuntimeException exception = null; 
            
            
            exception = new RuntimeException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;  //We have to turn any IOExceptions into RuntimeExceptions 
        }finally{
            try{
                file_stream.close();
            }catch(IOException e){
                throw new RuntimeException("Could not close file properly.");
            }
        }
        
        this.__file_read = true;
        this._alertPercentLoadedUpdateListeners(100);
    }
    
    /**
     * Gets the {@link Strixa3DElements} that were read in using this tool.
     * 
     * @return The {@link Strixa3DElements} that were read in using this tool, or null, if this method is called before the {@link WavefrontObjReader#read} method.
     */
    public Strixa3DElement[] getElements(){
        if(!this.__file_read){
            Log.logEvent(Log.Type.WARNING,"You must first call the read method on this object before attempting to retrive it's elements.");
            
            return null;
        }
        
        return this.__objects.toArray(new Strixa3DElement[this.__objects.size()]);
    }
}

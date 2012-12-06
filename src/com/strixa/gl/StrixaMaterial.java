/**
 * File:  StrixaMaterial.java
 * Date of Creation:  Oct 15, 2012
 */
package com.strixa.gl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Describes the material a StrixaElement is made of.
 *
 * @author Nicholas Rogé
 */
public class StrixaMaterial{
	private static final Map<String,StrixaMaterial> __material_map = new HashMap<String,StrixaMaterial>(); 
	
    Float                 __alpha;
    float[]               __ambient_color;
    float[]               __diffuse_color;
    float[]               __emission_color;
    String                __name;
    float[]               __specular_color;
    float                 __specular_coefficient;
    Texture               __texture;
    String                __texture_file_location;
    
    
    {
        this.__alpha = 1f;
        this.__ambient_color = new float[]{0f,0f,0f,1f};
        this.__diffuse_color = new float[]{1f,1f,1f,1f};
        this.__emission_color = new float[]{0f,0f,0f,1f};
        this.__specular_color = new float[]{1f,1f,1f,1f};
        this.__specular_coefficient = 0f;
        this.__texture = null;
        this.__texture_file_location = null;
    }
    /*Begin Constructors*/
    /**
     * Constructs a new anonymous material.  This material will not be able to be retrieved using {@link StrixaMaterial#getMaterialByName(String)} unless you register it manually.
     */
    public StrixaMaterial(){
    	this.__name = "";
    }
    
    /**
     * Constructs a material with the given name, allowing it to be retrieved in the future using {@link StrixaMaterial#getMaterialByName(String)}.  If a material with the given material name exists already, you will have to manually call the registration method with the third parameter as true to register this material.
     * 
     * @param material_name Name this material should have.
     */
    public StrixaMaterial(String material_name){
        if(material_name == null || material_name.trim().isEmpty()){
            throw new NullPointerException("Argument 'material_name' must not be null or empty.");
        }
        
        this.__name = material_name;
        if(!StrixaMaterial.registerMaterial(material_name,this,false)){
        	System.out.println("Could not register material.  A material with that name already exists.");
        }
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets the material's ambient color. 
     * 
     * @return The material's ambient color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     */
    public float[] getAmbientColor(){
        return this.__ambient_color;
    }
    
    /**
     * Gets the materia's alpha value. 
     * 
     * @return The material's alpha value.  A value of 1 indicates a fully opaque material, and a value of 0 indicates a fully transparent material.
     */
    public float getAlpha(){
        return this.__alpha;
    }
    
    /**
     * Gets the material's diffuse color. 
     * 
     * @return The material's diffuse color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     */
    public float[] getDiffuseColor(){
        return this.__diffuse_color;
    }
    
    /**
     * Gets the material's emission color.
     * 
     * @return The material's emission color.
     */
    public float[] getEmissionColor(){
        return this.__emission_color;
    }
    
    /**
     * Gets this material's name.
     * 
     * @return A string representing this materials name.
     */
    public String getName(){
        return this.__name;
    }
    
    /**
     * Gets the material's specular color. 
     * 
     * @return The material's specular color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     */
    public float[] getSpecularColor(){
        return this.__specular_color;
    }
    
    /**
     * Gets this material's specular coefficient.
     * 
     * @return The material's specular coefficient.  This will be a value between 0 and 1000 which represents this material's "shininess".
     */
    public float getSpecularCoefficient(){
        return this.__specular_coefficient;
    }
    
    /**
     * Gets the texture currently registered to this material.
     * 
     * @return The texture currently assigned to this material.  This may be null if no texture has been assigned to this material.
     */
    public Texture getTexture(){
        return this.__texture;
    }
    
    /**
     * Sets the material's ambient color. 
     * 
     * @param color The material's ambient color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if:
     * <ul>
     *     <li>Argument 'color' is null.</li>
     *     <li>Argument 'color' does not have exactly 3 elements.</li>
     *     <li>Any of the given colors value's are less than 0 or greater than 1.</li>
     * </ul>
     */
    public void setAmbientColor(float[] color){
        if(color == null || color.length != 3){
            throw new IllegalArgumentException();
        }
        
        this.setAmbientColor(color[0],color[1],color[2]);
    }
    
    /**
     * Sets the material's ambient color.
     * 
     * @param red Red component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param green Green component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param blue Blue component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if any of the given colors value's are less than 0 or greater than 1.
     */
    public void setAmbientColor(float red,float green,float blue){
        if(
            red < 0 || red > 1
            ||
            green < 0 || green > 1
            ||
            blue < 0 || blue > 1
        ){
            throw new IllegalArgumentException();
        }
        
        this.__ambient_color[0] = red;
        this.__ambient_color[1] = green;
        this.__ambient_color[2] = blue;
    }
    
    /**
     * Sets this material's alpha value.
     * 
     * @param alpha The material's alpha value.  A value of 1 indicates a fully opaque material, and a value of 0 indicates a fully transparent material.
     * 
     * @throws IllegalArgumentException Will be thrown if the 'alpha' argument is less than 0 or greater than 1.
     */
    public void setAlpha(float alpha){
        if(alpha < 0 || alpha > 1){
            throw new IllegalArgumentException();
        }
        
        this.__alpha = alpha;
        
        this.__ambient_color[3] = alpha;
        this.__diffuse_color[3] = alpha;
        this.__emission_color[3] = alpha;
        this.__specular_color[3] = alpha;
    }
    
    /**
     * Sets the material's diffuse color. 
     * 
     * @param color The material's diffuse color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if:
     * <ul>
     *     <li>Argument 'color' is null.</li>
     *     <li>Argument 'color' does not have exactly 3 elements.</li>
     *     <li>Any of the given colors value's are less than 0 or greater than 1.</li>
     * </ul>
     */
    public void setDiffuseColor(float[] color){
        if(color == null || color.length != 3){
            throw new IllegalArgumentException();
        }
        
        this.setDiffuseColor(color[0],color[1],color[2]);
    }
    
    /**
     * Sets the material's diffuse color.
     * 
     * @param red Red component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param green Green component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param blue Blue component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if any of the given colors value's are less than 0 or greater than 1.
     */
    public void setDiffuseColor(float red,float green,float blue){
        if(
            red < 0 || red > 1
            ||
            green < 0 || green > 1
            ||
            blue < 0 || blue > 1
        ){
            throw new IllegalArgumentException();
        }
        
        this.__diffuse_color[0] = red;
        this.__diffuse_color[1] = green;
        this.__diffuse_color[2] = blue;
    }
    
    /**
     * Sets the material's emission color. 
     * 
     * @param color The material's emission color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if:
     * <ul>
     *     <li>Argument 'color' is null.</li>
     *     <li>Argument 'color' does not have exactly 3 elements.</li>
     *     <li>Any of the given colors value's are less than 0 or greater than 1.</li>
     * </ul>
     */
    public void setEmissionColor(float[] color){
        if(color == null || color.length != 3){
            throw new IllegalArgumentException();
        }
        
        this.setEmissionColor(color[0],color[1],color[2]);
    }
    
    /**
     * Sets the material's emission color.
     * 
     * @param red Red component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param green Green component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param blue Blue component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if any of the given colors value's are less than 0 or greater than 1.
     */
    public void setEmissionColor(float red,float green,float blue){
        if(
            red < 0 || red > 1
            ||
            green < 0 || green > 1
            ||
            blue < 0 || blue > 1
        ){
            throw new IllegalArgumentException();
        }
        
        this.__emission_color[0] = red;
        this.__emission_color[1] = green;
        this.__emission_color[2] = blue;
    }
    
    /**
     * Sets the material's specular color. 
     * 
     * @param color The material's specular color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if:
     * <ul>
     *     <li>Argument 'color' is null.</li>
     *     <li>Argument 'color' does not have exactly 3 elements.</li>
     *     <li>Any of the given colors value's are less than 0 or greater than 1.</li>
     * </ul>
     */
    public void setSpecularColor(float[] color){
        if(color == null || color.length != 3){
            throw new IllegalArgumentException();
        }
        
        this.setSpecularColor(color[0],color[1],color[2]);
    }
    
    /**
     * Sets the material's specular color.
     * 
     * @param red Red component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param green Green component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param blue Blue component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if any of the given colors value's are less than 0 or greater than 1.
     */
    public void setSpecularColor(float red,float green,float blue){
        if(
            red < 0 || red > 1
            ||
            green < 0 || green > 1
            ||
            blue < 0 || blue > 1
        ){
            throw new IllegalArgumentException();
        }
        
        this.__specular_color[0] = red;
        this.__specular_color[1] = green;
        this.__specular_color[2] = blue;
    }
    
    /**
     * Sets this material's specular coefficient.
     * 
     * @param specular_coefficient The material's specular coefficient.  This will be a value between 0 and 1000 which represents this material's "shininess".
     */
    public void setSpecularCoefficient(float specular_coefficient){
        this.__specular_coefficient = specular_coefficient;
    }
    
    /**
     * Sets the location for the texture of this material.  Using this method, you must call {@link StrixaMaterial#loadTexture()} from a later point in the program.
     * 
     * @param file_location Location of the image for this material's texture.
     */
    public void setTexture(String file_location){    
        this.__texture_file_location = file_location;
    }
    
    /**
     * Sets this material's texture.
     * 
     * @param texture Texture the material should take on.
     */
    public void setTexture(Texture texture){
        this.__texture = texture;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    /**
     * Check to determine whether this material has a texture or not.
     * <strong>Note:</strong>  Just because an this method returns true does not necessarily mean the texture has been loaded into memory.
     * 
     * @return Returns true, if this material has been assigned a texture, or false, otherwise.
     */
    public boolean hasTexture(){
    	if(this.__texture != null || this.__texture_file_location != null){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Check to determine whether this material's texture has been loaded into memory or not.
     * 
     * @return Returns true if the texture is loaded and available to be used, and false, otherwise.
     */
    public boolean isTextureLoaded(){
        if(this.__texture  == null){
            return false;
        }else{
            return true;
        }
    }
    
    /**
     * Loads the requested texture into memory.
     */
    public void loadTexture() throws IOException{
        GL2 gl = null;
        
        
        if(this.__texture_file_location == null){
            throw new IOException("You must call either loadTexture(String) or setTexture before attempting to call this method.");
        }
        
        
        try{
            gl = GLContext.getCurrentGL().getGL2();
        }catch(GLException e){
            throw new RuntimeException("This method must be called from a thread with an active GLContext.");
        }
        
        if(this.__texture != null){
            this.__texture.destroy(gl);
        }
        this.__texture = TextureIO.newTexture(new File(this.__texture_file_location),false);
        this.__texture.setTexParameteri(gl,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
        this.__texture.setTexParameteri(gl,GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
    }
    
    /**
     * Loads the requested texture into memory.
     * 
     * @param file_location Path to the image for the texture that should be loaded into memory.
     */
    public void loadTexture(String file_location) throws IOException{
        this.setTexture(file_location);
        
        this.loadTexture();
    }
    /*End Other Methods*/
    
    /*Begin Static Methods*/
    /**
     * Retrieves a given material based on its name.
     * 
     * @param material_name Name of the material to be retrieved.
     * 
     * @return Material whose name matches the argument given.
     */
    public static StrixaMaterial getMaterialByName(String material_name){
    	return StrixaMaterial.__material_map.get(material_name);
    }
    
    /**
     * Registers a material to be eligible to be retrieved using the {@link StrixaMaterial#getMaterialByName(String)} method.
     * 
     * @param material Material to register.
     * 
     * @throws NullPointerException Thrown if the given material has no name.
     */
    public static boolean registerMaterial(String material_name,StrixaMaterial material,boolean overwrite_existing){
    	if(material == null || material.getName().isEmpty()){
    		throw new NullPointerException("Argument 'material_name' must not be null or empty.");
    	}
    	
    	if(StrixaMaterial.__material_map.containsKey(material_name) && !overwrite_existing){
    		return false;
    	}else{
    		StrixaMaterial.__material_map.put(material.getName(),material);
    		
    		return true;
    	}
    }
    /*End Static Methods*/
}

/**
 * File:  vertex3D.java
 * Date of Creation:  Jul 23, 2012
 */
package com.strixa.gl.util;

import com.strixa.util.Point3D;

/**
 * Generic vertex class which allows for any type of numeric to be used.
 *
 * @author Nicholas Rogé
 */
public class Vertex extends Point3D<Double>{
    private final float[] __color = new float[]{.8f,.8f,.8f,1};
    
    private double __weight;
    
    
    /*Begin Constructors*/
    /**
     * Constructs a vertex whose coordinates are at the given vertices and has the given weight.
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     * @param weight Weight parameter.
     */
    public Vertex(double x,double y,double z,double weight){
        super(x,y,z);
        this.__weight = weight;
    }
    
    /**
     * Constructs a copy of the given Vertex.
     * 
     * @param copy Vertex whose data should be copied.
     */
    public Vertex(Vertex copy){
        super(copy.getX(),copy.getY(),copy.getZ());
        this.__weight = copy.getWeight();
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets the point's color.
     * 
     * @return The point's color with the alpha value added in as the fourth element.
     */
    public float[] getColor(){
        float[] copy = new float[4];
        
        copy[0] = this.__color[0];
        copy[1] = this.__color[1];
        copy[2] = this.__color[2];
        copy[3] = this.__color[3];
        
        return copy;
    }
    
    /**
     * Gets this vertex's weight.
     * 
     * @return This vertex's weight.
     */
    public double getWeight(){
        return this.__weight;
    }
    
    /**
     * Sets the point's color. 
     * 
     * @param color The point's color as a float array with length 3.  Each element represents a red, green, or blue component of the color where a value of 0 indicates no intensity, and 1 indicates full intensity.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if:
     * <ul>
     *     <li>Argument 'color' is null.</li>
     *     <li>Argument 'color' does not have exactly 3 elements.</li>
     *     <li>Any of the given colors value's are less than 0 or greater than 1.</li>
     * </ul>
     */
    public void setColor(float[] color){
        if(color == null || color.length != 3){
            throw new IllegalArgumentException();
        }
        
        this.setColor(color[0],color[1],color[2]);
    }
    
    /**
     * Sets the point's color.
     * 
     * @param red Red component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param green Green component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * @param blue Blue component of the color, where 0 indicates no intensity, and 1 indicates full intensity of the color.
     * 
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if any of the given colors value's are less than 0 or greater than 1.
     */
    public void setColor(float red,float green,float blue){
        if(
            red < 0 || red > 1
            ||
            green < 0 || green > 1
            ||
            blue < 0 || blue > 1
        ){
            throw new IllegalArgumentException();
        }
        
        this.__color[0] = red;
        this.__color[1] = green;
        this.__color[2] = blue;
    }
    
    /**
     * Sets this vertex's coordinates and weight.
     * 
     * @param x X coordinate of the vertex.
     * @param y Y coordinate of the vertex.
     * @param z Z coordinate of the vertex.
     * @param weight Weight parameter.
     */
    public void setData(double x,double y,double z,double weight){
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setWeight(weight);
    }
    
    /**
     * Sets this weight.
     * 
     * @param weight This vertex's weight.
     */
    public void setWeight(double weight){
        this.__weight = weight;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    /**
     * Performs a check to see if the given vertex is at the same location as this one.
     * 
     * @param vertex vertex to compare against.
     * 
     * @return Returns true if the given vertex is at the same location as this one, and false, otherwise.
     */
    public boolean equals(Vertex vertex){
        if(
            super.equals(new Point3D<Double>(vertex.getX(),vertex.getY(),vertex.getZ()))
            &&
            this.getWeight() == vertex.getWeight()
        ){
            return true;
        }
        
        return false;
    }
    
    public String toString(){
        return "Vertex at location ("+this.getX()+","+this.getY()+","+this.getZ()+") with weight:  " + this.getWeight();
    }
    /*End Other Methods*/
}

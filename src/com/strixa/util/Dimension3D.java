/**
 * File:  Dimension.java
 * Date of Creation:  Jul 17, 2012
 */
package com.strixa.util;

/**
 * Describes an objects dimensions with the given numeric type using two dimensions:  width and height.  
 *
 * @author Nicholas Rogé
 */
public class Dimension3D<T extends Number> extends Dimension<T>{
    private T __depth;
    private T __height;
    private T __width;
    
    
    /*Begin Constructors*/
    public Dimension3D(Dimension3D<T> copy){
        this.__depth = copy.getDepth();
        this.__height = copy.getHeight();
        this.__width = copy.getWidth();
    }
    
    /**
     * Constructs a Dimension2D object using the given parameters as its width and height.
     * 
     * @param width Width of this Dimension.
     * @param height Height of this Dimension.
     * @param depth Depth of this Dimension.
     */
    public Dimension3D(T width,T height,T depth){
        this.__width = width;
        this.__height = height;
        this.__depth = depth;
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets this Dimension's depth.
     * 
     * @return This Dimension's depth.
     */
    public T getDepth(){
        return this.__depth;
    }
    
    /**
     * Gets this Dimension's height.
     * 
     * @return This Dimension's height.
     */
    public T getHeight(){
        return this.__height;
    }
    
    /**
     * Gets this Dimension's width.
     * 
     * @return This Dimension's width.
     */
    public T getWidth(){
        return this.__width;
    }
    
    /**
     * Sets this Dimension's width and height.
     * 
     * @param width New width this Dimension should take on.
     * @param height New height this Dimension should take on.
     * @param depth New depth this Dimension should take on.
     */
    public void setDimensions(T width,T height,T depth){
        this.__width = width;
        this.__height = height;
        this.__depth = depth;
    }
    
    /**
     * Sets this Dimension's height.
     * 
     * @param depth New depth this Dimension should take on.
     */
    public void setDepth(T depth){
        this.__depth = depth;
    }
    
    /**
     * Sets this Dimension's height.
     * 
     * @param height New height this Dimension should take on.
     */
    public void setHeight(T height){
        this.__height = height;
    }
    
    /**
     * Sets this Dimension's width.
     * 
     * @param width New width this Dimension should take on.
     */
    public void setWidth(T width){
        this.__width = width;
    }
    /*End Getter/Setter Methods*/
}

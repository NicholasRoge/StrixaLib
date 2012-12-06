/**
 * File:  Point3D.java
 * Date of Creation:  Jul 23, 2012
 */
package com.strixa.util;

/**
 * Generic point class which allows for any type of numeric to be used.
 *
 * @author Nicholas Rogé
 */
public class Point3D <T extends Number> extends Point<T>{
    private T __x;
    private T __y;
    private T __z;
    
    
    /*Begin Constructors*/
    /**
     * Constructs a Point whose coordinates are at the given points.
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate
     */
    public Point3D(T x,T y,T z){
        this.__x = x;
        this.__y = y;
        this.__z = z;
    }
    
    /**
     * Constructs a copy of the given Point.
     * 
     * @param copy Point whose data should be copied.
     */
    public Point3D(Point3D<T> copy){
        this.__x = copy.getX();
        this.__y = copy.getY();
        this.__z = copy.getZ();
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets this point's X coordinate.
     * 
     * @return This point's X coordinate.
     */
    public T getX(){
        return this.__x;
    }
    
    /**
     * Gets this point's Y coordinate.
     * 
     * @return This point's Y coordinate.
     */
    public T getY(){
        return this.__y;
    }
    
    /**
     * Gets this point's Z coordinate.
     * 
     * @return This point's Z coordinate.
     */
    public T getZ(){
        return this.__z;
    }
    
    /**
     * Sets this point's coordinates.
     * 
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @param z Z coordinate of the point.
     */
    public void setPoint(T x,T y,T z){
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }
    
    /**
     * Sets this point's X coordinate.
     * 
     * @param x X coordinate of the point.
     */
    public void setX(T x){
        this.__x = x;
    }
    
    /**
     * Sets this point's Y coordinate.
     * 
     * @param y Y coordinate of the point.
     */
    public void setY(T y){
        this.__y = y;
    }
    
    /**
     * Sets this point's Z coordinate.
     * 
     * @param z Z coordinate of the point.
     */
    public void setZ(T z){
        this.__z = z;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    /**
     * Performs a check to see if the given point is at the same location as this one.
     * 
     * @param point Point to compare against.
     * 
     * @return Returns true if the given point is at the same location as this one, and false, otherwise.
     */
    public boolean equals(Point3D<T> point){
        if(
            this.getX() == point.getX()
            &&
            this.getY() == point.getY()
            &&
            this.getZ() == point.getZ()
        ){
            return true;
        }
        
        return false;
    }
    
    public String toString(){
        return "("+this.getX()+","+this.getY()+","+this.getZ()+")";
    }
    /*End Other Methods*/
    
    /*Begin Static Methods*/
    
    /*End Static Methods*/
}

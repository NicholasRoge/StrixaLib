/**
 * File:  Observable.java
 * Date of Creation:  Nov 12, 2012
 */
package com.strixa.gl;

import com.strixa.util.Point3D;

/**
 * Interface which allows
 *
 * @author Nicholas Rogé
 */
public interface Observable{
    /**
     * Gets the position the camera should be relative to the object which has implemented the Observable class.
     * 
     * @return Camera position.
     */
    public Point3D<Double> getCameraPosition();
    
    /**
     * Boolean check to determine whether the observer can view the object being observed.
     * 
     * @return Method should return true if the user can view the object being observed, and false, otherwise.
     */
    public boolean isObserverViewable();
}

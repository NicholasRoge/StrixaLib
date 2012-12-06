/**
 * File:  StrixaGLContext.java
 * Date of Creation:  Jul 19, 2012
 */
package com.strixa.gl;

import com.strixa.gl.properties.Cuboid;

/**
 * Describes the current context of a running StrixaGL application.
 *
 * @author Nicholas Rogé
 */
public class StrixaGLContext{
    private Cuboid __viewable_area;
    
    
    /**
     * Returns a {@link Cuboid} which represents the maximum viewable area.
     * 
     * @return A {@link Cuboid} which represents the maximum viewable area.
     */
    public Cuboid getViewableArea(){
        return this.__viewable_area;
    }
    
    /**
     * Sets teh viewable area of this canvas.
     * 
     * @param viewable_area A {@link Cuboid} which represents the total viewable area of this canvas.
     */
    public void setViewableArea(Cuboid viewable_area){
        this.__viewable_area = viewable_area;
    }
}
//TODO:  Look at renaming the "viewable_area" portions of this class, as that's a bit of a misnomer.  Rather, these methods actually represent the total renderable area of the canvas.
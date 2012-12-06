/**
 * File:  Strixa2DCanvas.java
 * Date of Creation:  Jul 19, 2012
 */
package com.strixa.gl;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.glu.GLU;

import com.strixa.gl.properties.Cuboid;
import com.strixa.util.Point2D;
import com.strixa.util.Point3D;

/**
 * TODO:  Write Class Description
 *
 * @author Nicholas Rogé
 */
public abstract class Strixa2DCanvas extends StrixaGLCanvas implements MouseMotionListener{
    /** Field needed for the serialization of this object. */
    private static final long serialVersionUID = 7940290686156245285L;
    
    private final Point2D<Double>       __camera_location = new Point2D<Double>(0.0,0.0);
    
    private List<Strixa2DElement> __children;
    private double                __x_axis_units;
    private double                __y_axis_units;
    
    
    /*Begin Constructors*/
    /**
     * Constructs the object with the given capabilities.
     * 
     * @param capabilities Capabilities this canvas should have.
     * @param aspect_ratio Ratio of the width of this canvas, to it's height. (width/height)
     */
    public Strixa2DCanvas(GLCapabilities capabilities,double aspect_ratio){
        super(capabilities,aspect_ratio);
        
        
      //this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setXAxisUnits(100);
    }
    /*End Constructors*/
    
    /*Begin Overridden Methods*/    
    @Override public void reshape(GLAutoDrawable drawable,int x,int y,int width,int height){
        super.reshape(x,y,width,height);
        
        this.setAspectRatio((double)width/(double)height);
        this._refreshViewableArea();
    }
    /*End Overridden Methods*/
    
    /*Begin Getter/Setter Methods*/    
    /**
     * Gets this object's Strixa2DElement children.
     * 
     * @return This object's Strixa2DElement children.
     */
    public List<Strixa2DElement> getChildren(){
        if(this.__children==null){
            this.__children = new ArrayList<Strixa2DElement>();
        }
        
        return this.__children;
    }
    
    /**
     * Gets the number of units on the X axis. 
     * 
     * @return The number of units on the X axis.
     */
    public double getXAxisUnits(){
        return this.__x_axis_units;
    }
    
    /**
     * Sets the number of units on the X axis.  This method will also scale the Y axis units by the percent this axis is reduced or increased.
     * 
     * @param num_units The number of units which the X axis should contain.
     */
    public void setXAxisUnits(double num_units){
        this.setXAxisUnits(num_units,true);
    }
    
    /**
     * Sets the number of units on the X axis.
     * 
     * @param num_units The number of units which the X axis should contain.
     * @param adjust_y Should be true if you would like the Y axis to be scaled with this axis, or false, otherwise.
     */
    public void setXAxisUnits(double num_units,boolean adjust_y){
        this.__x_axis_units = num_units;
        if(adjust_y){
            this.__y_axis_units = num_units/this.getAspectRatio();
        }
        
        this._refreshViewableArea();
    }
    
    /**
     * Gets the number of units on the minor axis.  The minor axis is defined as the axis which has the lesser dimension. 
     * 
     * @return The number of units on the minor axis.
     */
    public double getYAxisUnits(){
        return this.__y_axis_units;
    }
    
    /**
     * Sets the number of units on the Y axis.  This method will also scale the X axis units by the percent this axis is reduced.
     * 
     * @param num_units The number of units which the Y axis should contain.
     */
    public void setYAxisUnits(double num_units){
        this.setYAxisUnits(num_units,true);
    }
    
    /**
     * Sets the number of units on the Y axis.
     * 
     * @param num_units The number of units which the minor axis should contain.
     * @param adjust_x Should be true if you would like the X axis to be scaled with this axis, or false, otherwise.
     */
    public void setYAxisUnits(double num_units,boolean adjust_x){        
        this.__y_axis_units = num_units;       
        if(adjust_x){
            this.__x_axis_units = num_units*this.getAspectRatio();
        }
        
        this._refreshViewableArea();
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Essential Methods*/
    /**
     * Adds a child to this canvas.
     * 
     * @param child Child to be added to the canvas.
     */
    public void addChild(Strixa2DElement child){
        final List<Strixa2DElement> children = this.getChildren();
        
        
        if(!children.contains(child)){
            children.add(child);
        }
    }
    
    protected void _drawChildren(GL2 gl){
        final List<Strixa2DElement> children = this.getChildren();
        final int                   child_count = children.size();
        final GLU                   glu = new GLU();
        
        
        if(child_count==0){
            return;
        }
        
        
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        glu.gluPerspective(
            90,
            this.getAspectRatio(),
            1,
            1000
        );
        glu.gluLookAt(
            //Where am I?
            this.__camera_location.getX(),
            this.__camera_location.getY(),
            0,
            //What am I looking at?
            this.__camera_location.getX(),
            this.__camera_location.getY(),
            0,
            //Which way is up?  
            0,
            1,
            0
        );
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        
        /*Draw the models!*/
        synchronized(children){
            for(int index = 0;index<child_count;index++){
                if(children.get(index).isVisible(this.getStrixaGLContext())){
                    gl.glPushMatrix();                    
                        children.get(index).draw(gl);
                    gl.glPopMatrix();
                }
            }
        }
        
        this.swapBuffers();
    }
    
    public void mouseDragged(MouseEvent event){
        System.out.println("Mouse Dragged");
    }
    
    public void mouseMoved(MouseEvent event){
        System.out.println("Mouse Moved");
    }
    
    /**
     * Updates the viewable area to the most recent dimensions.
     */
    protected void _refreshViewableArea(){
        this.getStrixaGLContext().setViewableArea(new Cuboid(
           new Point3D<Double>(
               this.__camera_location.getX() - (this.getXAxisUnits()/2),
               this.__camera_location.getY() - (this.getYAxisUnits()/2),
               0.0
           ),
           this.getXAxisUnits(),
           this.getYAxisUnits(),
           0.0
        ));
    }
    
    /**
     * Removes a child from this canvas.
     * 
     * @param child Child to be removed from the canvas.
     */
    public void removeChild(StrixaGLElement child){
        final List<Strixa2DElement> children = this.getChildren();
        
        
        if(children.contains(child)){
            children.remove(child);
        }
    }
    
    /**
     * Moves the viewing area to the 
     * 
     * @param x_modification The number of units x which the viewing area should be shifted left or right.
     * @param y_modification The number of units y which the viewing area should be shifted up or down.
     */
    public void shiftViewingArea(double x_modification,double y_modification){
        this.__camera_location.setPoint(
            this.__camera_location.getX() - x_modification,
            this.__camera_location.getY() - y_modification
        );
        
        this._refreshViewableArea();
    }
    /*Begin Other Essential Methods*/
}

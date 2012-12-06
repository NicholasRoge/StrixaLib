/**
 * File:  StrixaGLCanvas.java
 * Date of Creation:  Jul 16, 2012
 */
package com.strixa.gl;

import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;


/**
 * Creates an canvas to which any Strixa elements should be drawn.
 *
 * @author Nicholas Rogé
 */
public abstract class StrixaGLCanvas extends GLCanvas implements GLEventListener{
    /** Field needed for the serialization of this object. */
    private static final long serialVersionUID = -6426147154592668101L;
    
    private final FPSAnimator     __animator = new FPSAnimator(this,60);
    private final StrixaGLContext __context = new StrixaGLContext();
   
    private double  __aspect_ratio;
    
    
    /*Begin Constructors*/
    /**
     * Constructs the objects with the given capabilities.
     * 
     * @param capabilities Capabilities GLCanvas should have.
     * @param aspect_ratio Ratio of the width of this canvas, to it's height. (width/height)
     */
    public StrixaGLCanvas(GLCapabilities capabilities,double aspect_ratio){
        super(capabilities);
        
        
        this.setAspectRatio(aspect_ratio);        
        
        this.addGLEventListener(this);
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    public FPSAnimator getAnimator(){
        return this.__animator;
    }
    
    /**
     * Gets the aspect ratio of this canvas.
     * 
     * @return The aspect ratio of this canvas.
     */
    public double getAspectRatio(){
        return this.__aspect_ratio;
    }
    
    /**
     * Gets the canvas' current context.
     * 
     * @return The canvas' current context.
     */
    public StrixaGLContext getStrixaGLContext(){        
        return this.__context;
    }
    
    /**
     * Sets this canvas' aspect ratio.  That is to say, the width of this canvas divided by its height.
     * 
     * @param aspect_ratio Aspect ratio of this canvas.
     */
    public void setAspectRatio(double aspect_ratio){
        this.__aspect_ratio = aspect_ratio;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    public void display(GLAutoDrawable drawable){   
        this._performGameLogic(this.getStrixaGLContext());
        
        /*Clear everything up.*/
        drawable.getGL().glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        /*Draw everything that needs to be drawn.*/
        this._drawChildren(drawable.getGL().getGL2());
    }
    
    public void dispose(GLAutoDrawable drawable){
    }
    
    public void init(GLAutoDrawable drawable){
        //drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
        final GL2 gl = drawable.getGL().getGL2();
        
        gl.glDisable(GL2.GL_CULL_FACE);
        gl.glClearColor(.1f,.1f,.1f,1f);
        gl.setSwapInterval(1);
        
        
        this.__animator.add(this);
        this.__animator.start();
    }
    
    public void reshape(GLAutoDrawable drawable,int x,int y,int width,int height){
        drawable.getGL().glViewport(x,y,width,height);
    }
    
    /*Begin Abstract Methods*/
    /**
     * Draws this canvas' children.
     * 
     * @param gl GL2 canvas that the children should be drawn to.
     */
    protected abstract void _drawChildren(GL2 gl);
    
    /**
     * Define this method to implement your game or program's logic.
     * 
     * @param context This is the context in which the game or program is currently running.
     */
    protected abstract void _performGameLogic(StrixaGLContext context);
    /*End Abstract Methods*/
}

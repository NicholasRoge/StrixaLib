/**
 * File:  Strixa2DElement.java
 * Date of Creation:  July 17, 2012
 */
package com.strixa.gl;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.strixa.gl.properties.Cuboid;
import com.strixa.gl.shapes.RectangularPrism;
import com.strixa.gl.util.Vertex;
import com.strixa.math.StrixaMath;
import com.strixa.util.Dimension3D;
import com.strixa.util.Log;
import com.strixa.util.Point2D;
import com.strixa.util.Point3D;


/**
 * Creates an object to be displayed on a 3D plane.
 *
 * @author Nicholas Rogé
 */
public class Strixa3DElement extends StrixaGLElement{
    /**
     * Methods for collision detection.
     *
     * @author Nicholas Rogé
     */
    public enum CollisionDetectionMethod{
        /**
         * Determines collision by comparing the two bounding boxes of both objects.
         */
        BOUNDING_BOX,
        
        /**
         * Determines collision by comparing the individual points of an object with the current instance of the object's bounding box.
         */
        POINT
    }
    
    private final List<StrixaPolygon>   __components = new ArrayList<StrixaPolygon>();
    
    private Cuboid         __bounding_box;
    private boolean        __bounding_box_visible;
    private Integer        __list_index;
    private StrixaMaterial __material;
    
    
    /*Begin Constructor*/
    /**
     * Constructs a basic Strixa3DElement.
     */
    public Strixa3DElement(){
        this.__material = new StrixaMaterial();
        this.__bounding_box = new Cuboid(
            new Point3D<Double>(0.0,0.0,0.0),
            0,0,0
        );
        
        this.setBoundingBoxVisible(false);
    }
    /*End Constructor*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets the box which completely and exactly encloses all of this element.
     * 
     * @return The box which completely and exactly encloses all of this element.
     */
    public Cuboid getBoundingBox(){
       return this.__bounding_box; 
    }
    
    /**
     * Gets the list of components currently added to this element.
     * 
     * @return The list of components currently added to this element.
     */
    public List<StrixaPolygon> getComponents(){
        return this.__components;
    }
    
    public Point3D<Double> getCoordinates(){        
        return this.getBoundingBox().getCoordinates();
    }
    
    public Dimension3D<Double> getDimensions(){
        return this.__bounding_box.getDimensions();
    }
    
    /**
     * Gets the material currently being used while drawing this object.
     * 
     * @return Returns the material currently being used.
     */
    public StrixaMaterial getMaterial(){
        return this.__material;
    }
    
    /**
     * Returns whether the bounding box is visible or not.
     * 
     * @return Returns true if the bounding box is visible, and false, otherwise.
     */
    public boolean isBoundingBoxVisible(){
        return this.__bounding_box_visible;
    }
    
    /**
     * Determines whether the bounding box should be displayed on this object or not.
     * 
     * @param visible This should true if the bounding box should be displayed, and false, otherwise.
     */
    public void setBoundingBoxVisible(boolean visible){
        this.__bounding_box_visible = visible;
        
        this.invalidate();
    }
    
    /**
     * Sets this element's coordinates.
     * 
     * @param x X coordinate this object should be moved to.
     * @param y Y coordinate this object should be moved to.
     * @param z Z coordinate this object should be moved to.
     */
    public void setCoordinates(double x,double y,double z){
        this.getCoordinates().setPoint(x,y,z);
        
        this.invalidate();
    }
    
    /**
     * Sets the material this element should be using while being drawn.
     * 
     * @param material Material to be used.
     */
    public void setMaterial(StrixaMaterial material){
        this.__material = material;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    /**
     * Adds a polygon to this element.  If the polygon already exists within this element, it will not be added again.<br />
     * <strong>Note:</strong>  The {@link Strixa3DElement#addComponents(List)} method is the preferred method to use when adding multiple components to this element. 
     * 
     * @param polygon Polygon to add to this element.
     */
    public void addComponent(StrixaPolygon polygon){
        if(!this.__components.contains(polygon)){
            polygon.setParent(this);
            this.__components.add(polygon);
        }
        
        this.invalidate();
    }
    
    /**
     * Adds the polygons in the given list to this element.
     * 
     * @param polygon_list Polygons to be added.
     */
    public void addComponents(List<StrixaPolygon> polygons){
        for(int index = 0,end_index = polygons.size() - 1;index <= end_index;index++){
            if(!this.__components.contains(polygons)){
                polygons.get(index).setParent(this);
                this.__components.add(polygons.get(index));
            }
        }
        
        this.invalidate();
    }
   
    /**
     * Check to determine if the given point is within the objects parimeters.  This method only checks the X and Y dimensions.
     * 
     * @param point Point to check against.
     * 
     * @return Returns true, if the point is inside the object, and false, otherwise.
     */
    public boolean containsPoint(Point2D<Double> point){
        final double max_x = this.getCoordinates().getX() + this.getBoundingBox().getWidth();
        final double max_y = this.getCoordinates().getY() + this.getBoundingBox().getHeight();
        final double min_x = this.getCoordinates().getX();
        final double min_y = this.getCoordinates().getY();
        
        
        if(
            !(point.getX() >= min_x && point.getX() <= max_x) 
            ||
            !(point.getY() >= min_y && point.getY() <= max_y)
        ){
            return false;
        }else{
            return true;
        }
    }
    
    /**
     * Check to determine if the given point is with the objects parimeters.
     * 
     * @param point Point to check against.
     * 
     * @return Returns true, if the point is inside the object, and false, otherwise.
     */
    public boolean containsPoint(Point3D<Double> point){
        final double max_x = this.getCoordinates().getX() + this.getBoundingBox().getWidth();
        final double max_y = this.getCoordinates().getY() + this.getBoundingBox().getHeight();
        final double max_z = this.getCoordinates().getZ() + this.getBoundingBox().getDepth();
        final double min_x = this.getCoordinates().getX();
        final double min_y = this.getCoordinates().getY();
        final double min_z = this.getCoordinates().getZ();
        
        
        if(
            !(point.getX() >= min_x && point.getX() <= max_x) 
            ||
            !(point.getY() >= min_y && point.getY() <= max_y)
            ||
            !(point.getZ() >= min_z && point.getZ() <= max_z)
        ){
            return false;
        }else{
            return true;
        }
    }
    
    public void draw(GL2 gl){        
        if(this.__material.hasTexture()){
            if(!this.__material.isTextureLoaded()){
                try{
                    this.__material.loadTexture();
                }catch(IOException e){
                    Log.logEvent(Log.Type.WARNING,"Texture could not be loaded, and will not be displayed.");
                }
            }
        }
        
        if(this.__list_index == null){
            this.__list_index = gl.glGenLists(1);
            gl.glNewList(this.__list_index,GL2.GL_COMPILE);
            
            this._drawComponents(gl,this.getComponents(),this.__material);
            if(this.isBoundingBoxVisible()){
                this._drawBoundingBox(gl);
            }
            
            gl.glEndList();
        }
        
        
        gl.glCallList(this.__list_index);
    }  
    
    protected void _drawBoundingBox(GL2 gl){
        final Point3D<Double>     coordinates = this.getCoordinates();
        final Dimension3D<Double> dimensions = this.getBoundingBox().getDimensions();
        final IntBuffer           old_mode = IntBuffer.allocate(1);
        
        RectangularPrism bounding_box = null;
        
        
        bounding_box = new RectangularPrism(dimensions.getWidth(),dimensions.getHeight(),dimensions.getDepth());
        bounding_box.setCoordinates(coordinates.getX(),coordinates.getY(),coordinates.getZ());
        bounding_box.getMaterial().setAmbientColor(1,1,0);
        
        gl.glGetIntegerv(GL2.GL_POLYGON_MODE,old_mode);
        
        //Draw it
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK,GL2.GL_LINE);
        bounding_box._drawComponents(gl,bounding_box.getComponents(),bounding_box.getMaterial());
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK,old_mode.get(0));
    }
    
    /**
     * Draws the requested component.
     * 
     * @param component Component to be drawn.
     */
    protected void _drawComponent(GL2 gl,StrixaPolygon component){
        /*Begin Parameter Verification*/
        if(gl == null){
            try{
                gl = GLContext.getCurrentGL().getGL2();
            }catch(GLException e){
                Log.logEvent(Log.Type.WARNING,"Attempt was made to draw a polygon with no GLContext available.");
                
                return;
            }
        }
        
        if(component == null){
            Log.logEvent(Log.Type.WARNING,"Attempt was made to draw empty polygon.");
            
            return;
        }
        /*End Parameter Verification*/
        
        
        final List<Vertex> coordinate_points = component.getPoints();
        final List<Vertex> normal_points = component.getNormalPoints();
        final List<Vertex> texture_points = component.getTexturePoints();
        
        
        gl.glPushMatrix();
        gl.glTranslated(component.getCoordinates().getX(),component.getCoordinates().getY(),component.getCoordinates().getZ());
        
        switch(coordinate_points.size()){
            case 0:
            case 1:
            case 2:
                Log.logEvent(Log.Type.WARNING,"You must add at least 3 points to a polygon in order for it to be drawn.");
                return;
            case 3:
                gl.glBegin(GL2.GL_TRIANGLES);
                break;
            case 4:
                gl.glBegin(GL2.GL_QUADS);
                break;
            default:
                gl.glBegin(GL2.GL_POLYGON);
                break;
        }
        
        for(int point_index = 0,point_end_index = component.getPoints().size();point_index < point_end_index;point_index++){            
            if(!texture_points.isEmpty()){
                gl.glTexCoord3d(
                    texture_points.get(point_index).getX(),     //U
                    texture_points.get(point_index).getY(),     //V
                    texture_points.get(point_index).getWeight() //W
                );
            }
            if(!normal_points.isEmpty()){
                gl.glNormal3d(
                    normal_points.get(point_index).getX(),
                    normal_points.get(point_index).getY(),
                    normal_points.get(point_index).getZ()
                );
            }
            if(!gl.glIsEnabled(GL2.GL_LIGHTING)){
                gl.glColor4fv(coordinate_points.get(point_index).getColor(),0);
            }
            gl.glVertex4d(
                coordinate_points.get(point_index).getX(),
                coordinate_points.get(point_index).getY(),
                coordinate_points.get(point_index).getZ(),
                coordinate_points.get(point_index).getWeight()
            );
        }
            
        gl.glEnd();
        gl.glPopMatrix();
    }
    
    /**
     * Draws the requested components.
     * 
     * @param components Components to be drawn.
     */
    protected void _drawComponents(GL2 gl,List<StrixaPolygon> components,StrixaMaterial material){
        final Point3D<Double> this_coordinates = this.getCoordinates();
        
        
        gl.glPushMatrix();
        gl.glTranslated(this_coordinates.getX(),this_coordinates.getY(),this_coordinates.getZ());     
        
        if(material.isTextureLoaded()){  //We're adding a second if here to make sure that if the material for some reason couldn't be loaded, we don't try to bind to it still.
            material.getTexture().bind(gl);
            material.getTexture().enable(gl);
            
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV,GL2.GL_TEXTURE_ENV_MODE,GL2.GL_MODULATE);
            gl.glTexParameterf(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_WRAP_S,GL2.GL_REPEAT);
            gl.glTexParameterf(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_WRAP_T,GL2.GL_REPEAT);
        }
        
        if(gl.glIsEnabled(GL2.GL_LIGHTING)){
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,GL2.GL_AMBIENT,material.getAmbientColor(),0);
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,GL2.GL_DIFFUSE,material.getDiffuseColor(),0);
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,GL2.GL_EMISSION,material.getEmissionColor(),0);
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,GL2.GL_SPECULAR,material.getSpecularColor(),0);
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,GL2.GL_SHININESS,new float[]{material.getSpecularCoefficient()},0);
        }
        
        for(int component_index = 0,component_end_index = components.size();component_index < component_end_index;component_index++){
            this._drawComponent(gl,components.get(component_index));
        }
        
        if(material.isTextureLoaded()){
            material.getTexture().disable(gl);
        }
        
        gl.glPopMatrix();
    }
    
    /**
     * Indicates that something about this element has changed, and that it should be recreated.
     */
    public void invalidate(){
        this.__list_index = null;
        this._regenerateBoundingBox();
    }
    
    /**
     * Method to check for collision with another object.
     * 
     * @param element Element who you're trying to detect if this object is colliding with.
     * 
     * @return Returns true if this object is colliding with the given object, and false, otherwise. 
     */
    public boolean isColliding(Strixa3DElement element){        
        return this.isColliding(element,CollisionDetectionMethod.BOUNDING_BOX);
    }
    
    /**
     * Method to check for collision with another object.
     * 
     * @param element Element who you're trying to detect if this object is colliding with.
     * @param method Method of collision detection to use.
     * 
     * @return Returns true if this object is colliding with the given object, and false, otherwise. 
     */
    public boolean isColliding(Strixa3DElement element,CollisionDetectionMethod method){        
        List<StrixaPolygon> element_components = element.getComponents();
        List<StrixaPolygon> this_components = this.getComponents(); 
        
        
        if(!this.isCollisionDetectionEnabled() || !element.isCollisionDetectionEnabled()){
            return false;
        }
        
        switch(method){
            case BOUNDING_BOX:
                double e1_min_x = this.getBoundingBox().getCoordinates().getX();
                double e1_min_y = this.getBoundingBox().getCoordinates().getY();
                double e1_min_z = this.getBoundingBox().getCoordinates().getZ();
                double e1_max_x = this.getBoundingBox().getCoordinates().getX() + this.getBoundingBox().getWidth();
                double e1_max_y = this.getBoundingBox().getCoordinates().getY() + this.getBoundingBox().getHeight();
                double e1_max_z = this.getBoundingBox().getCoordinates().getZ() + this.getBoundingBox().getDepth();
                double e2_min_x = element.getBoundingBox().getCoordinates().getX();
                double e2_min_y = element.getBoundingBox().getCoordinates().getY();
                double e2_min_z = element.getBoundingBox().getCoordinates().getZ();
                double e2_max_x = element.getBoundingBox().getCoordinates().getX() + element.getBoundingBox().getWidth();
                double e2_max_y = element.getBoundingBox().getCoordinates().getY() + element.getBoundingBox().getHeight();
                double e2_max_z = element.getBoundingBox().getCoordinates().getZ() + element.getBoundingBox().getDepth();
                
                
                if(
                    (
                        (e1_max_x >= e2_min_x && e1_max_x <= e2_max_x)
                        ||
                        (e2_max_x >= e1_min_x && e2_max_x <= e1_max_x)
                    )
                    &&
                    (
                        (e1_max_y >= e2_min_y && e1_max_y <= e2_max_y)
                        ||
                        (e2_max_y >= e1_min_y && e2_max_y <= e1_max_y)
                    )
                    &&
                    (
                        (e1_max_z >= e2_min_z && e1_max_z <= e2_max_z)
                        ||
                        (e2_max_z >= e1_min_z && e2_max_z <= e1_max_z)
                    )
                ){
                    return true;
                }else{
                    return false;
                }
            case POINT:
                {
                    final ArrayList<Vertex> adjusted_vertices = new ArrayList<Vertex>();
                    final Point3D<Double> element_coordinates = element.getCoordinates();
                    
                    boolean collision = false;
                    List<Vertex> component_vertices = null;
                    Vertex       vertex = null;
                    
                    
                    for(int component_index = 0,component_end_index = element_components.size() - 1;component_index <= component_end_index;component_index++){
                        component_vertices = element_components.get(component_index).getPoints();
                        
                        for(int point_index = 0,point_end_index = component_vertices.size() - 1;point_index <= point_end_index;point_index++){
                            vertex = component_vertices.get(point_index);
                            
                            if(!adjusted_vertices.contains(vertex)){
                                adjusted_vertices.add(new Vertex(
                                    vertex.getX() + element_coordinates.getX(),
                                    vertex.getY() + element_coordinates.getY(),
                                    vertex.getZ() + element_coordinates.getZ(),
                                    vertex.getWeight() //This really should matter.
                                ));
                            }
                        }
                    }
                    
                    for(int point_index = 0,point_end_index = adjusted_vertices.size() - 1;point_index <= point_end_index;point_index++){
                        if(this.containsPoint(adjusted_vertices.get(point_index))){
                            collision = true;
                            
                            break;
                        }
                    }
                    
                    return collision;
                }
            default:
                for(int this_index = 0,this_end_index = this_components.size() - 1;this_index <= this_end_index;this_index++){
                    for(int element_index = 0,element_end_index = element_components.size() - 1;element_index <= element_end_index;element_index++){
                        if(this.__components.get(this_index).isColliding(element_components.get(element_index))){
                            return true;
                        }
                    }
                }
                
                return false;
        }
    }
    
    /**
     * Simple check to determine whether this element is visible in the current context.
     * 
     * @param context The context in which the StrixaGL application is currently being run.
     * 
     * @return Returns true if this element is visible and should be drawn, and false, otherwise.
     */
    public boolean isVisible(StrixaGLContext context){
        if(!super.isVisible(context)){
            return false;
        }
        
        final Cuboid          bounding_box = this.getBoundingBox();
        final Point3D<Double> bounding_box_coordinates = bounding_box.getCoordinates();
        final Cuboid          viewable_area = context.getViewableArea();
        
        
        if(viewable_area.isPointInside(bounding_box_coordinates)){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX() + bounding_box.getWidth(),
            bounding_box_coordinates.getY(),
            bounding_box_coordinates.getZ()
        ))){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX(),
            bounding_box_coordinates.getY() + bounding_box.getHeight(),
            bounding_box_coordinates.getZ()
        ))){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX(),
            bounding_box_coordinates.getY(),
            bounding_box_coordinates.getZ() + bounding_box.getDepth()
        ))){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX() + bounding_box.getWidth(),
            bounding_box_coordinates.getY() + bounding_box.getHeight(),
            bounding_box_coordinates.getZ()
        ))){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX(),
            bounding_box_coordinates.getY() + bounding_box.getHeight(),
            bounding_box_coordinates.getZ() + bounding_box.getDepth()
        ))){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX() + bounding_box.getWidth(),
            bounding_box_coordinates.getY(),
            bounding_box_coordinates.getZ() + bounding_box.getDepth()
        ))){
            return true;
        }else if(viewable_area.isPointInside(new Point3D<Double>(
            bounding_box_coordinates.getX() + bounding_box.getWidth(),
            bounding_box_coordinates.getY() + bounding_box.getHeight(),
            bounding_box_coordinates.getZ() + bounding_box.getDepth()
        ))){
            return true;
        }else{        
            return false;
        }
    }
    
    /**
     * Regenerates the element's bounding box.
     */
    protected void _regenerateBoundingBox(){
        final List<StrixaPolygon> polygons = this.getComponents();
        final int                 polygon_count = polygons.size();
        final Point3D<Double>     this_coordinates = this.getCoordinates();
        
        double            depth = 0.0;
        double            height = 0.0;
        List<Vertex>      points = null;
        double            width = 0.0;
        
        
        if(!polygons.isEmpty()){
            for(int polygon_index = 0;polygon_index < polygon_count;polygon_index++){
                points = polygons.get(polygon_index).getPoints();
                
                for(int index = 0,end_index = points.size() - 1;index < end_index;index++){
                    width = Math.max(width,points.get(index).getX());
                    height = Math.max(height,points.get(index).getY());
                    depth = Math.max(depth,points.get(index).getZ());
                }
            }
        }
        
        this.__bounding_box = new Cuboid(
            new Point3D<Double>(this_coordinates),
            width,
            height,
            depth
        );
    }
    
    /**
     * Rotates the object a certain number of degrees on the given axis.
     * 
     * @param degrees Number of degrees to rotate the object.
     * 
     * @param axis Axis around which the object should be rotated.
     */
    public void rotate(double degrees,int axis){
        final Point3D<Double> rotational_origin = new Point3D<Double>(
            this.getBoundingBox().getWidth()/2,
            this.getBoundingBox().getHeight()/2,
            this.getBoundingBox().getDepth()/2
        );
        final ArrayList<Vertex> unique_points = new ArrayList<Vertex>();
        
        Double          coordinate_modification_x = 0.0;
        Double          coordinate_modification_y = 0.0;
        Double          coordinate_modification_z = 0.0;
        Point3D<Double> rotated_point = null;
        Vertex          point = null;
        List<Vertex>    points = null;
                
        
        
        //Now for the fun part.
        /*First Pass:  Rotate the points*/
        for(int component_index = 0,component_end_index = this.__components.size() - 1;component_index <= component_end_index;component_index++){
            points = this.__components.get(component_index).getPoints();
            for(int point_index = 0,point_end_index = points.size() - 1;point_index <= point_end_index;point_index++){
                if(!unique_points.contains(points.get(point_index))){
                    unique_points.add(points.get(point_index));
                }
            }
        }
        for(int point_index = 0,point_end_index = unique_points.size() - 1;point_index <= point_end_index;point_index++){
            point = unique_points.get(point_index);
                    
            rotated_point = StrixaMath.rotate(point,rotational_origin,degrees,axis);
            point.setPoint(rotated_point.getX(),rotated_point.getY(),rotated_point.getZ());
            
            if(point_index == 0){
                coordinate_modification_x = point.getX();
                coordinate_modification_y = point.getY();
                coordinate_modification_z = point.getZ();
            }else{
                coordinate_modification_x = Math.min(coordinate_modification_x,point.getX());
                coordinate_modification_y = Math.min(coordinate_modification_y,point.getY());
                coordinate_modification_z = Math.min(coordinate_modification_z,point.getZ());
            }
        }
        
        this.setCoordinates(coordinate_modification_x + this.getCoordinates().getX(),coordinate_modification_y + this.getCoordinates().getY(),coordinate_modification_z + this.getCoordinates().getZ());
        
        /*Second Pass:  Make all the points relative to the origin*/
        for(int point_index = 0,point_end_index = unique_points.size() - 1;point_index <= point_end_index;point_index++){
            point = unique_points.get(point_index);
                
            point.setPoint(
                point.getX() - coordinate_modification_x,
                point.getY() - coordinate_modification_y,
                point.getZ() - coordinate_modification_z
            );
        }
        
        //Woo hoo!  We're done.
        this.invalidate();
    }
    
    /**
     * Scales this object by a certain percentage.
     * 
     * @param scaling_amount Amount to scale object, where 0 indicates no scaling, and 1 indicates that the object should become infinitely small.
     */
    public void scale(double scaling_amount){
        final ArrayList<Vertex> unique_points = new ArrayList<Vertex>();
        
        double          coordinate_modification_x = 0.0;
        double          coordinate_modification_y = 0.0;
        double          coordinate_modification_z = 0.0;
        Point3D<Double> scaled_point = null;
        Vertex          point = null;
        List<Vertex>    points = null;
                
        
        
        //Now for the fun part.
        /*First Pass:  Scale the points*/
        for(int component_index = 0,component_end_index = this.__components.size() - 1;component_index <= component_end_index;component_index++){
            points = this.__components.get(component_index).getPoints();
            for(int point_index = 0,point_end_index = points.size() - 1;point_index <= point_end_index;point_index++){
                if(!unique_points.contains(points.get(point_index))){
                    unique_points.add(points.get(point_index));
                }
            }
        }
        for(int point_index = 0,point_end_index = unique_points.size() - 1;point_index <= point_end_index;point_index++){
            point = unique_points.get(point_index);
                    
            scaled_point = StrixaMath.scale(point,scaling_amount);
            point.setPoint(scaled_point.getX(),scaled_point.getY(),scaled_point.getZ());
            
            if(point_index == 0){
                coordinate_modification_x = point.getX();
                coordinate_modification_y = point.getY();
                coordinate_modification_z = point.getZ();
            }else{
                coordinate_modification_x = Math.min(coordinate_modification_x,point.getX());
                coordinate_modification_y = Math.min(coordinate_modification_y,point.getY());
                coordinate_modification_z = Math.min(coordinate_modification_z,point.getZ());
            }
        }
        
        this.setCoordinates(coordinate_modification_x + this.getCoordinates().getX(),coordinate_modification_y + this.getCoordinates().getY(),coordinate_modification_z + this.getCoordinates().getZ());
        
        /*Second Pass:  Make all the points relative to the origin*/
        for(int point_index = 0,point_end_index = unique_points.size() - 1;point_index <= point_end_index;point_index++){
            point = unique_points.get(point_index);
                
            point.setPoint(
                point.getX() - coordinate_modification_x,
                point.getY() - coordinate_modification_y,
                point.getZ() - coordinate_modification_z
            );
        }
        
        //Woo hoo!  We're done.
        this.invalidate();
    }
    /*End Other Methods*/
}

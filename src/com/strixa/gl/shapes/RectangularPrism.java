/**
 * File:  RectangularPrism.java
 * Date of Creation:  Nov 12, 2012
 */
package com.strixa.gl.shapes;

import java.util.ArrayList;
import java.util.List;

import com.strixa.gl.Strixa3DElement;
import com.strixa.gl.StrixaPolygon;
import com.strixa.gl.util.Vertex;
import com.strixa.util.Dimension3D;
import com.strixa.util.Point3D;

/**
 * TODO:  Write Class Description
 *
 * @author Nicholas Rogé
 */
public class RectangularPrism extends Strixa3DElement{    
    private Dimension3D<Double> __dimensions;
    
    
    /*Begin Constructor*/
    public RectangularPrism(double width,double height,double depth){
        if(width < 0 || height < 0 || depth < 0){
            throw new IllegalArgumentException("No length defining parameters may take on negative values.");
        }
        
        this.__dimensions = new Dimension3D<Double>(width,height,depth);
        
        this._recreate();
    }
    /*End Constructor*/
    
    /*Begin Other Methods*/
    protected void _recreate(){
        final Vertex[] points = new Vertex[]{
            new Vertex(0,this.__dimensions.getHeight(),0,1),
            new Vertex(this.__dimensions.getWidth(),this.__dimensions.getHeight(),0,1),
            new Vertex(0,0,0,1),
            new Vertex(this.__dimensions.getWidth(),0,0,1),
            new Vertex(0,this.__dimensions.getHeight(),this.__dimensions.getDepth(),1),
            new Vertex(this.__dimensions.getWidth(),this.__dimensions.getHeight(),this.__dimensions.getDepth(),1),
            new Vertex(0,0,this.__dimensions.getDepth(),1),
            new Vertex(this.__dimensions.getWidth(),0,this.__dimensions.getDepth(),1)
            
            /*
             * Point descriptions:
             * 
             * 4------5
             * |\     |\
             * | \    | \
             * |  0------1
             * |  |   |  |
             * 6--|---7  |
             *  \ |    \ |
             *   \|     \|
             *    2------3
             */
        };
        final ArrayList<Vertex> polygon_points = new ArrayList<Vertex>();
        
        StrixaPolygon polygon = null;     
        
        
        
        this.getComponents().clear();
        
        //Front Face
        polygon = new StrixaPolygon();
            polygon_points.add(points[0]);
            polygon_points.add(points[1]);
            polygon_points.add(points[3]);
            polygon_points.add(points[2]);
        polygon.addPoints(polygon_points);
        this.addComponent(polygon);
        polygon_points.clear();
        
        //Back Face
        polygon = new StrixaPolygon();
            polygon_points.add(points[5]);
            polygon_points.add(points[4]);
            polygon_points.add(points[6]);
            polygon_points.add(points[7]);
        polygon.addPoints(polygon_points);
        this.addComponent(polygon);
        polygon_points.clear();
        
        //Left Face
        polygon = new StrixaPolygon();
            polygon_points.add(points[4]);
            polygon_points.add(points[0]);
            polygon_points.add(points[2]);
            polygon_points.add(points[6]);
        polygon.addPoints(polygon_points);
        this.addComponent(polygon);
        polygon_points.clear();
        
        //Right Face
        polygon = new StrixaPolygon();
            polygon_points.add(points[1]);
            polygon_points.add(points[5]);
            polygon_points.add(points[7]);
            polygon_points.add(points[3]);
        polygon.addPoints(polygon_points);
        this.addComponent(polygon);
        polygon_points.clear();
        
        //Bottom Face
        polygon = new StrixaPolygon();
            polygon_points.add(points[2]);
            polygon_points.add(points[3]);
            polygon_points.add(points[7]);
            polygon_points.add(points[6]);
        polygon.addPoints(polygon_points);
        this.addComponent(polygon);
        polygon_points.clear();
        
        //Top Face
        polygon = new StrixaPolygon();
            polygon_points.add(points[4]);
            polygon_points.add(points[5]);
            polygon_points.add(points[1]);
            polygon_points.add(points[0]);
        polygon.addPoints(polygon_points);
        this.addComponent(polygon);
        polygon_points.clear();
    }
    /*End Other Methods*/
}

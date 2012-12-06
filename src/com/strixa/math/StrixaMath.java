/**
 * File:  Math.java
 * Date of Creation:  Nov 23, 2012
 */
package com.strixa.math;

import com.strixa.util.Point3D;

/**
 * TODO:  Write Class Description
 *
 * @author Nicholas Rogé
 */
public class StrixaMath{
    /** X axis constant for use with rotation. */
    public static final int AXIS_X = 0x1;
    /** Y axis constant for use with rotation. */
    public static final int AXIS_Y = 0x2;
    /** Z axis constant for use with rotation. */
    public static final int AXIS_Z = 0x4;
    
    
    /**
     * Rotates the given point around the requested axis.
     * <strong>Note:</strong>  This method rotates around the (0,0,0).
     * 
     * @param point Point to rotate.
     * @param degrees Degrees to rotate point.
     * @param axis Axis to rotate the point around.  You can bitwise OR the axis together to rotate around multiple axis.
     * 
     * @return The newly rotated point.
     */
    public static Point3D<Double> rotate(Point3D<Double> point,double degrees,int axis){
        return StrixaMath.rotate(point,new Point3D<Double>(0.0,0.0,0.0),degrees,axis);
    }
    
    /**
     * Rotates the given point around the requested axis.
     * 
     * @param point Point to rotate.
     * @param rotational_origin Point around which this point should rotate.
     * @param degrees Degrees to rotate point.
     * @param axis Axis to rotate the point around.  You can bitwise OR the axis together to rotate around multiple axis.
     * 
     * @return The newly rotated point.
     */
    public static Point3D<Double> rotate(Point3D<Double> point,Point3D<Double> rotational_origin,double degrees,int axis){
        final double radians = (degrees*Math.PI)/180;
        final Matrix x_matrix = new Matrix(3,3,new double[][]{
            {
                1,0,0
            },
            {
                0,Math.cos(radians),-Math.sin(radians)
            },
            {
                0,Math.sin(radians),Math.cos(radians)
            }
        }); 
        final Matrix y_matrix = new Matrix(3,3,new double[][]{
            {
                Math.cos(radians),0,Math.sin(radians)
            },
            {
                0,1,0
            },
            {
                -Math.sin(radians),0,Math.cos(radians)
            }
        });
        final Matrix z_matrix = new Matrix(3,3,new double[][]{
            {
                Math.cos(radians),-Math.sin(radians),0
            },
            {
                Math.sin(radians),Math.cos(radians),0
            },
            {
                0,0,1
            }
        });
        
        Matrix coordinates = null;
        Matrix rotation_matrix = null;
        
        
        /*Set up the rotation_matrix*/
        if((axis & 0x1) > 0){  //X-axis
            rotation_matrix = x_matrix;
        }
        if((axis & 0x2) > 0){  //Y-axis
            if(rotation_matrix == null){
                rotation_matrix = y_matrix;
            }else{
                rotation_matrix = Matrix.multiply(rotation_matrix,y_matrix);
            }
        }
        if((axis & 0x4) > 0){  //Z-axis
            if(rotation_matrix == null){
                rotation_matrix = z_matrix;
            }else{
                rotation_matrix = Matrix.multiply(rotation_matrix,z_matrix);
            }
        }
        
        /*Rotate the point*/
        coordinates = new Matrix(3,1,new double[][]{
            {
                point.getX() - rotational_origin.getX()
            },
            {
                point.getY() - rotational_origin.getY()
            },
            {
                point.getZ() - rotational_origin.getZ()
            }
        });
        coordinates = Matrix.multiply(rotation_matrix,coordinates);
        
        return new Point3D<Double>(
            coordinates.getCell(0,0) + rotational_origin.getX(),
            coordinates.getCell(1,0) + rotational_origin.getY(),
            coordinates.getCell(2,0) + rotational_origin.getZ()
        );
    }
    
    /**
     * Scales a point toward the origin.
     * 
     * @param point Point to scale.
     * @param scaling_amount Amount to scale, where 1 is no scaling, making the argument more negative makes the object smaller, and making the argument more positive makes the object larger.
     * 
     * @return Returns the scaled point.
     */
    public static Point3D<Double> scale(Point3D<Double> point,double scaling_amount){
        final Matrix scaling_matrix = new Matrix(3,3,new double[][]{
            {
                scaling_amount,0,0
            },
            {
                0,scaling_amount,0
            },
            {
                0,0,scaling_amount
            }
        });
        
        Matrix coordinates = null;
        
        
        coordinates = new Matrix(3,1,new double[][]{
            {
                point.getX()
            },
            {
                point.getY()
            },
            {
                point.getZ()
            }
        });
        coordinates = Matrix.multiply(scaling_matrix,coordinates);
        
        return new Point3D<Double>(
            coordinates.getCell(0,0),
            coordinates.getCell(1,0),
            coordinates.getCell(2,0)
        );
    }
}

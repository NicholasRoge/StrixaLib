/**
 * File:  Matrix.java
 * Date of Creation:  Nov 7, 2012
 */
package com.strixa.math;

/**
 * Matrix which holds a given number of rows and columns.
 *
 * @author Nicholas Rogé
 */
public class Matrix{    
    private int        __columns;
    private double[][] __data;
    private int        __rows;
    
    
    
    /*Begin Constructors*/
    /**
     * Creates a matrix whose dimensions are those given with all data values initially set to zero.
     * 
     * @param rows Number of rows the matrix should contain.
     * @param columns Number of columns the matrix should contain.
     * 
     * @throws IllegalArgumentException Thrown if either argument 'rows' or 'columns' is less than one.
     */
    public Matrix(int rows,int columns){
        if(rows < 1 || columns < 1){
            throw new IllegalArgumentException();
        }
        
        this.__rows = rows;
        this.__columns = columns;
        
        this.__data = new double[rows][columns];
        for(int row_index = 0,row_end_index = rows - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = columns - 1;column_index <= column_end_index;column_index++){
                this.__data[row_index][column_index] = 0.0;
            }
        }
    }
    
    /**
     * Creates a matrix whose dimensions are those given with all data set according to the 'data' argument..
     * 
     * @param rows Number of rows the matrix should contain.
     * @param columns Number of columns the matrix should contain.
     * @param data Data values this matrix should take on upon initialization.  This must have not be null, and must have the same number of rows and columns as the matrix which will take on its data.
     * 
     * @throws IllegalArgumentException Thrown if either argument 'rows' or 'columns' is less than one.
     */
    public Matrix(int rows,int columns,double[][] data){
        this(rows,columns);
        this.setData(data);
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * Gets the data from the cell at the requested coordinates.
     * 
     * @param row Index of the row to retrieve.
     * @param column Index of the column to retrieve.
     * 
     * @return Returns the data from the cell at the requested coordinates.
     * 
     * @throws IndexOutOfBoundsException Thrown if the requested row is greater than the number of rows - 1, or the requested column is greater than the number of columns - 1.
     */
    public double getCell(int row,int column){
        return this.__data[row][column];
    }
    
    /**
     * Gets the number of columns in this matrix.
     * 
     * @return The number of columns in this matrix.
     */
    public int getColumnCount(){
        return this.__columns;
    }
    
    /**
     * Gets the number of rows in this matrix.
     * 
     * @return The number of rows in this matrix.
     */
    public int getRowCount(){
        return this.__rows;
    }
    
    /**
     * Sets the data from the cell at the requested coordinates.
     * 
     * @param row Index of the row to set.
     * @param column Index of the column to set.
     * 
     * @throws IndexOutOfBoundsException Thrown if the requested row is greater than the number of rows - 1, or the requested column is greater than the number of columns - 1.
     */
    public void setCell(double data,int row,int column){
        this.__data[row][column] = data;
    }
    
    /**
     * Sets this matrix's data.
     * 
     * @param data Data values this matrix should take on upon initialization.  This must have not be null, and must have the same number of rows and columns as the matrix which will take on its data.
     * 
     * @throws NullPointerException Thrown if argument 'data' is null.
     * @throws IllegalArgumentExceptoin Thrown if the number of rows and columns do not match that of the matrix which will take on argument data's data.
     */
    public void setData(double[][] data){
        /*Begin Parameter Verification*/
        if(data == null){
            throw new NullPointerException();
        }
        
        if(data.length != this.getRowCount()){
            throw new IllegalArgumentException();
        }
        
        for(int row_index = 0,row_end_index = data.length - 1;row_index <= row_end_index;row_index++){
            if(data[row_index] == null){
                throw new NullPointerException();
            }else if(data[row_index].length != this.getColumnCount()){
                throw new IllegalArgumentException();
            }
        }
        /*End Parameter Verification*/
        
        for(int row_index = 0,row_end_index = data.length - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = data[row_index].length - 1;column_index <= column_end_index;column_index++){
                this.__data[row_index][column_index] = data[row_index][column_index];
            }
        }
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Methods*/
    public boolean equals(Matrix matrix){
        if(this.getRowCount() != matrix.getRowCount() || this.getColumnCount() != matrix.getColumnCount()){
            return false;
        }
        
        for(int row_index = 0,row_end_index = this.getRowCount() - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = this.getRowCount() - 1;column_index <= column_end_index;column_index++){
                if(this.getCell(row_index,column_index) != matrix.getCell(row_index,column_index)){
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override public String toString(){
        int spaces_to_add = 0;
        StringBuffer buffer = null;
        
        
        for(int row_index = 0,row_end_index = this.getRowCount() - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = this.getRowCount() - 1;column_index <= column_end_index;column_index++){
                spaces_to_add = Math.max(spaces_to_add,(int)Math.ceil(Math.log10(this.__data[row_index][column_index])));
            }
        }
        
        buffer = new StringBuffer();
        for(int row_index = 0,row_end_index = this.getRowCount() - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = this.getRowCount() - 1;column_index <= column_end_index;column_index++){
                buffer.append("[ ");
                buffer.append(this.__data[row_index][column_index]);
                
                for(int space_count = (int)Math.ceil(Math.log10(this.__data[row_index][column_index]));space_count < spaces_to_add;space_count++){
                    buffer.append(" ");
                }
                
                buffer.append(" ]");
            }
            
            buffer.append("\n");
        }
        
        return buffer.toString();
    }
    /*End Other Methods*/
    
    /*Begin Static Methods*/
    /**
     * Adds to matrices together and returns their sum.
     * 
     * @param matrix_one First matrix to add.  
     * @param matrix_two Second matrix to add.
     * 
     * @return The sum of the two given matrices.
     * 
     * @throws NullPointerException Thrown if either argument 'matrix_one' or 'matrix_two" are null.
     * @throws IllegalArgumentException Thrown if the number of columns and rows in argument 'matrix_one' is not the same as the number of columns and rows in argument 'matrix_two'.
     */
    public static Matrix add(Matrix matrix_one,Matrix matrix_two){
        /*Begin Parameter Verification*/
        if(matrix_one == null){
            throw new NullPointerException("Argument 'matrix_one' must not be null.");
        }
        
        if(matrix_two == null){
            throw new NullPointerException("Argument 'matrix_two' must not be null.");
        }
        
        if(matrix_one.getRowCount() != matrix_two.getRowCount() || matrix_one.getColumnCount() != matrix_two.getColumnCount()){
            throw new IllegalArgumentException();
        }
        /*End Parameter Verification*/
        
        Matrix results = null;
        double sum = 0.0;
        
        
        results = new Matrix(matrix_one.getRowCount(),matrix_one.getColumnCount());
        for(int row_index = 0,row_end_index = results.getRowCount() - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = results.getColumnCount() - 1;column_index <= column_end_index;column_index++){
                sum = matrix_one.getCell(row_index,column_index) + matrix_two.getCell(row_index,column_index);
                
                results.setCell(sum,row_index,column_index);
            }
        }
        return results;
    }
    
    /**
     * Multiplies a matrix by the given scalar.
     * 
     * @param scalar Scalar to multiply the argument 'matrix' by.
     * @param matrix Matrix to multiply the scalar with.
     * 
     * @return The newly multiplied matrix.
     * 
     * @throws NullPointerException Thrown if the argument 'matrix' is null.
     */
    public static Matrix multiply(double scalar,Matrix matrix){
        /*Begin Parameter Verification*/
        if(matrix == null){
            throw new NullPointerException("Argument 'matrix' must not be null.");
        }
        /*End Parameter Verification*/
        
        Matrix results = null;
        double product = 0.0;
        
        
        results = new Matrix(matrix.getRowCount(),matrix.getColumnCount());
        for(int row_index = 0,row_end_index = results.getRowCount() - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = results.getColumnCount() - 1;column_index <= column_end_index;column_index++){
                product = scalar * matrix.getCell(row_index,column_index);
                
                results.setCell(product,row_index,column_index);
            }
        }
        return results;
    }
    
    /**
     * Multiplies two matrices together.
     * 
     * @param matrix_one Left-hand matrix.
     * @param matrix_two Right-hand matrix.
     * 
     * @return Returns the dot product of the two given matrices.
     * 
     * @throws NullPointerException Exception will be thrown if either argument 'matrix_one' or 'matrix_two' are null.
     * @throws IllegalArgumentException Exception will be thrown if the number of columns in argument 'matrix_one' does not match the number of rows in argument 'matrix_two'.
     */
    public static Matrix multiply(Matrix matrix_one,Matrix matrix_two){
        /*Begin Parameter Verification*/
        if(matrix_one == null){
            throw new NullPointerException("Argument 'matrix_one' must not be null.");
        }
        
        if(matrix_two == null){
            throw new NullPointerException("Argument 'matrix_two' must not be null.");
        }
        
        if(matrix_one.getColumnCount() != matrix_two.getRowCount()){
            throw new IllegalArgumentException();
        }
        /*End Parameter Verification*/
        
        Matrix results = null;
        double sum = 0;
        
        
        results = new Matrix(matrix_one.getRowCount(),matrix_two.getColumnCount());
        for(int row_index = 0,row_end_index = results.getRowCount() - 1;row_index <= row_end_index;row_index++){
            for(int column_index = 0,column_end_index = results.getColumnCount() - 1;column_index <= column_end_index;column_index++){
                sum = 0;
                
                for(int sub_index = 0,sub_end_index = matrix_one.getRowCount() - 1;sub_index <= sub_end_index;sub_index++){
                    sum += matrix_one.getCell(row_index,sub_index) * matrix_two.getCell(sub_index,column_index);
                }
                
                results.setCell(sum,row_index,column_index);
            }
        }
        return results;
    }
    /*End Static Methods*/
}

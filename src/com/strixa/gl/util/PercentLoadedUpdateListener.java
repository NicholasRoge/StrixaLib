/**
 * File:  FileLoadPercentListener.java
 * Date of Creation:  Oct 9, 2012
 */
package com.strixa.gl.util;

/**
 * Used when a class needs to provide a listener with load updates.
 *
 * @author Nicholas Rogé
 */
public interface PercentLoadedUpdateListener{
    /**
     * Called when an object has a load update.
     * 
     * @param amount_loaded Amount that has been loaded.  Exact number range will vary between classes.
     */
    public void onPercentLoadedUpdate(double amount_loaded);
}

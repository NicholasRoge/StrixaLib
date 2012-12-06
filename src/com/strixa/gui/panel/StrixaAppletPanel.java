/**
 * File:  RPanel.java
 * Date of Creation:  Jul 2, 2012
 */
package com.strixa.gui.panel;

import javax.swing.JPanel;

import com.strixa.gui.StrixaApplet;
import com.strixa.gui.StrixaWindow;



/**
 * Panel which should be extended and used in conjunction with the RWindow class.
 * 
 * @author Nicholas Rogé
 */
public abstract class StrixaAppletPanel extends JPanel{
    private StrixaApplet __parent;    
    
    
    /*Begin Constructors*/
    /**
     * Constructs the panel.
     * 
     * @param parent Parent window.
     */
    public StrixaAppletPanel(StrixaApplet parent){
        if(parent==null){
            throw new NullPointerException("The 'parent' argument must not be null.");
        }
        
        this.__parent=parent;
    }
    /*End Constructors*/
    
    /*Begin Getter/Setter Methods*/
    /**
     * @return Returns an initialized object of type StrixaApplet.
     */
    public StrixaApplet getParent(){
        return this.__parent;
    }
    /*End Getter/Setter Methods*/
    
    /*Begin Other Essential Methods*/
    /**
     * This method is called when the panel is being disposed of and should only be called by the parent window.  
     */
    public void onPanelClose(){}
    /*End Other Essential Methods*/
}

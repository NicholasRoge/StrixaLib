/**
 * File:  StrixaApplet.java
 * Date of Creation:  Sep 19, 2012
 */
package com.strixa.gui;

import java.awt.Component;

import javax.swing.JApplet;
import javax.swing.JPanel;

import com.strixa.gui.panel.StrixaAppletPanel;

/**
 * TODO:  Write Class Description
 *
 * @author Nicholas Rogé
 */
public abstract class StrixaApplet extends JApplet{
    private StrixaAppletPanel __main_panel;
    
    
    /*Begin Constructors*/
    public StrixaApplet(){
        this.__main_panel = null;
    }
    /*End Constructors*/
    
    /*Begin Overridden Methods*/
    @Override public Component add(Component component){
        System.out.println("Warning:  Use of the 'add(Component)' method on StrixaApplet is discouraged.  Please see documentation for encouraged usage.");
        
        return super.add(component);
    }
    /*End Overridden Methods*/
    
    /*Begin Other Essential Methods*/
    public void setMainPanel(StrixaAppletPanel panel){
        if(this.__main_panel != null){
            this.__main_panel.onPanelClose();
        }
        this.__main_panel = panel;
        
        this.setContentPane(this.__main_panel);
    }
    /*End Other Essential Methods*/
}

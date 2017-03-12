package edu.usfca.vas.layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Thomas Schweich on 1/17/2017.
 *
 * A JTabbedPane which reads an image destination from sidebar.json intended for use as the switcher between views
 */
public class LeftSideBar extends SideBar {

    private JSONReader settings;

    public LeftSideBar(int layout) {
        super(layout);
        settings = JSONReaders.SETTINGS.getReader();
    }

    /**
     * Adds the component as a tab -- effectively a shortcut for addTab(comp, name, true, width, height), but differs in
     * that it contains code for displaying the text
     * @param comp The component to add a tab for
     * @param name The name of the tab which is also the key for its entry in sidebar.json
     */


    public void addTab(Component comp, String name, int width, int height) {
    	addTab(comp, name, width, height, true);
    	}

    
    
    public void addTab(Component comp, String name, int width, int height, Boolean Enabled) {
        super.addSideTab(comp, getClass().getResource(settings.getValue("sidebar/" + name)).toString(), name,
                width, height, Enabled);
    }

    /**
     * Adds a component to the LeftSideBar as a tab, with an image who's path is obtained from the given name, which
     * doubles as the label for the tab if displayText is set to true.
     * @param comp The component to add as a tab
     * @param name The name and key for access the image representing the tab
     * @param displayText Whether or not to display the name under the tab
     * @param width The width to display the image
     * @param height The height to display the image
     */
    public void addTab(Component comp, String name, boolean displayText, int width, int height, Boolean Enabled) {
        super.addSideTab(comp, getClass().getResource(settings.getValue("sidebar/" + name)).toString(), name,
                displayText, width, height, Enabled);
    }

}

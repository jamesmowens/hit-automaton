package edu.usfca.vas.layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Thomas Schweich on 2/8/2017.
 *
 * Class for creating sidebars which contain images and text in a uniform arrangement
 */
public abstract class SideBar extends JTabbedPane {

    public SideBar(int layout) {
        super(layout);
    }

    /**
     * Adds the component as a tab -- effectively a shortcut for addTab(comp, name, true, width, height), but differs in
     * that it contains code for displaying the text
     * @param comp The component to add a tab for
     * @param name The name of the tab which is also the key for its entry in sidebar.json
     */
    public void addSideTab(Component comp, String imgPath, String name, int width, int height) {
        addTab("<html> " +
                "<img src=" + imgPath +
                " height=" + height +
                " width=" + width +
                " hspace=10> " +
                "<br> " +
                "<p>" + name + "</p> " +
                "</html>", comp);
    }

    /**
     * Adds a component to the LeftSideBar as a tab
     * @param comp The component to add as a tab
     * @param name The name and key for access the image representing the tab
     * @param displayText Whether or not to display the name under the tab
     * @param width The width to display the image
     * @param height The height to display the image
     */
    public void addSideTab(Component comp, String imgPath, String name, boolean displayText, int width, int height) {
        if(displayText) addSideTab(comp, imgPath, name, width, height);
        else {
            addTab("<html> " +
                    "<img src=" + imgPath +
                    " height=" + height +
                    " width=" + width +
                    " hspace=10> " +
                    "</html>", comp);
        }
    }
}

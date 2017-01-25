package edu.usfca.vas.layout;

import edu.usfca.xj.appkit.frame.XJWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Thomas on 1/24/2017.
 * The class representing the main window of the program, including the left-sidebar and the capability to add Containers
 * to the left-sidebar.
 */
public abstract class MainWindow extends XJWindow {
    private LeftSideBar leftSideBar;
    private Container subFrame;

    public MainWindow() {
        subFrame = new JPanel();
        subFrame.setPreferredSize(getJFrame().getPreferredSize());
        getJFrame().add(subFrame);
        leftSideBar = new LeftSideBar(JTabbedPane.LEFT);
        getJFrame().add(leftSideBar);
        addSideTab(new JPanel(), "Logo", false);
        addSideTab(subFrame, "Model");
        // The below lines can be replaced with calls to addSideTab from other classes -- i.e classes for
        // Analytics view and Map View
        addSideTab(new JPanel(), "Analytics");
        addSideTab(new JPanel(), "Map");
        leftSideBar.setSelectedIndex(1);
        leftSideBar.setVisible(true);
    }

    /**
     * Adds a JPanel whose image can be found under the given name in sidebar.json to the sidebar of main window
     * @param tab The JPanel to display upon clicking the tab
     * @param name The name to display under the tab and the string under which to look up the image in sidebar.json
     */
    public void addSideTab(Container tab, String name) {
        leftSideBar.addTab(tab, name);
    }

    /**
     * Adds a JPanel whose image can be found under the given name in sidebar.json to the sidebar of main window,
     * displaying its name if displayText is set to true
     * @param tab The JPanel to display upon clicking the tab
     * @param name The name to display under the tab if displayText is true and the string under which to look up the
     *             image in sidebar.json
     */
    public void addSideTab(Container tab, String name, boolean displayText) {
        if(displayText) {
            leftSideBar.addTab(tab, name);
        } else {
            leftSideBar.addTab(tab, name, false);
        }
    }

    @Override
    public Container getContentPane() {
        return subFrame;
    }
}
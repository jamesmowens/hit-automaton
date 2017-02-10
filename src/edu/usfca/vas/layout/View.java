package edu.usfca.vas.layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Thomas Schweich on 2/8/2017.
 *
 * Interface for views of the program (i.e. Model View, Analytics View, and Map View)
 */
public interface View {
    /**
     * @return The container with the contents of the view
     */
    Container getContainer();

    /**
     * @return The name of the View
     */
    String getName();



}

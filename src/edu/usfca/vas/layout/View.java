package edu.usfca.vas.layout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by thoma on 2/8/2017.
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

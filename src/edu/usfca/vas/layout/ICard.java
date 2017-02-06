package edu.usfca.vas.layout;

import java.awt.*;

/**
 * Created by Thomas on 1/17/2017.
 *
 * Interface which anything that is considered a main screen should implement in order to be toggled by buttons on the
 * left side
 */
public interface ICard {
    /**
     * @return The component in which all the swing elements of this ICard are placed
     */
    Component getMasterComp();
}

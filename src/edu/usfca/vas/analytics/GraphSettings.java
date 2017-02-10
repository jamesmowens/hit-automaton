package edu.usfca.vas.analytics;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Thomas Schweich on 2/9/2017.
 *
 * Class representing the settings associated with a graph such as labels and colors.
 */
public class GraphSettings {
    public String title = "", xLabel = "", yLabel = "", background = "#FFFFFF", lineColor = "#0000FF";

    /**
     * Uses reflection to set all members of this class to the values who's keys correspond to the members' names in
     * settings. Settings does not have to contain an entry for every member of this class.
     * @param settings Map from String to String of any desired settings
     */
    public GraphSettings(Map<String, String> settings) {
        for(Field f : getClass().getDeclaredFields()) {
            final String name = f.getName();
            if(settings.containsKey(f.getName())) {
                try {
                    f.set(this, settings.get(f.getName()));
                } catch (IllegalAccessException e) {
                    System.err.println("Couldn't access the desired setting \"" + name + "\"");
                    //Non-fatal -- field will simply be default value
                }
            }
        }
    }
}

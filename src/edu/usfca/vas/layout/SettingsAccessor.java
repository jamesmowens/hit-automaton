package edu.usfca.vas.layout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Thomas on 1/6/2017.
 *
 * Singleton for accessing settings stored in {String : String, ...} format
 */
public enum SettingsAccessor {
    INSTANCE;

    private Map<String, String> colors;

    /**
     * Creates the instance (INSTANCE) of SettingsAccessor, reading json file(s)
     */
    SettingsAccessor() {
        colors = accessSettings("src/edu/usfca/vas/layout/colors.json");
    }

    /**
     * Returns the settings stored at the given path in {@code {String : String, ...}} format in a {@code Map<String, String>}
     * @param path The location of the settings file
     * @return {@code Map<String, String>} of the (key, value) pairs in the settings file
     */
    private static Map<String, String> accessSettings(String path) {
        Gson gson = new Gson();
        Map<String, String> result;
        try {
            String json = new Scanner(new File(path)).useDelimiter("\\Z").next();
            result = gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find " + path + ". CWD: " + new File(".").getAbsoluteFile());
            result = new HashMap<String, String>();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Retrieves the color value stored under {@param key} from colors.json
     * @param key The key for the value to retrieve in colors.json
     * @return Color representing the hex value from colors.json
     */
    public static Color getColor(String key) {
        try {
            return Color.decode(INSTANCE.colors.get(key));
        } catch (NullPointerException n) {
            System.err.println("No matching entry for \"" + key + "\" in colors.json");
            return Color.GRAY;
        } catch (NumberFormatException n) {
            System.err.println("Bad format under \"" + key + "\" in colors.json");
            return Color.GRAY;
        }
    }
}

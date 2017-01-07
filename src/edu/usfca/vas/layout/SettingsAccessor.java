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
 * Created by thoma on 1/6/2017.
 */
public enum SettingsAccessor {
    INSTANCE;

    private Map<String, String> colors;

    SettingsAccessor() {
        Gson gson = new Gson();
        try {
            String json = new Scanner(new File("src/edu/usfca/vas/layout/colors.json")).useDelimiter("\\Z").next();
            colors = gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find colors.json" + " cwd: " + new File(".").getAbsoluteFile());
            colors = new HashMap<String, String>();
            e.printStackTrace();
        }
    }

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

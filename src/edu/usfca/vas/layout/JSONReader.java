package edu.usfca.vas.layout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Thomas on 2/1/2017.
 *
 * Class for reading JSON files in "root_setting/sub_setting/sub_setting2/..." format
 */
public class JSONReader {
    String path;

    /**
     * Creates a reader for a  file at the given path
     * @param path The path to the .json file
     */
    public JSONReader(String path) {
        this.path = path;
    }

    /**
     * Gets the value stored under location in the JSON file given by jsonPath
     * @param location Slash-separated path to the desired setting
     * @return The desired setting in String form
     */
    public String getValue(String location) {
        String txt;
        try {
            txt = new Scanner(new File(path)).useDelimiter("\\Z").next();
            System.out.println(txt);
            JsonObject json = new JsonParser().parse(txt).getAsJsonObject();
            String lastTerm;
            int lastSlashIndex = location.lastIndexOf("/");
            if(lastSlashIndex > 0)
                lastTerm = location.substring(lastSlashIndex + 1);
            else
                lastTerm = location;
            JsonObject terminal = getTerminalJsonObject(location, json);
            return terminal.get(lastTerm).getAsString();
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find " + path + ". CWD: " + new File(".").getAbsoluteFile());
            e.printStackTrace();
        }
        return "";
    }

    int i = 0;
    /**
     * Recursively finds the given setting under the given slash-separated path
     * @param location The location of the desired setting in slash-separated (filesystem-like) format
     * @param jsonObject The jsonObject to be searched
     * @return
     */
    private JsonObject getTerminalJsonObject(String location, JsonObject jsonObject) {
        System.out.println("Iterations :" + ++i);
        final int slashLoc = location.indexOf("/");
        if(!(slashLoc > 0)) {
            return jsonObject;
        } else {
            final String currLoc = location.substring(0, slashLoc);
            final String nextLoc = location.substring(slashLoc + 1);
            return getTerminalJsonObject(nextLoc, jsonObject.get(currLoc).getAsJsonObject());
        }
    }
}

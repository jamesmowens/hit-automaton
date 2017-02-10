package edu.usfca.vas.layout;

import com.google.gson.*;
import com.orsoncharts.util.json.JSONArray;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Thomas Schweich on 2/1/2017.
 *
 * Class for reading JSON files in "root_setting/sub_setting/sub_setting2/..." format
 */
public class JSONReader {
    JsonObject json;

    /**
     * Creates a reader for a  file at the given path
     * @param path The path to the .json file
     */
    public JSONReader(String path) throws FileNotFoundException {
        String txt;
        txt = new Scanner(new File(path)).useDelimiter("\\Z").next();
        json = new JsonParser().parse(txt).getAsJsonObject();
    }

    /**
     * Creates a reader for the given JsonObject
     * @param jsonObject The JsonObject to read
     */
    public JSONReader(JsonObject jsonObject) {
        this.json = jsonObject;
    }

    /**
     * Gets the JsonObject at the given location
     * @param location Slash-separated path to the desired JsonObject
     * @return JsonObject at the given locatin
     */
    public JsonObject getJsonObject(String location) {
        return readTerminalElement(location).getAsJsonObject();
    }

    /**
     * Gets the value stored under location in the JSON file given by jsonPath as a String
     * @param location Slash-separated path to the desired setting
     * @return The desired setting in String form
     */
    public String getValue(String location) {
        return readTerminalElement(location).getAsString();
    }

    /**
     * Gets the value stored under location in the JSON file given by jsonPath as a Number
     * @param location Slash-separated path to the desired setting
     * @return The desired setting in String form
     */
    public Number getNumberValue(String location) {
        return readTerminalElement(location).getAsNumber();
    }

    /**
     * Gets the value stored under location in the JSON file given by jsonPath as a JsonArray
     * @param location Slash-separated path to the desired setting
     * @return The desired setting in String form
     */
    public JsonArray getJsonArrayValue(String location) {
        return readTerminalElement(location).getAsJsonArray();
    }

    /**
     * Gets the value stored under location in the JSON file as a Map with Strings as keys and values.
     * Underlying map is a HashMap.
     * @param location Slash-separated path to the desired setting
     * @return The desired setting as a Map
     */
    public Map<String, String> getAsSSMap(String location) {
        return (new Gson()).fromJson(readTerminalElement(location), (new HashMap<String, String>()).getClass());
    }

    /**
     * Gives the last JsonElement, free of type, found by following location in the file given by this JSONReader's
     * path
     * @param location The slash-separated location of the value to find
     * @return JsonElement representing the object at the location
     * @throws FileNotFoundException if
     */
    private JsonElement readTerminalElement(String location) {
        String lastTerm;
        int lastSlashIndex = location.lastIndexOf("/");
        if(lastSlashIndex > 0)
            lastTerm = location.substring(lastSlashIndex + 1);
        else
            lastTerm = location;
        JsonObject terminal = getTerminalJsonObject(location, json);
        return terminal.get(lastTerm);
    }

    /**
     * Recursively finds the given setting under the given slash-separated path
     * @param location The location of the desired setting in slash-separated (filesystem-like) format
     * @param jsonObject The jsonObject to be searched
     * @return The last JSONObject (dictionary-like) in the path -- NOT the result
     */
    private JsonObject getTerminalJsonObject(String location, JsonObject jsonObject) {
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

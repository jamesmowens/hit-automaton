package Query;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Thomas Schweich on 3/8/2017.
 *
 * Class representing a saved set of queries. Parsing is super memory inefficient. For now, meant
 * to be robust. Would not work for a huge amount of data in its current state.
 */
public class DataQuerySave {

    HashMap<String, LinkedList<Query>> queryDatabase;

    /**
     * Creates a DataQuerySave containing the given data. Does not write to disk.
     * @param toSave The data to save
     */
    public DataQuerySave(HashMap<String, LinkedList<Query>> toSave) {
        saveData(toSave);
    }

    /**
     * Creates a DataQuerySave from the contents of the given file
     * @param save The json file to create from
     */
    public DataQuerySave(File save) {
        loadFile(save);
    }

    /**
     * Creates an empty DataQuerySave
     */
    public DataQuerySave() {}

    /**
     * Adds the given data to this DataQuerySave. Does not write to file.
     * @param data The data to add
     */
    public void saveData(HashMap<String, LinkedList<Query>> data) {
        queryDatabase = data;
    }

    /**
     * Loads the file's contents into this object
     * @param f The file to load
     */
    public void loadFile(File f) {
        try {
            loadFromString(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads json text into this object
     * @param source The JSON string
     */
    public void loadFromString(String source) {
        Gson gson = new Gson();
        DataQuerySave result = gson.fromJson(source, getClass());
        queryDatabase = result.queryDatabase;
    }

    /**
     * Gets the save as a String
     * @return the saved data in JSON format
     */
    public String getAsJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Writes the save to a file
     * @param f The File to write to
     */
    public void writeToFile(File f) {
        try(PrintStream out = new PrintStream(new FileOutputStream(f))) {
            out.print(getAsJson());
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't write to file");
            e.printStackTrace();
        }
    }

    /**
     * Creates a file at the given location/name and saves to the file
     * @param fName The name/location of the file to create and save to
     */
    public void writeToFile(String fName) {
        writeToFile(new File(fName));
    }

    public HashMap<String, LinkedList<Query>> getQueryDatabase() {
        return queryDatabase;
    }
}

package Query;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import connection.Step;

import java.io.*;
import java.lang.reflect.Type;
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

    private HashMap<String, LinkedList<Query>> queryDatabase;

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
        System.out.println("String being loaded: " + source);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Query.class, new QueryDeserializer())
                .create();
        DataQuerySave result = gson.fromJson(source, DataQuerySave.class);
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

    /**
     * Class to deserialize Queries using GSON
     */
    class QueryDeserializer implements JsonDeserializer<Query> {
        @Override
        public Query deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = element.getAsJsonObject();
            if(obj.has("successStep")) {
                return new TransitionQuery(
                        obj.get("state").getAsString(),
                        obj.get("info").getAsString(),
                        context.deserialize(obj.get("ex"), Condition.class),
                        context.deserialize(obj.get("successStep"), Step.class));
            } else {
                return new VariableQuery(
                        obj.get("state").getAsString(),
                        obj.get("info").getAsString(),
                        context.deserialize(obj.get("ex"), Condition.class),
                        obj.get("set").getAsString());
            }
        }
    }

    /*class QueryDeserializer extends TypeAdapter<Query> {

        /**
         * Used by GSON internally. Encodes the type of query as well as all of its data.
         */
        //@Override
        /*public void write(JsonWriter jsonWriter, Query query) throws IOException {
            if(query == null) {
                jsonWriter.nullValue();
            } else {
                JsonElement asJson = new Gson().toJsonTree(query);
                if(query instanceof TransitionQuery) {
                    asJson.getAsJsonObject().addProperty("type", "transition");
                } else if (query instanceof VariableQuery) {
                    asJson.getAsJsonObject().addProperty("type", "variable");
                }
                Streams.write(asJson, jsonWriter);
            }
        }

        @Override
        public Query read(JsonReader jsonReader) throws IOException {
            JsonObject obj = Streams.parse(jsonReader).getAsJsonObject();
            Gson gson = new Gson();
            if(obj.get("type").getAsString().equals("transition")) {
                return new TransitionQuery((Condition) obj.get("condition"), obj.get());
            }
        }
    }*/
}

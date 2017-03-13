package Query;

import com.google.gson.*;
import connection.Step;
import edu.usfca.xj.appkit.document.XJDocument;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Thomas Schweich on 3/8/2017.
 *
 * Class representing a saved project
 */
public class ProjectSave {

    private HashMap<String, LinkedList<Query>> queryDatabase;
    private String faLoc, dataLoc;
    private XJDocument faDoc;

    /**
     * Creates a ProjectSave containing the given queries. Does not write to disk.
     * @param toSave The queries to save
     */
    public ProjectSave(HashMap<String, LinkedList<Query>> toSave) {
        saveData(toSave);
    }

    /**
     * Creates a ProjectSave containing the given queries. Does not write to disk.
     * @param queries The database of queries to save
     * @param faLoc The location of the .fa file to save
     * @param dataLoc The location of the data xml file to save
     */
    public ProjectSave(HashMap<String, LinkedList<Query>> queries, String faLoc, String dataLoc) {
        saveData(queries, faLoc, dataLoc);
    }

    /**
     * Creates a ProjectSave containing the given data. Does not write to disk.
     * @param queries The database of queries to save
     * @param faDoc The location of the .fa file to save
     * @param dataLoc The location of the data xml file to save
     */
    public ProjectSave(HashMap<String, LinkedList<Query>> queries, XJDocument faDoc, String dataLoc) {
        saveData(queries, faDoc, dataLoc);
    }

    /**
     * Creates a ProjectSave from the contents of the given file
     * @param save The json file to create from
     */
    public ProjectSave(File save) {
        loadFile(save);
    }

    /**
     * Creates an empty ProjectSave. Do not delete; used by GSON.
     */
    public ProjectSave() {}

    /**
     * Adds the given data to this ProjectSave. Does not write to file.
     * @param data The data to add
     */
    public void saveData(HashMap<String, LinkedList<Query>> data) {
        queryDatabase = data;
    }

    /**
     * Adds the given data to this ProjectSave. Does not write to file.
     * @param queries The database of queries
     * @param faLoc The location of the .fa file
     * @param dataLoc The location of the data xml file
     */
    public void saveData(HashMap<String, LinkedList<Query>> queries, String faLoc, String dataLoc) {
        this.queryDatabase = queries;
        this.faLoc = faLoc;
        this.dataLoc = dataLoc;
    }


    /**
     * Adds the given data to this ProjectSave. Does not write to file.
     * @param queries The database of queries
     * @param faDoc The location of the .fa file
     * @param dataLoc The location of the data xml file
     */
    public void saveData(HashMap<String, LinkedList<Query>> queries, XJDocument faDoc, String dataLoc) {
        this.queryDatabase = queries;
        this.faDoc = faDoc;
        this.dataLoc = dataLoc;
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
        ProjectSave result = gson.fromJson(source, ProjectSave.class);
        this.queryDatabase = result.queryDatabase;
        this.faLoc = result.faLoc;
        this.faDoc = result.faDoc;
        this.dataLoc = result.dataLoc;
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

    public String getFaLoc() { return faLoc; }

    public String getDataLoc() { return dataLoc; }

    public XJDocument getFaDoc() { return faDoc; }

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
                        obj.get("pattern").getAsString(),
                        context.deserialize(obj.get("ex"), Condition.class),
                        context.deserialize(obj.get("successStep"), Step.class));
            } else {
                return new VariableQuery(
                        obj.get("state").getAsString(),
                        obj.get("info").getAsString(),
                        obj.get("pattern").getAsString(),
                        context.deserialize(obj.get("ex"), Condition.class),
                        obj.get("set").getAsString());
            }
        }
    }
}

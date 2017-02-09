package edu.usfca.vas.layout;

import java.io.FileNotFoundException;

/**
 * Created by Thomas on 1/6/2017.
 *
 * Enum for accessing settings stored as Strings in JSON format
 */
public enum JSONReaders {
    SETTINGS;

    JSONReader reader;

    // Sets the proper JSONReader to instances. EACH INSTANCE MUST HAVE A PROPER JSONReader!
    static {
        try {
            SETTINGS.reader = new JSONReader("src/edu/usfca/vas/layout/settings.json");
        } catch (FileNotFoundException f) {
            System.err.println("Couldn't find settings.json");
            f.printStackTrace();
        }
    }

    public String getValue(String location) {
        return reader.getValue(location);
    }

    public JSONReader getReader() {
        return reader;
    }
}

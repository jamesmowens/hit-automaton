package edu.usfca.vas.layout;

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
        SETTINGS.reader = new JSONReader("src/edu/usfca/vas/layout/settings.json");
    }

    public String getValue(String location) {
        return reader.getValue(location);
    }
}

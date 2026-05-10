package it.polimi.ingsw.config;

import com.google.gson.Gson;

import java.io.FileReader;

/**
 * Utility class used to load application configuration
 * from a JSON file.
 *
 * @author Andrea Markvukaj
 */
public class ConfigLoader {

    public static DBConfiguration load() {
        try {
            return new Gson().fromJson(new FileReader("dbconfig.json"), DBConfiguration.class);
        } catch (Exception e) {
            System.out.println("! dbconfig.json not found or invalid → using fallback");
            return null;
        }
    }
}
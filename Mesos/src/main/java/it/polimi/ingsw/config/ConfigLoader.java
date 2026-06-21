package it.polimi.ingsw.config;

import java.util.Map;

/**
 * Utility class used to load application configuration
 * from environment variables.
 *
 * @author Andrea Markvukaj
 */
public class ConfigLoader {

    public static DBConfiguration load() {
        return load(System.getenv());
    }

    static DBConfiguration load(Map<String, String> environment) {

        String url  = environment.get("DB_URL");
        String user = environment.get("DB_USER");
        String pass = environment.get("DB_PASSWORD");

        if (url == null || user == null || pass == null) {
            System.out.println("! DB_URL, DB_USER or DB_PASSWORD not set → leaderboard disabled");
            return null;
        }

        DBConfiguration config = new DBConfiguration();
        config.dbUrl = url;
        config.dbUser = user;
        config.dbPassword = pass;

        return config;
    }
}

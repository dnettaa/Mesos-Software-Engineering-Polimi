package it.polimi.ingsw.config;

/**
 * Utility class used to load application configuration
 * from environment variables.
 *
 * @author Andrea Markvukaj
 */
public class ConfigLoader {

    public static DBConfiguration load() {
        String url  = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");

        if (url == null || user == null || pass == null) {
            System.out.println("! DB_URL, DB_USER or DB_PASSWORD not set → leaderboard disabled");
            return null;
        }

        DBConfiguration config = new DBConfiguration();
        config.dbUrl      = url;
        config.dbUser     = user;
        config.dbPassword = pass;
        return config;
    }
}
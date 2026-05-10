package it.polimi.ingsw.config;

/**
 * Simple configuration class used to map database connection
 * parameters from dbconfig.json via Gson.
 *
 * @author Andrea Markvukaj
 */
public class DBConfiguration {

    public String dbUrl;
    public String dbUser;
    public String dbPassword;
}
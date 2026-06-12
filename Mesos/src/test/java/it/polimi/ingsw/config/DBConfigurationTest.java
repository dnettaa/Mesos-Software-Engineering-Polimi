package it.polimi.ingsw.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the database configuration data holder used by Gson when loading
 * {@code dbconfig.json}.
 *
 * @author Diana
 */
class DBConfigurationTest {

    /**
     * Verifies that the database configuration exposes the fields expected by
     * the application startup code.
     * Setup: a new configuration object is created and its public fields are assigned.
     * Action: the fields are read back directly, as Gson and the controller do.
     * Expected behavior: each field preserves the assigned value.
     * Edge case covered: the test protects the simple field names used by JSON mapping.
     */
    @Test
    void configurationFieldsShouldStoreDatabaseConnectionValues() {
        DBConfiguration configuration = new DBConfiguration();

        configuration.dbUrl = "jdbc:postgresql://localhost:5432/mesos";
        configuration.dbUser = "mesos_user";
        configuration.dbPassword = "secret";

        assertEquals("jdbc:postgresql://localhost:5432/mesos", configuration.dbUrl);
        assertEquals("mesos_user", configuration.dbUser);
        assertEquals("secret", configuration.dbPassword);
    }
}

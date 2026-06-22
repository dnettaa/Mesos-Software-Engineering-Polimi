package it.polimi.ingsw.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests database configuration loading from the environment variables used
 * by the application startup flow.
 *
 * @author Diana
 */
class ConfigLoaderTest {

    /**
     * Verifies that the configuration loader falls back safely when the database
     * environment variables are absent.
     * Setup: no database environment variables are provided.
     * Action: load the database configuration.
     * Expected behavior: the loader returns {@code null}.
     * Edge case covered: running the application without local database credentials must not crash.
     */
    @Test
    void loadShouldReturnNullWhenRequiredEnvironmentVariablesAreMissing() {
        DBConfiguration configuration = ConfigLoader.load(Map.of());

        assertNull(configuration);
    }

    /**
     * Verifies that valid environment variables are mapped to a
     * {@link DBConfiguration} instance.
     * Setup: environment values contain URL, user, and password properties.
     * Action: load the database configuration.
     * Expected behavior: all public configuration fields contain the values from the environment.
     * Edge case covered: environment variable names must match the keys expected by production startup code.
     */
    @Test
    void loadShouldMapValidEnvironmentVariables() {
        DBConfiguration configuration = ConfigLoader.load(Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/mesos",
                "DB_USER", "mesos_user",
                "DB_PASSWORD", "secret"
        ));

        assertEquals("jdbc:postgresql://localhost:5432/mesos", configuration.dbUrl);
        assertEquals("mesos_user", configuration.dbUser);
        assertEquals("secret", configuration.dbPassword);
    }

    /**
     * Verifies that partial database environment configuration is treated as unavailable.
     * Setup: only some required database environment variables are provided.
     * Action: load the database configuration.
     * Expected behavior: the loader returns {@code null}.
     * Edge case covered: incomplete credentials must not create an unusable database configuration.
     */
    @Test
    void loadShouldReturnNullWhenOnlySomeEnvironmentVariablesAreSet() {
        DBConfiguration configuration = ConfigLoader.load(Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/mesos",
                "DB_USER", "mesos_user"
        ));

        assertNull(configuration);
    }
}

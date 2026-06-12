package it.polimi.ingsw.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests database configuration loading from the {@code dbconfig.json} file used
 * by the application startup flow.
 *
 * @author Diana
 */
class ConfigLoaderTest {

    private static final Path CONFIG_PATH = Path.of("dbconfig.json");

    private byte[] originalConfigContent;
    private boolean originalConfigExisted;

    @BeforeEach
    void setUp() throws IOException {
        originalConfigExisted = Files.exists(CONFIG_PATH);
        originalConfigContent = originalConfigExisted ? Files.readAllBytes(CONFIG_PATH) : null;
        Files.deleteIfExists(CONFIG_PATH);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(CONFIG_PATH);

        if (originalConfigExisted) {
            Files.write(CONFIG_PATH, originalConfigContent);
        }
    }

    /**
     * Verifies that the configuration loader falls back safely when the database
     * configuration file is absent.
     * Setup: no {@code dbconfig.json} file exists in the working directory.
     * Action: load the database configuration.
     * Expected behavior: the loader returns {@code null}.
     * Edge case covered: running the application without local database credentials must not crash.
     */
    @Test
    void loadShouldReturnNullWhenConfigurationFileIsMissing() {
        DBConfiguration configuration = ConfigLoader.load();

        assertNull(configuration);
    }

    /**
     * Verifies that a valid JSON configuration file is mapped to a
     * {@link DBConfiguration} instance.
     * Setup: {@code dbconfig.json} contains URL, user, and password properties.
     * Action: load the database configuration.
     * Expected behavior: all public configuration fields contain the values from the JSON file.
     * Edge case covered: Gson field mapping must match the property names expected by production startup code.
     */
    @Test
    void loadShouldMapValidConfigurationFile() throws IOException {
        Files.writeString(CONFIG_PATH, """
                {
                  "dbUrl": "jdbc:postgresql://localhost:5432/mesos",
                  "dbUser": "mesos_user",
                  "dbPassword": "secret"
                }
                """);

        DBConfiguration configuration = ConfigLoader.load();

        assertEquals("jdbc:postgresql://localhost:5432/mesos", configuration.dbUrl);
        assertEquals("mesos_user", configuration.dbUser);
        assertEquals("secret", configuration.dbPassword);
    }

    /**
     * Verifies that malformed JSON is treated as an unavailable configuration.
     * Setup: {@code dbconfig.json} exists but contains invalid JSON syntax.
     * Action: load the database configuration.
     * Expected behavior: the loader catches the parse failure and returns {@code null}.
     * Edge case covered: a corrupted local configuration file must not prevent the application from starting.
     */
    @Test
    void loadShouldReturnNullWhenConfigurationFileIsInvalid() throws IOException {
        Files.writeString(CONFIG_PATH, "{ invalid json");

        DBConfiguration configuration = ConfigLoader.load();

        assertNull(configuration);
    }
}

package it.polimi.ingsw.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.phase.Phase;

import java.io.*;

/**
 * Utility class responsible for managing game persistence.
 * The game state is serialized into JSON using Gson.
 * To reduce the risk of save corruption during crashes,
 * the game is first written to a temporary file and then
 * atomically promoted to the main save file.
 *
 * @author Andrea Markvukaj
 */
public class PersistenceManager {

    private static final String SAVE_FILE = "mesos_save.json";
    private static final String TEMP_FILE = "mesos_save.tmp";

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Card.class, new InterfaceAdapter<>())
            .registerTypeHierarchyAdapter(Phase.class, new InterfaceAdapter<>())
            .setPrettyPrinting()
            .create();

    /**
     * Private constructor for utility class.
     */
    private PersistenceManager() {
    }

    /**
     * Saves the current game state.
     * <p>
     * The game is first serialized into a temporary file.
     * If the operation succeeds, the temporary file replaces
     * the main save file.
     *
     * @param game the game instance to serialize
     */
    public static void saveGame(GameActions game) {

        File tempFile = new File(TEMP_FILE);
        File saveFile = new File(SAVE_FILE);

        try (Writer writer = new FileWriter(tempFile)) {
            gson.toJson(game, writer);

        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Error writing temp save: " + e.getMessage());
            return;
        }

        if (saveFile.exists() && !saveFile.delete()) {
            System.err.println("[PERSISTENCE] Unable to delete old save file");
            return;
        }

        if (!tempFile.renameTo(saveFile)) {
            System.err.println("[PERSISTENCE] Failed to promote temp save");
            return;
        }

        System.out.println("[PERSISTENCE] Game saved successfully");
    }

    /**
     * Loads the latest valid game state.
     * <p>
     * The method first attempts to load the main save file.
     * If loading fails, it falls back to the temporary file.
     *
     * @return the loaded game instance, or {@code null} if no valid save exists
     */
    public static GameActions loadGame() {

        GameActions game = tryLoad(SAVE_FILE);

        if (game != null) {
            System.out.println("[PERSISTENCE] Loaded main save");
            return game;
        }

        game = tryLoad(TEMP_FILE);

        if (game != null) {
            System.out.println("[PERSISTENCE] Loaded fallback save");
            return game;
        }

        System.out.println("[PERSISTENCE] No valid save found");
        return null;
    }

    /**
     * Attempts to load a game state from a specific file.
     *
     * @param path path of the save file
     * @return the loaded game instance, or {@code null} if loading fails
     */
    private static GameActions tryLoad(String path) {

        File file = new File(path);

        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            Game game = gson.fromJson(reader, Game.class);
            game.restoreReferencesAfterLoad();
            return game;

        } catch (Exception e) {
            System.err.println("[PERSISTENCE] Corrupted save file: " + path);
            return null;
        }
    }

    /**
     * Deletes all save files.
     */
    public static void deleteSave() {

        deleteFile(SAVE_FILE);
        deleteFile(TEMP_FILE);

        System.out.println("[PERSISTENCE] Save files removed");
    }

    /**
     * Deletes a save file if it exists.
     *
     * @param path path of the file to delete
     */
    private static void deleteFile(String path) {

        File file = new File(path);

        if (file.exists() && !file.delete()) {
            System.err.println("[PERSISTENCE] Unable to delete " + path);
        }
    }
}

package it.polimi.ingsw.network.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameActions;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.game.phase.Phase;

import java.io.*;

/**
 * Utility class responsible for managing the persistence of the game state.
 * <p>
 * It handles the serialization and deserialization of the main {@link Game} object
 * to and from a local JSON file using the GSON library. This allows the server
 * to recover from unexpected crashes by restoring the exact state of an ongoing match.
 * </p>
 */
public class PersistenceManager {

    /** The default file path used for storing the JSON backup. */
    private static final String SAVE_FILE = "mesos_persistence.json";

    /** * The uniquely configured GSON instance.
     * It registers custom hierarchy adapters to properly handle the polymorphism
     * of abstract classes and interfaces (such as {@link Card} and {@link Phase})
     * during the JSON conversion process.
     */
    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Card.class, new InterfaceAdapter<>())
            .registerTypeHierarchyAdapter(Phase.class, new InterfaceAdapter<>())
            .setPrettyPrinting()
            .create();

    /**
     * Serializes the current game state and saves it to the disk.
     * Overwrites any existing save file.
     * * @param game The {@link GameActions} instance representing the current state of the game to be persisted.
     */
    public static void saveGame(GameActions game) {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(game, writer);
            System.out.println("[PERSISTENCE] Stato della partita salvato con successo.");
        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Errore durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * Attempts to read and deserialize a previously saved game state from the disk.
     * * @return The reconstructed {@link GameActions} instance, or {@code null} if no save file exists or if the file is corrupted.
     */
    public static GameActions loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            GameActions loadedGame = gson.fromJson(reader, Game.class);
            System.out.println("[PERSISTENCE] Partita ripristinata dal disco!");
            return loadedGame;
        } catch (Exception e) {
            System.err.println("[PERSISTENCE] Impossibile caricare il salvataggio: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes the local save file.
     * This should be called when a game concludes naturally, freeing up the disk
     * and preventing the server from attempting a recovery on a finished game.
     */
    public static void deleteSave() {
        File file = new File(SAVE_FILE);
        if (file.exists() && file.delete()) {
            System.out.println("[PERSISTENCE] Salvataggio rimosso (partita terminata).");
        }
    }
}
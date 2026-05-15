package it.polimi.ingsw.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.phase.Phase;

import java.io.*;

/**
 * Utility class responsible for managing the persistence of the game state.
 * It serializes and deserializes the {@link Game} instance using JSON.
 * To prevent data corruption in case of crashes during write operations,
 * it uses a double-file strategy (A/B).
 * Strategy:
 * - Always write to the "temp" file first
 * - If successful, replace the main file atomically
 * - On load, try main file first, then fallback to temp
 */
public class PersistenceManager {

    private static final String SAVE_FILE_A = "mesos_save_A.json";
    private static final String SAVE_FILE_B = "mesos_save_B.json";

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Card.class, new InterfaceAdapter<>())
            .registerTypeHierarchyAdapter(Phase.class, new InterfaceAdapter<>())
            .setPrettyPrinting()
            .create();

    /**
     * Saves the current game state using a double-file strategy.
     */
    public static void saveGame(GameActions game) {
        File fileA = new File(SAVE_FILE_A);
        File fileB = new File(SAVE_FILE_B);

        String target;

        if (!fileA.exists()) {
            target = SAVE_FILE_A;
        } else if (!fileB.exists()) {
            target = SAVE_FILE_B;
        } else {
            target = (fileA.lastModified() <= fileB.lastModified())
                    ? SAVE_FILE_A
                    : SAVE_FILE_B;
        }

        File tempFile = new File(target + ".tmp");
        File finalFile = new File(target);

        try (Writer writer = new FileWriter(tempFile)) {
            gson.toJson(game, writer);
        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Errore scrittura temp: " + e.getMessage());
            return;
        }

        if (finalFile.exists() && !finalFile.delete()) {
            System.err.println("[PERSISTENCE] Impossibile eliminare file vecchio");
            return;
        }

        if (!tempFile.renameTo(finalFile)) {
            System.err.println("[PERSISTENCE] Rename fallita");
            return;
        }

        System.out.println("[PERSISTENCE] Salvataggio completato su " + target);
    }

    /**
     * Loads the game state trying both files (A then B).
     */
    public static GameActions loadGame() {
        GameActions game = tryLoad(SAVE_FILE_A);

        if (game != null) {
            System.out.println("[PERSISTENCE] Caricato da A");
            return game;
        }

        game = tryLoad(SAVE_FILE_B);

        if (game != null) {
            System.out.println("[PERSISTENCE] Caricato da B");
            return game;
        }

        System.out.println("[PERSISTENCE] Nessun salvataggio valido trovato");
        return null;
    }

    /**
     * Attempts to load a game from a specific file.
     */
    private static GameActions tryLoad(String path) {
        File file = new File(path);
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, Game.class);
        } catch (Exception e) {
            System.err.println("[PERSISTENCE] File corrotto: " + path);
            return null;
        }
    }

    /**
     * Deletes both save files.
     */
    public static void deleteSave() {
        deleteFile(SAVE_FILE_A);
        deleteFile(SAVE_FILE_B);
        System.out.println("[PERSISTENCE] Salvataggi rimossi");
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) file.delete();
    }
}
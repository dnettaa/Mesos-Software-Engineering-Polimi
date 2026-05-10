package it.polimi.ingsw.network.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameActions;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.game.phase.Phase;

import java.io.*;

/**
 * Gestisce il salvataggio e il caricamento dello stato della partita su disco utilizzando GSON.
 */
public class PersistenceManager {

    private static final String SAVE_FILE = "mesos_persistence.json";

    // --- IL NUOVO GSON BUILDER "INTELLIGENTE" ---
    private static final Gson gson = new GsonBuilder()
            // Diciamo a GSON di usare l'Adapter per le Carte (incluse tutte le sue sottoclassi astratte)
            .registerTypeHierarchyAdapter(Card.class, new InterfaceAdapter<>())
            // Diciamo a GSON di usare l'Adapter anche per le Fasi del gioco (InGamePhase, EndRoundPhase, ecc.)
            .registerTypeHierarchyAdapter(Phase.class, new InterfaceAdapter<>())
            .setPrettyPrinting()
            .create();
    /**
     * Salva l'istanza corrente del gioco su file.
     * @param game l'oggetto Game da persistere.
     */
    public static void saveGame(GameActions game) {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            // GSON salverà i campi della classe concreta Game passata come parametro
            gson.toJson(game, writer);
            System.out.println("[PERSISTENCE] Stato della partita salvato con successo.");
        } catch (IOException e) {
            System.err.println("[PERSISTENCE] Errore durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * Carica una partita precedentemente salvata dal disco.
     * @return l'oggetto GameActions ricostruito, oppure null se non esiste un salvataggio.
     */
    public static GameActions loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            // Specifichiamo la classe concreta Game.class per la deserializzazione
            GameActions loadedGame = gson.fromJson(reader, Game.class);
            System.out.println("[PERSISTENCE] Partita ripristinata dal disco!");
            return loadedGame;
        } catch (Exception e) {
            System.err.println("[PERSISTENCE] Impossibile caricare il salvataggio: " + e.getMessage());
            return null;
        }
    }

    /**
     * Elimina il salvataggio a partita conclusa.
     */
    public static void deleteSave() {
        File file = new File(SAVE_FILE);
        if (file.exists() && file.delete()) {
            System.out.println("[PERSISTENCE] Salvataggio rimosso (partita terminata).");
        }
    }
}
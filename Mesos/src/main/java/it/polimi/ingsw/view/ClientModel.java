package it.polimi.ingsw.view;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.network.message.OfferSlotData;
import it.polimi.ingsw.network.message.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replica locale dello stato di gioco (Dumb Model).
 * Viene aggiornato dai messaggi delta del server.
 */
public class ClientModel {
    private int currentRound;
    private Era currentEra;
    private String currentPhaseName;
    private String currentPlayerNickname;
    private List<Character> resolutionOrder;
    private List<String> turnOrder;
    private int tribeDeckRemaining;
    private List<String> upperRowCardIDs;
    private List<String> lowerRowCardIDs;
    private List<OfferSlotData> offerSlots;
    private Map<String, PlayerData> players;

    public ClientModel() {
        this.resolutionOrder = new ArrayList<>();
        this.turnOrder = new ArrayList<>();
        this.upperRowCardIDs = new ArrayList<>();
        this.lowerRowCardIDs = new ArrayList<>();
        this.offerSlots = new ArrayList<>();
        this.players = new HashMap<>();
    }

    // =========================================================
    // MUTATORI (Chiamati dai messaggi delta, es. EventResolvedMessage)
    // =========================================================

    public void removeUpperCard(String cardID) {
        if (upperRowCardIDs != null) {
            upperRowCardIDs.remove(cardID);
        }
    }

    public void removeLowerCard(String cardID) {
        if (lowerRowCardIDs != null) {
            lowerRowCardIDs.remove(cardID);
        }
    }

    public void addTribeCardTo(String nickname, String cardID) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            List<String> tribes = old.getTribeCardID();
            tribes.add(cardID);
            // PlayerData è immutabile, creiamo un rimpiazzo aggiornato
            PlayerData updated = new PlayerData(
                    old.getNickname(), old.getTotemColor(), old.getFood(),
                    old.getPrestigePoints(), tribes, old.getBuildingID()
            );
            players.put(nickname, updated);
        }
    }

    public void addBuildingTo(String nickname, String cardID) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            List<String> builds = old.getBuildingID();
            builds.add(cardID);
            PlayerData updated = new PlayerData(
                    old.getNickname(), old.getTotemColor(), old.getFood(),
                    old.getPrestigePoints(), old.getTribeCardID(), builds
            );
            players.put(nickname, updated);
        }
    }

    public void adjustFood(String nickname, int delta) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            int newFood = Math.max(0, old.getFood() + delta); // Evita cibo negativo
            PlayerData updated = new PlayerData(
                    old.getNickname(), old.getTotemColor(), newFood,
                    old.getPrestigePoints(), old.getTribeCardID(), old.getBuildingID()
            );
            players.put(nickname, updated);
        }
    }

    public void adjustPP(String nickname, int delta) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            int newPP = Math.max(0, old.getPrestigePoints() + delta);
            PlayerData updated = new PlayerData(
                    old.getNickname(), old.getTotemColor(), old.getFood(),
                    newPP, old.getTribeCardID(), old.getBuildingID()
            );
            players.put(nickname, updated);
        }
    }

    public void placeTotemOnSlot(String nickname, char slotID) {
        /*
         * Nota: OfferSlotData è probabilmente immutabile. Se i tuoi colleghi
         * hanno aggiunto dei setter usa quelli, altrimenti devi sostituire l'oggetto
         * nella lista con un nuovo `new OfferSlotData(slotID, up, down, food, nickname)`
         */
        // Da implementare a seconda del costruttore esatto di OfferSlotData
    }

    public void freeSlot(char slotID) {
        // Come sopra, da implementare rimpiazzando l'oggetto per svuotare l'occupante
    }


    // =========================================================
    // SETTER COMPLETI (Usati dal GameStateMessage principale)
    // =========================================================

    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }
    public void setCurrentEra(Era currentEra) { this.currentEra = currentEra; }
    public void setCurrentPhaseName(String currentPhaseName) { this.currentPhaseName = currentPhaseName; }
    public void setCurrentPlayerNickname(String currentPlayerNickname) { this.currentPlayerNickname = currentPlayerNickname; }
    public void setResolutionOrder(List<Character> resolutionOrder) { this.resolutionOrder = new ArrayList<>(resolutionOrder); }
    public void setTurnOrder(List<String> turnOrder) { this.turnOrder = new ArrayList<>(turnOrder); }
    public void setTribeDeckRemaining(int tribeDeckRemaining) { this.tribeDeckRemaining = tribeDeckRemaining; }
    public void setUpperRowCardIDs(List<String> upperRowCardIDs) { this.upperRowCardIDs = new ArrayList<>(upperRowCardIDs); }
    public void setLowerRowCardIDs(List<String> lowerRowCardIDs) { this.lowerRowCardIDs = new ArrayList<>(lowerRowCardIDs); }
    public void setOfferSlots(List<OfferSlotData> offerSlots) { this.offerSlots = new ArrayList<>(offerSlots); }
    public void setPlayers(Map<String, PlayerData> players) { this.players = new HashMap<>(players); }


    // =========================================================
    // GETTER (Usati dalla TUI e GUI per disegnare la plancia)
    // =========================================================

    public int getCurrentRound() { return currentRound; }
    public Era getCurrentEra() { return currentEra; }
    public String getCurrentPhaseName() { return currentPhaseName; }
    public String getCurrentPlayerNickname() { return currentPlayerNickname; }
    public List<Character> getResolutionOrder() { return resolutionOrder; }
    public List<String> getTurnOrder() { return turnOrder; }
    public int getTribeDeckRemaining() { return tribeDeckRemaining; }
    public List<String> getUpperRowCardIDs() { return upperRowCardIDs; }
    public List<String> getLowerRowCardIDs() { return lowerRowCardIDs; }
    public List<OfferSlotData> getOfferSlots() { return offerSlots; }
    public Map<String, PlayerData> getPlayers() { return players; }
}
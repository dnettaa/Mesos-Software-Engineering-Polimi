package it.polimi.ingsw.view;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;

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
    private List<String> placementOrder;

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
            List<String> tribes = old.tribeCardID();
            tribes.add(cardID);
            // PlayerData è immutabile, creiamo un rimpiazzo aggiornato
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), old.food(),
                    old.prestigePoints(), tribes, old.buildingID()
            );
            players.put(nickname, updated);
        }
    }

    public void addBuildingTo(String nickname, String cardID) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            List<String> builds = old.buildingID();
            builds.add(cardID);
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), old.food(),
                    old.prestigePoints(), old.tribeCardID(), builds
            );
            players.put(nickname, updated);
        }
    }

    public void adjustFood(String nickname, int delta) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            int newFood = Math.max(0, old.food() + delta); // Evita cibo negativo
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), newFood,
                    old.prestigePoints(), old.tribeCardID(), old.buildingID()
            );
            players.put(nickname, updated);
        }
    }

    public void adjustPP(String nickname, int delta) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            int newPP = Math.max(0, old.prestigePoints() + delta);
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), old.food(),
                    newPP, old.tribeCardID(), old.buildingID()
            );
            players.put(nickname, updated);
        }
    }

    public void placeTotemOnSlot(String nickname, char slotID) {
        for (int i = 0; i < offerSlots.size(); i++) {
            OfferSlotData slot = offerSlots.get(i);
            if (slot.slotID() == slotID) {
                offerSlots.set(i, new OfferSlotData(
                        slot.slotID(), slot.upSel(), slot.downSel(), slot.foodReward(), nickname
                ));
                return;
            }
        }
    }

    public void freeSlot(char slotID) {
        for (int i = 0; i < offerSlots.size(); i++) {
            OfferSlotData slot = offerSlots.get(i);
            if (slot.slotID() == slotID) {
                offerSlots.set(i, new OfferSlotData(
                        slot.slotID(), slot.upSel(), slot.downSel(), slot.foodReward(), null
                ));
                return;
            }
        }
    }


    // =========================================================
    // SETTER COMPLETI (Usati dal GameStateMessage principale)
    // =========================================================

    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }
    public void setCurrentEra(Era currentEra) { this.currentEra = currentEra; }
    public void setCurrentPhase(String currentPhaseName) { this.currentPhaseName = currentPhaseName; }
    public void setCurrentPlayer(String currentPlayerNickname) { this.currentPlayerNickname = currentPlayerNickname; }
    public void setResolutionOrder(List<Character> resolutionOrder) { this.resolutionOrder = new ArrayList<>(resolutionOrder); }
    public void setTurnOrder(List<String> turnOrder) { this.turnOrder = new ArrayList<>(turnOrder); }
    public void setTribeDeckRemaining(int tribeDeckRemaining) { this.tribeDeckRemaining = tribeDeckRemaining; }
    public void setUpperRow(List<String> upperRowCardIDs) { this.upperRowCardIDs = new ArrayList<>(upperRowCardIDs); }
    public void setLowerRow(List<String> lowerRowCardIDs) { this.lowerRowCardIDs = new ArrayList<>(lowerRowCardIDs); }
    public void setOfferSlots(List<OfferSlotData> offerSlots) { this.offerSlots = new ArrayList<>(offerSlots); }
    public void setPlayers(List<PlayerData> players) {
        Map<String, PlayerData> playersList = new HashMap<>();
        for(PlayerData p: players){
            playersList.put(p.nickname(), p);
        }
        this.players = playersList;
    }
    public void setPlacementOrder(List<String> placementOrder){this.placementOrder = new ArrayList<>(placementOrder);}


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
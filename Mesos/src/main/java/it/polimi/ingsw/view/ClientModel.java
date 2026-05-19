package it.polimi.ingsw.view;

import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.game.Era;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local replica of the game state (Dumb Model).
 * <p>
 * This class stores all the data required by the View to render the game board.
 * It is completely passive and contains no game logic. It is updated incrementally
 * via delta messages from the server, ensuring efficient network usage.
 * </p>
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
    private List<String> ranking;
    private Map<String, Integer> finalPPByPlayer;
    private Map<String, Integer> endGameBonusByPlayer;

    /**
     * Constructs an empty ClientModel and initializes all list and map collections.
     */
    public ClientModel() {
        this.resolutionOrder = new ArrayList<>();
        this.turnOrder = new ArrayList<>();
        this.upperRowCardIDs = new ArrayList<>();
        this.lowerRowCardIDs = new ArrayList<>();
        this.offerSlots = new ArrayList<>();
        this.players = new HashMap<>();
    }

    // =========================================================
    // MUTATORS (Called by delta messages, e.g., EventResolvedMessage)
    // =========================================================

    /**
     * Removes a specific card from the upper row of the game board.
     *
     * @param cardID The identifier of the card to be removed.
     */
    public void removeUpperCard(String cardID) {
        if (upperRowCardIDs != null) {
            upperRowCardIDs.remove(cardID);
        }
    }

    /**
     * Removes a specific card from the lower row of the game board.
     *
     * @param cardID The identifier of the card to be removed.
     */
    public void removeLowerCard(String cardID) {
        if (lowerRowCardIDs != null) {
            lowerRowCardIDs.remove(cardID);
        }
    }

    /**
     * Adds a newly acquired Tribe Card to a specific player's hand.
     * Replaces the immutable {@link PlayerData} record with an updated instance.
     *
     * @param nickname The nickname of the player acquiring the card.
     * @param cardID   The identifier of the acquired Tribe Card.
     */
    public void addTribeCardTo(String nickname, String cardID) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            List<String> tribes = new ArrayList<>(old.tribeCardID());
            tribes.add(cardID);
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), old.food(),
                    old.prestigePoints(), tribes, old.buildingID()
            );
            players.put(nickname, updated);
        }
    }

    /**
     * Adds a newly acquired Building Card to a specific player's hand.
     * Replaces the immutable {@link PlayerData} record with an updated instance.
     *
     * @param nickname The nickname of the player acquiring the card.
     * @param cardID   The identifier of the acquired Building Card.
     */
    public void addBuildingTo(String nickname, String cardID) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            List<String> builds = new ArrayList<>(old.buildingID());
            builds.add(cardID);
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), old.food(),
                    old.prestigePoints(), old.tribeCardID(), builds
            );
            players.put(nickname, updated);
        }
    }

    /**
     * Adjusts the food reserve of a specific player by a given delta.
     * Ensures that the total food amount never drops below zero.
     *
     * @param nickname The nickname of the player whose food is being adjusted.
     * @param delta    The amount of food to add (or remove, if negative).
     */
    public void adjustFood(String nickname, int delta) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            int newFood = Math.max(0, old.food() + delta);
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), newFood,
                    old.prestigePoints(), old.tribeCardID(), old.buildingID()
            );
            players.put(nickname, updated);
        }
    }

    /**
     * Adjusts the Prestige Points (PP) of a specific player by a given delta.
     *
     * @param nickname The nickname of the player whose points are being adjusted.
     * @param delta    The amount of points to add (or remove, if negative).
     */
    public void adjustPP(String nickname, int delta) {
        PlayerData old = players.get(nickname);
        if (old != null) {
            int newPP = old.prestigePoints() + delta;
            PlayerData updated = new PlayerData(
                    old.nickname(), old.totemColor(), old.food(),
                    newPP, old.tribeCardID(), old.buildingID()
            );
            players.put(nickname, updated);
        }
    }

    /**
     * Assigns a player's totem to a specific slot on the Offer Track.
     * Replaces the immutable {@link OfferSlotData} record with an updated instance.
     *
     * @param nickname The nickname of the player placing the totem.
     * @param slotID   The character identifier of the chosen slot (e.g., 'A', 'B').
     */
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

    /**
     * Removes the occupant from a specific slot on the Offer Track, marking it as free.
     *
     * @param slotID The character identifier of the slot to be freed.
     */
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
    // FULL SETTERS (Used by the initial GameStateMessage)
    // =========================================================

    /** Sets the current game round number. */
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }
    /** Sets the current Era of the game. */
    public void setCurrentEra(Era currentEra) { this.currentEra = currentEra; }
    /** Sets the name of the currently active game phase. */
    public void setCurrentPhase(String currentPhaseName) { this.currentPhaseName = currentPhaseName; }
    /** Sets the nickname of the player currently taking their turn. */
    public void setCurrentPlayer(String currentPlayerNickname) { this.currentPlayerNickname = currentPlayerNickname; }
    /** Sets the order of slots resolving on the offer track. */
    public void setResolutionOrder(List<Character> resolutionOrder) { this.resolutionOrder = new ArrayList<>(resolutionOrder); }
    /** Sets the chronological turn order of the players. */
    public void setTurnOrder(List<String> turnOrder) { this.turnOrder = new ArrayList<>(turnOrder); }
    /** Sets the amount of cards remaining in the main tribe deck. */
    public void setTribeDeckRemaining(int tribeDeckRemaining) { this.tribeDeckRemaining = tribeDeckRemaining; }
    /** Overwrites the entire list of cards currently visible in the upper row. */
    public void setUpperRow(List<String> upperRowCardIDs) { this.upperRowCardIDs = new ArrayList<>(upperRowCardIDs); }
    /** Overwrites the entire list of cards currently visible in the lower row. */
    public void setLowerRow(List<String> lowerRowCardIDs) { this.lowerRowCardIDs = new ArrayList<>(lowerRowCardIDs); }
    /** Overwrites the entire list of offer slots and their states. */
    public void setOfferSlots(List<OfferSlotData> offerSlots) { this.offerSlots = new ArrayList<>(offerSlots); }

    /**
     * Populates the internal map of players based on a provided list of PlayerData.
     * @param players The list of all players in the game.
     */
    public void setPlayers(List<PlayerData> players) {
        Map<String, PlayerData> playersList = new HashMap<>();
        for(PlayerData p: players){
            playersList.put(p.nickname(), p);
        }
        this.players = playersList;
    }

    /** Sets the chronological placement order for totems. */
    public void setPlacementOrder(List<String> placementOrder){this.placementOrder = new ArrayList<>(placementOrder);}
    /** Sets the final player ranking (1st to last) for the end-game screen. */
    public void setRanking(List<String> ranking) { this.ranking = new ArrayList<>(ranking); }
    /** Sets the mapping of total final Prestige Points for each player. */
    public void setFinalPP(Map<String, Integer> finalPP) { this.finalPPByPlayer = new HashMap<>(finalPP); }
    /** Sets the mapping of bonus Prestige Points awarded during the end-game calculation. */
    public void setEndGameBonus(Map<String, Integer> bonus) { this.endGameBonusByPlayer = new HashMap<>(bonus); }

    // =========================================================
    // GETTERS (Used by the TUI/GUI to render the board)
    // =========================================================

    /** @return The current game round number. */
    public int getCurrentRound() { return currentRound; }
    /** @return The current Era of the game. */
    public Era getCurrentEra() { return currentEra; }
    /** @return The string identifier of the active phase. */
    public String getCurrentPhaseName() { return currentPhaseName; }
    /** @return The nickname of the active player. */
    public String getCurrentPlayerNickname() { return currentPlayerNickname; }
    /** @return The list of slot IDs representing the offer resolution order. */
    public List<Character> getResolutionOrder() { return resolutionOrder; }
    /** @return The list of player nicknames representing the current turn order. */
    public List<String> getTurnOrder() { return turnOrder; }
    /** @return The number of cards left to draw in the tribe deck. */
    public int getTribeDeckRemaining() { return tribeDeckRemaining; }
    /** @return The list of card IDs currently sitting in the upper row. */
    public List<String> getUpperRowCardIDs() { return upperRowCardIDs; }
    /** @return The list of card IDs currently sitting in the lower row. */
    public List<String> getLowerRowCardIDs() { return lowerRowCardIDs; }
    /** @return The list of data objects representing the offer track slots. */
    public List<OfferSlotData> getOfferSlots() { return offerSlots; }
    /** @return A map linking player nicknames to their respective data and inventories. */
    public Map<String, PlayerData> getPlayers() { return players; }
    /** @return The ordered list of player nicknames from 1st place to last. */
    public List<String> getRanking() { return ranking; }
    /** @return A map of the total final Prestige Points achieved by each player. */
    public Map<String, Integer> getFinalPP() { return finalPPByPlayer; }
    /** @return A map of the bonus Prestige Points awarded specifically at the end of the game. */
    public Map<String, Integer> getEndGameBonus() { return endGameBonusByPlayer; }

    // =========================================================
    // APPLY METHODS
    // =========================================================

    /**
     * Applies a complete game state snapshot, effectively resetting and overriding
     * the local model with the server's master state. Usually called at the start
     * of the game or upon reconnection.
     *
     * @param snapshot The data transfer object containing the full game state.
     */
    public void applyGameStarted(GameStateSnapshot snapshot) {
        this.currentRound = snapshot.currentRound();
        this.currentEra = snapshot.currentEra();
        this.currentPhaseName = snapshot.currentPhaseName();
        this.currentPlayerNickname = snapshot.currentPlayerNickname();
        this.placementOrder = new ArrayList<>(snapshot.placementOrder());
        this.resolutionOrder = new ArrayList<>(snapshot.resolutionOrder());
        this.turnOrder = new ArrayList<>(snapshot.turnOrder());
        this.tribeDeckRemaining = snapshot.tribeDeckRemaining();
        this.upperRowCardIDs = new ArrayList<>(snapshot.upperRowCardIDs());
        this.lowerRowCardIDs = new ArrayList<>(snapshot.lowerRowCardIDs());
        this.offerSlots = new ArrayList<>(snapshot.offerSlots());
        setPlayers(snapshot.players());
    }

    /**
     * Updates the local model to reflect a totem placement action by a player.
     * Updates the target offer slot and advances the active player turn.
     *
     * @param dto The data object describing the totem placement details.
     */
    public void applyTotemPlaced(TotemPlacedDTO dto) {
        this.placeTotemOnSlot(dto.placerNickname(), dto.slotID());
        this.currentPlayerNickname = dto.nextPlayerNickname();
        this.currentPhaseName = dto.nextPhaseName();
    }

    /**
     * Updates the local model to reflect a player taking cards from the board.
     * Removes the taken cards from the rows, adds them to the player's inventory,
     * adjusts resources, frees the corresponding offer slot, and advances the turn.
     *
     * @param dto The data object containing the details of the taken cards.
     */
    public void applyCardsTaken(CardsTakenDTO dto) {
        for(String cardID : dto.takenUpperIDs()){
            this.removeUpperCard(cardID);
        }

        for(String cardID : dto.takenLowerIDs()){
            this.removeLowerCard(cardID);
        }

        for(String cardID : dto.addedTribeCardIDs()){
            this.addTribeCardTo(dto.nickname(), cardID);
        }

        for(String cardID : dto.addedBuildingIDs()){
            this.addBuildingTo(dto.nickname(), cardID);
        }

        this.adjustFood(dto.nickname(), dto.foodDelta());
        this.adjustPP(dto.nickname(), dto.ppDelta());
        this.freeSlot(dto.freedSlotID());
        this.currentPlayerNickname = dto.nextPlayerNickname();
        this.currentPhaseName = dto.nextPhaseName();
    }

    /**
     * Updates the local model when a player takes an extra card outside the normal
     * offer resolution (e.g., triggered by a specific building or event effect).
     *
     * @param dto The data object describing the extra card acquisition.
     */
    public void applyExtraCardTaken(ExtraCardTakenDTO dto) {
        if (dto.fromUpperRow()) {
            this.removeUpperCard(dto.cardID());
        } else {
            this.removeLowerCard(dto.cardID());
        }

        if (dto.isBuilding()) {
            this.addBuildingTo(dto.nickname(), dto.cardID());
        } else {
            this.addTribeCardTo(dto.nickname(), dto.cardID());
        }

        this.adjustFood(dto.nickname(), dto.foodDelta());
        this.currentPhaseName = dto.nextPhaseName();
    }

    /**
     * Updates the local model after an Event Card has been resolved.
     * Applies the resulting resource variations (Prestige Points and Food) to
     * all affected players.
     *
     * @param dto The data object detailing the resource deltas by player.
     */
    public void applyEventResolved(EventResolvedDTO dto) {
        for (Map.Entry<String, Integer> entry : dto.ppDeltaByPlayer().entrySet()){
            this.adjustPP(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : dto.foodDeltaByPlayer().entrySet()){
            this.adjustFood(entry.getKey(), entry.getValue());
        }

        this.currentPhaseName = dto.nextPhaseName();
    }

    /**
     * Handles the end-of-round board maintenance.
     * Slides cards from the upper row to the lower row, discards leftover cards
     * based on the provided deltas, and updates the turn order and Era state
     * for the new round.
     *
     * @param dto The data object containing the board deltas and next round info.
     */
    public void applyRoundEnded(RoundEndedDTO dto) {
        this.lowerRowCardIDs = new ArrayList<>(dto.newLowerRowIDs());
        this.upperRowCardIDs = new ArrayList<>(dto.newUpperRowIDs());

        this.currentRound = dto.newRound();
        this.currentEra = dto.newEra();
        this.turnOrder = new ArrayList<>(dto.newTurnOrder());
        this.currentPlayerNickname = dto.firstPlayerNickname();
        this.tribeDeckRemaining = dto.tribeDeckRemaining();
        this.currentPhaseName = "TotemPlacementPhase";
    }

    /**
     * Transitions the model into the endgame state.
     * Receives and stores the final calculated scores, endgame bonuses, and the
     * definitive player ranking.
     *
     * @param dto The data object containing the final scoring results.
     */
    public void applyGameEnded(GameEndedDTO dto) {
        this.currentPhaseName = "EndGame";
        this.ranking = new ArrayList<>(dto.ranking());
        this.finalPPByPlayer = new HashMap<>(dto.finalPPByPlayer());
        this.endGameBonusByPlayer = new HashMap<>(dto.endGameBonusByPlayer());
    }
}
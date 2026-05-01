package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.view.ClientModel;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the server to update clients with a complete snapshot
 * of the current game state.
 */

public class GameStateMessage extends ServerMessage{
    private final int currentRound;
    private final Era currentEra;
    private final String currentPhaseName;
    private final String currentPlayerNickname;
    private final List<String> placementOrder;
    private final List<Character> resolutionOrder;
    private final List<String> turnOrder;
    private final int tribeDeckRemaining;
    private final List<String> upperRowCardIDs;
    private final List<String> lowerRowCardIDs;
    private final List<OfferSlotData> offerSlots;
    private final List<PlayerData> players;

    /**
     * Creates a new game state message.
     *
     * @param currentRound the current round number
     * @param currentEra the current era
     * @param currentPhaseName the name of the current game phase
     * @param currentPlayerNickname the nickname of the current active player
     * @param placementOrder the totem placement order
     * @param resolutionOrder the offer resolution order
     * @param turnOrder the turn order for the current or next round
     * @param tribeDeckRemaining the number of remaining cards in the tribe deck
     * @param upperRowCardIDs the ids of the cards currently in the upper row
     * @param lowerRowCardIDs the ids of the cards currently in the lower row
     * @param offerSlots the public state of the offer slots
     * @param players the public state of the players
     */
    public GameStateMessage(int currentRound, Era currentEra, String currentPhaseName, String currentPlayerNickname, List<String> placementOrder, List<Character> resolutionOrder, List<String> turnOrder, int tribeDeckRemaining, List<String> upperRowCardIDs, List<String> lowerRowCardIDs, List<OfferSlotData> offerSlots, List<PlayerData> players) {
        this.currentRound = currentRound;
        this.currentEra = currentEra;
        this.currentPhaseName = currentPhaseName;
        this.currentPlayerNickname = currentPlayerNickname;
        this.placementOrder = new ArrayList<>(placementOrder);
        this.resolutionOrder = new ArrayList<>(resolutionOrder);
        this.turnOrder = new ArrayList<>(turnOrder);
        this.tribeDeckRemaining = tribeDeckRemaining;
        this.upperRowCardIDs = new ArrayList<>(upperRowCardIDs);
        this.lowerRowCardIDs = new ArrayList<>(lowerRowCardIDs);
        this.offerSlots = new ArrayList<>(offerSlots);
        this.players = new ArrayList<>(players);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public Era getCurrentEra() {
        return currentEra;
    }

    public String getCurrentPhaseName() {
        return currentPhaseName;
    }

    public String getCurrentPlayerNickname() {
        return currentPlayerNickname;
    }

    public List<String> getPlacementOrder() {
        return new ArrayList<>(placementOrder);
    }

    public List<Character> getResolutionOrder() {
        return new ArrayList<>(resolutionOrder);
    }

    public List<String> getTurnOrder() {
        return new ArrayList<>(turnOrder);
    }

    public int getTribeDeckRemaining() {
        return tribeDeckRemaining;
    }

    public List<String> getUpperRowCardIDs() {
        return new ArrayList<>(upperRowCardIDs);
    }

    public List<String> getLowerRowCardIDs() {
        return new ArrayList<>(lowerRowCardIDs);
    }

    public List<OfferSlotData> getOfferSlots() {
        return new ArrayList<>(offerSlots);
    }

    public List<PlayerData> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Applies this full game snapshot to the given view.
     * <p>
     * This message is intended to initialize or completely reset the client-side
     * model. After the snapshot has been copied into a new {@code ClientModel},
     * the view is rendered using the updated local state.
     *
     * @param view the view that must receive and render the full game snapshot
     */
    @Override
    public void apply(View view){
        ClientModel model = new ClientModel();

        model.setCurrentRound(currentRound);
        model.setCurrentEra(currentEra);
        model.setCurrentPhase(currentPhaseName);
        model.setCurrentPlayer(currentPlayerNickname);
        model.setPlacementOrder(placementOrder);
        model.setResolutionOrder(resolutionOrder);
        model.setTurnOrder(turnOrder);
        model.setTribeDeckRemaining(tribeDeckRemaining);
        model.setUpperRow(upperRowCardIDs);
        model.setLowerRow(lowerRowCardIDs);
        model.setOfferSlots(offerSlots);
        model.setPlayers(players);

        view.setClientModel(model);
        view.render()
    }
}
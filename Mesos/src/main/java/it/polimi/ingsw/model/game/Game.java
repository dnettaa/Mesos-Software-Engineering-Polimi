package it.polimi.ingsw.model.game;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.card.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Main class representing a Mesos game session.
 * Manages the game flow, including totem placement, card selection,
 * event resolution, and end-game scoring.
 *
 * @author Luca Grecchi
 */
public class Game {

    private final int gameID;
    private final List<Player> players;
    private final Board board;
    private int currentRound;
    private GamePhase phase;
    private GameState state;
    private List<Player> placementOrder;
    private int currentPlayerIndex;
    private List<OfferSlot> resolutionOrder;

    public Game(int gameID, List<Player> players, Board board, int currentRound, GamePhase phase,
                GameState state, List<Player> placementOrder, int currentPlayerIndex, List<OfferSlot> resolutionOrder)
    {

        this.gameID = gameID;
        this.players = players;
        this.board = board;
        this.currentRound = currentRound;
        this.phase = phase;
        this.state = state;
        this.placementOrder = placementOrder;
        this.currentPlayerIndex = currentPlayerIndex;
        this.resolutionOrder = resolutionOrder;
    }

    public int getGameID() {
        return gameID;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public List<OfferSlot> getResolutionOrder() {
        return resolutionOrder;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public List<Player> getPlacementOrder() {
        return placementOrder;
    }

    public GameState getState() {
        return state;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Places a player's totem on the specified offer slot.
     * When all players have placed, transitions to OfferResolution phase.
     *
     * @param player the player placing the totem
     * @param slotID the ID of the chosen offer slot
     */
    public void placeTotem(Player player, char slotID){

        validateState();
        validatePhase(GamePhase.TotemPlacement);
        validateActivePlayerTotemPlacement(player);

        board.placeTotemOnOffer(player, slotID);
        currentPlayerIndex++;

        if(currentPlayerIndex == players.size()){
            resolutionOrder = board.getOfferResolutionOrder();
            currentPlayerIndex = 0;
            phase = GamePhase.OfferResolution;
        }
    }


    /**
     * Allows the current player to take cards from the upper and lower rows
     * based on their offer slot action. Returns the totem to the turn order track.
     * When all players have resolved, transitions to EventResolution phase.
     *
     * @param player the player taking cards
     * @param chosenUpper cards chosen from the upper row
     * @param chosenLower cards chosen from the lower row
     */
    public void takeCards(Player player, List<Card> chosenUpper, List<Card> chosenLower){

        validateState();
        validatePhase(GamePhase.OfferResolution);
        validateActivePlayerOfferResolution(player);

        validateChosenCards(player, chosenUpper, chosenLower);

        for(Card upperCard: chosenUpper){
            board.removeCardFrom(board.getUpperRow(), upperCard);
            upperCard.applyTo(player);
        }
        for(Card lowerCard: chosenLower){
            board.removeCardFrom(board.getLowerRow(), lowerCard);
            lowerCard.applyTo(player);
        }

        board.returnTotemToTurnOrder(player);
        applyTurnOrderBonus(player);

        currentPlayerIndex++;
        if(currentPlayerIndex == players.size()){
            currentPlayerIndex = 0;
            phase = GamePhase.EventResolution;
        }
    }







    /**
     * Check if the state is "In progress"
     */
    private void validateState(){
        if (state != GameState.InProgress) {
            throw new IllegalStateException("Game is not in progress!");
        }
    }

    /**
     * Check if the phase is the one expected
     * @param expected expected game phase
     */
    private void validatePhase(GamePhase expected){
        if(phase != expected){
            throw new IllegalStateException("Expected phase " + expected + " but current is " + phase);
        }
    }

    /**
     * Check if the player is the one expected to act
     * @param player the player attempting to act
     */
    private void validateActivePlayerTotemPlacement(Player player){
        if(!player.equals(placementOrder.get(currentPlayerIndex))){
            throw new IllegalArgumentException("Wrong player");
        }
    }

    /**
     * Check if the player is the one expected to act
     * @param player the player attempting to act
     */
    private void validateActivePlayerOfferResolution(Player player){
        if(!player.equals(resolutionOrder.get(currentPlayerIndex).getOccupant())){
            throw new IllegalArgumentException("Wrong player");
        }
    }

    /**
     * Validates that the chosen cards match the player's offer slot action
     * and that no Event cards are selected.
     *
     * @param player the player taking cards
     * @param chosenUpper cards chosen from the upper row
     * @param chosenLower cards chosen from the lower row
     */
    private void validateChosenCards(Player player, List<Card> chosenUpper, List<Card> chosenLower){

        int[] action = board.getActionFor(player);

        List<Card> pickableUpper = board.getUpperRowCards();
        List<Card> pickableLower = board.getLowerRowCards();

        if(action[0] != chosenUpper.size() || action[1] != chosenLower.size()){
            throw new IllegalArgumentException("Wrong chosen cards");
        }

        for(Card card: chosenUpper){
            if(!pickableUpper.contains(card) || !card.isPickable()){
                throw new IllegalArgumentException("Wrong chosen cards");
            }
        }

        for(Card card: chosenLower){
            if(!pickableLower.contains(card) || !card.isPickable()){
                throw new IllegalArgumentException("Wrong chosen cards");
            }
        }

        int totalCost = 0;
        for (Card card : chosenUpper) {
            totalCost += card.getCostFor(player);
        }
        for (Card card : chosenLower) {
            totalCost += card.getCostFor(player);
        }
        if (totalCost > player.getFood()) {
            throw new IllegalArgumentException("Not enough food for buildings");
        }

    }
}

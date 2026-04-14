package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.game.state.*;
import it.polimi.ingsw.model.board.OfferSlot;
import it.polimi.ingsw.model.board.Board;
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
    private Phase currentPhase;
    private GameState state;
    private List<Player> placementOrder;
    private int currentPlayerIndex;
    private List<OfferSlot> resolutionOrder;

    public Game(int gameID, List<Player> players, Board board, int currentRound, Phase phase,
                GameState state, List<Player> placementOrder, int currentPlayerIndex, List<OfferSlot> resolutionOrder)
    {

        this.gameID = gameID;
        this.players = players;
        this.board = board;
        this.currentRound = currentRound;
        this.currentPhase = phase;
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

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public void setResolutionOrder(List<OfferSlot> order) {
        this.resolutionOrder = order;
    }

    public void setCurrentPhase(Phase phase) {
        this.currentPhase = phase;
    }

    public void setPlacementOrder(List<Player> order) {
        this.placementOrder = order;
    }

    public void setCurrentRound(int round) {
        this.currentRound = round;
    }

    /**
     * Places a player's totem on the specified offer slot.
     * When all players have placed, transitions to OfferResolution phase.
     *
     * @param player the player placing the totem
     * @param slotID the ID of the chosen offer slot
     */
    public void placeTotem(Player player, char slotID){
        currentPhase.placeTotem(this, player, slotID);
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
        currentPhase.takeCards(this, player, chosenUpper, chosenLower);
    }

    /**
     * Resolves all event cards in the lower row.
     * On the last round, also resolves events from the upper row.
     * Sustenance events are always resolved last.
     * Same-type events are resolved in Era order.
     * Transitions to EndRound phase.
     */
    public void resolveEvents() {
        currentPhase.resolveEvents(this);
    }

    /**
     * Ends the current round. If not the last round, sets up the board
     * for the next round and transitions to TotemPlacement.
     * If it's the last round, transitions to EndGame phase.
     */
    public void endRound(){
        currentPhase.endRound(this);
    }

    /**
     * Calculates the final scoring for all players and ends the game.
     * Transitions the game state to Finished.
     */
    public void endGame() {
        currentPhase.endGame(this);
    }


    /**
     * Allows the player with the ExtraPick building to take an additional
     * card from the upper row. Transitions to EventResolution phase.
     *
     * @param player the player taking the extra card
     * @param card the card chosen from the upper row
     */
    public void takeExtraCard(Player player, Card card) {
        currentPhase.takeExtraCard(this, player, card);
    }

    /**
     * Applies the turn order bonus or penalty to the player.
     * If the player lands on a food bonus slot, they receive food.
     * If the player lands on the last slot, they pay 1 food or lose 2 PP.
     *
     * @param player the player returning to the turn order track
     */
    public void applyTurnOrderBonus(Player player){
        int bonus = board.getFoodBonus(player);

        if(bonus > 0){
            player.addFood(bonus);
            for(BuildingCard b : player.getTribe().getBuildings()){
                b.onTurnOrderPlaced(bonus, player);
            }
        } else if(bonus < 0){
            if(player.getFood() >= Math.abs(bonus)){
                player.spendFood(Math.abs(bonus));
            } else {
                player.losePP(2);
            }
        }
    }

    /**
     * Check if the state is "In progress"
     */
    public void validateState(){
        if (state != GameState.InProgress) {
            throw new IllegalStateException("Game is not in progress!");
        }
    }

    /**
     * Check if the player is the one expected to act
     * @param player the player attempting to act
     */
    public void validateActivePlayerTotemPlacement(Player player){
        if(!player.equals(placementOrder.get(currentPlayerIndex))){
            throw new IllegalArgumentException("Wrong player");
        }
    }

    /**
     * Check if the player is the one expected to act
     * @param player the player attempting to act
     */
    public void validateActivePlayerOfferResolution(Player player){
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
    public void validateChosenCards(Player player, List<Card> chosenUpper, List<Card> chosenLower){

        int[] action = board.getActionFor(player);

        List<Card> pickableUpper = board.getUpperRowCards();
        List<Card> pickableLower = board.getLowerRowCards();

        if(action[0] != chosenUpper.size() || action[1] != chosenLower.size()){
            throw new IllegalArgumentException("Wrong chosen cards");
        }

        int totalCost = 0;

        for(Card card: chosenUpper){
            if(!pickableUpper.contains(card) || !card.isPickable()){
                throw new IllegalArgumentException("Wrong chosen cards");
            }
            totalCost += card.getCostFor(player);
        }

        for(Card card: chosenLower){
            if(!pickableLower.contains(card) || !card.isPickable()){
                throw new IllegalArgumentException("Wrong chosen cards");
            }
            totalCost += card.getCostFor(player);
        }

        if (totalCost > player.getFood()) {
            throw new IllegalArgumentException("Not enough food for buildings");
        }
    }
}

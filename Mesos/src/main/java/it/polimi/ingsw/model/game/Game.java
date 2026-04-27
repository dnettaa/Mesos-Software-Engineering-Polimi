package it.polimi.ingsw.model.game;

import it.polimi.ingsw.message.GameStateMessage;
import it.polimi.ingsw.message.OfferSlotData;
import it.polimi.ingsw.message.PlayerData;
import it.polimi.ingsw.model.Exception.ErrorCode;
import it.polimi.ingsw.model.Exception.GameException;
import it.polimi.ingsw.model.game.phase.*;
import it.polimi.ingsw.model.board.OfferSlot;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.card.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class representing a Mesos game session.
 * Manages the game flow, including totem placement, card selection,
 * event resolution, and end-game scoring.
 *
 * @author Luca Grecchi
 */
public class Game implements GameActions{

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
     * @param nickname the player placing the totem
     * @param slotID the ID of the chosen offer slot
     */
    public void placeTotem(String nickname, char slotID){
        Player player = findPlayerByNickname(nickname);
        currentPhase.placeTotem(this, player, slotID);
    }


    /**
     * Allows the current player to take cards from the upper and lower rows
     * based on their offer slot action. Returns the totem to the turn order track.
     * When all players have resolved, transitions to EventResolution phase.
     *
     * @param nickname the player taking cards
     * @param chosenUpperIDs cards chosen from the upper row
     * @param chosenLowerIDs cards chosen from the lower row
     */
    public void takeCards(String nickname, List<String> chosenUpperIDs, List<String> chosenLowerIDs){
        Player player = findPlayerByNickname(nickname);
        List<Card> chosenUpper = resolveCards(chosenUpperIDs, board.getUpperRowCards());
        List<Card> chosenLower = resolveCards(chosenLowerIDs, board.getLowerRowCards());
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
     * @param nickname the player taking the extra card
     * @param cardID the card chosen from the upper row
     */
    public void takeExtraCard(String nickname, String cardID) {
        Player player = findPlayerByNickname(nickname);
        List<Card> card = resolveCards(List.of(cardID), board.getUpperRowCards());
        currentPhase.takeExtraCard(this, player, card.getFirst());
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
     * Check if the state is "In progress".
     *
     * @throws GameException with {@link ErrorCode#INVALID_PHASE} if the game is not currently in progress
     */
    public void validateState(){
        if (state != GameState.InProgress) {
            throw new GameException(ErrorCode.INVALID_PHASE, "Game is not in progress!");
        }
    }

    /**
     * Check if the player is the one expected to act during totem placement.
     *
     * @param player the player attempting to act
     * @throws GameException with {@link ErrorCode#NOT_YOUR_TURN} if it is not the provided player's turn to place a totem
     */
    public void validateActivePlayerTotemPlacement(Player player){
        if(!player.equals(placementOrder.get(currentPlayerIndex))){
            throw new GameException(ErrorCode.NOT_YOUR_TURN, "Wrong player");
        }
    }

    /**
     * Check if the player is the one expected to act during offer resolution.
     *
     * @param player the player attempting to act
     * @throws GameException with {@link ErrorCode#NOT_YOUR_TURN} if it is not the provided player's turn to resolve their offer
     */
    public void validateActivePlayerOfferResolution(Player player){
        if(!player.equals(resolutionOrder.get(currentPlayerIndex).getOccupant())){
            throw new GameException(ErrorCode.NOT_YOUR_TURN, "Wrong player");
        }
    }

    /**
     * Validates that the chosen cards match the player's offer slot action
     * and that no Event cards are selected.
     *
     * @param player the player taking cards
     * @param chosenUpper cards chosen from the upper row
     * @param chosenLower cards chosen from the lower row
     * @throws GameException with {@link ErrorCode#INVALID_SELECTION} if the number of chosen cards does not match the slot action
     * @throws GameException with {@link ErrorCode#CARD_NOT_IN_ROW} if any chosen card is not pickable or not in the specified row
     * @throws GameException with {@link ErrorCode#INSUFFICIENT_FOOD} if the player does not have enough food to pay for the chosen buildings
     */
    public void validateChosenCards(Player player, List<Card> chosenUpper, List<Card> chosenLower){

        int[] action = board.getActionFor(player);

        List<Card> pickableUpper = board.getUpperRowCards();
        List<Card> pickableLower = board.getLowerRowCards();

        if(action[0] != chosenUpper.size() || action[1] != chosenLower.size()){
            throw new GameException(ErrorCode.INVALID_SELECTION, "Wrong number of chosen cards");
        }

        int totalCost = 0;

        for(Card card: chosenUpper){
            if(!pickableUpper.contains(card) || !card.isPickable()){
                throw new GameException(ErrorCode.CARD_NOT_IN_ROW, "Card not available in upper row");
            }
            totalCost += card.getCostFor(player);
        }

        for(Card card: chosenLower){
            if(!pickableLower.contains(card) || !card.isPickable()){
                throw new GameException(ErrorCode.CARD_NOT_IN_ROW, "Card not available in lower row");
            }
            totalCost += card.getCostFor(player);
        }

        if (totalCost > player.getFood()) {
            throw new GameException(ErrorCode.INSUFFICIENT_FOOD, "Not enough food for buildings");
        }
    }

    /**
     * Returns the nickname of the player who is currently expected to act.
     * Delegates to the current phase, which knows which player is active.
     *
     * @return the nickname of the active player, or null if no player is expected to act
     */
    public String getCurrentPlayerNickname(){
        return currentPhase.getCurrentPlayerNickname(this);
    }

    /**
     * Returns the simple class name of the current game phase.
     * Used by the client to determine which action to prompt the user for.
     *
     * @return the name of the current phase (e.g. "TotemPlacementPhase")
     */
    public String getCurrentPhaseName() {
        return currentPhase.getClass().getSimpleName();
    }

    /**
     * Returns whether the game has ended.
     *
     * @return true if the game state is Finished, false otherwise
     */
    public boolean isGameEnded(){
        return state == GameState.Finished;
    }

    /**
     * Finds and returns the player with the given nickname.
     *
     * @param nickname the nickname to search for
     * @return the matching player
     * @throws GameException with {@link ErrorCode#UNKNOWN_PLAYER} if no player with that nickname exists
     */
    private Player findPlayerByNickname(String nickname){
        for(Player p: players){
            if(p.getNickname().equals(nickname)){
                return p;
            }
        }
        throw new GameException(ErrorCode.UNKNOWN_PLAYER, "Unknown player: " + nickname);
    }

    /**
     * Resolves a list of card IDs against a row of available cards.
     * Returns the matched cards in the same order as the input IDs.
     *
     * @param ids the list of card IDs to resolve
     * @param row the list of available cards to search in
     * @return the list of matched cards
     * @throws GameException with {@link ErrorCode#UNKNOWN_CARD} if any ID is not found in the row
     */
    private List<Card> resolveCards(List<String> ids, List<Card> row){
        List<Card> result = new ArrayList<>();
        for (String id : ids) {
            boolean found = false;
            for (Card c : row) {
                if (c.getId().equals(id)) {
                    result.add(c);
                    found = true;
                    break;
                }
            }
            if(!found){
                throw new GameException(ErrorCode.UNKNOWN_CARD, "Card not found: " + id);
            }
        }
        return result;
    }

    /**
     * Builds and returns a complete snapshot of the current game state.
     * Collects all relevant information from the game and board and packages
     * it into a serializable {@link GameStateMessage} to be broadcast to all clients.
     *
     * @return a GameStateMessage representing the current state of the game
     */
    public GameStateMessage buildGameStateMessage(){
        //placement order
        List<String> placementNicknames = new ArrayList<>();
        for(Player p: placementOrder){
            placementNicknames.add(p.getNickname());
        }

        //resolution order
        List<Character> resolutionSlotIDs  = new ArrayList<>();
        for(OfferSlot offerSlot: resolutionOrder){
            resolutionSlotIDs.add(offerSlot.getSlotID());
        }

        //turn order
        List<String> turnOrder = new ArrayList<>();
        for (Player p : board.getTurnOrderTrack().getPlayersInOrder()) {
            turnOrder.add(p.getNickname());
        }

        // upperRowCardIDs and lowerRowCardIDs
        List<String> upperIDs = new ArrayList<>();
        for (Card c : board.getUpperRowCards()) {
            upperIDs.add(c.getId());
        }

        List<String> lowerIDs = new ArrayList<>();
        for(Card c: board.getLowerRowCards()){
            lowerIDs.add(c.getId());
        }

        //OfferSlotData
        List<OfferSlotData> offerSlots = board.buildOfferSlotsData();

        //PlayerData
        List<PlayerData> playersData = new ArrayList<>();
        for(Player p: players){
            playersData.add(p.buildPlayerData());
        }


        return new GameStateMessage(
                this.currentRound,
                this.board.getCurrentEra(),
                this.getCurrentPhaseName(),
                this.getCurrentPlayerNickname(),
                placementNicknames,
                resolutionSlotIDs,
                turnOrder,
                this.board.getTribeDeckRemaining(),
                upperIDs,
                lowerIDs,
                offerSlots,
                playersData
        );
    }
}

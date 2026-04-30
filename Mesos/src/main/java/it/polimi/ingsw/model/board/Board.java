package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.EventCard;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

/**
 * Represents the main board of the game.
 * It coordinates offer track, turn order track, decks and visible rows.
 *
 * @author Diana
 */

public class Board {
    private final OfferTrack offerTrack;
    private final TurnOrderTrack turnOrderTrack;
    private final TribeDeck tribeDeck;
    private final BuildingDeck buildingDeck;
    private final CardRow upperRow;
    private final CardRow lowerRow;
    private Era currentEra;
    /**
     * Creates a new board with all of its components.
     *
     * @param offerTrack the offer track of the board
     * @param turnOrderTrack the turn order track of the board
     * @param tribeDeck the tribe deck
     * @param buildingDeck the building deck
     * @param upperRow the upper visible row
     * @param lowerRow the lower visible row
     * @param currentEra the current era of the game
     */
    public Board(OfferTrack offerTrack, TurnOrderTrack turnOrderTrack, TribeDeck tribeDeck, BuildingDeck buildingDeck, CardRow upperRow, CardRow lowerRow, Era currentEra) {
        this.offerTrack = offerTrack;
        this.turnOrderTrack = turnOrderTrack;
        this.tribeDeck = tribeDeck;
        this.buildingDeck = buildingDeck;
        this.upperRow = upperRow;
        this.lowerRow = lowerRow;
        this.currentEra = currentEra;
    }

    /**
     * Returns the turn order track of this board.
     *
     * @return the turn order track
     */
    public TurnOrderTrack getTurnOrderTrack(){
        return turnOrderTrack;
    }

    public Era getCurrentEra() {
        return currentEra;
    }

    /**
     * Returns all cards currently visible in the upper row.
     *
     * @return a defensive copy of the upper row cards
     */
    public List<Card> getUpperRowCards(){
        return upperRow.getAllCards();
    }

    /**
     * Returns all cards currently visible in the lower row.
     *
     * @return a defensive copy of the lower row cards
     */
    public List<Card> getLowerRowCards(){
        return lowerRow.getAllCards();
    }

    /**
     * Places the given player's totem on the specified offer slot.
     *
     * @param player the player placing the totem
     * @param slotID the target slot identifier
     */
    public void placeTotemOnOffer(Player player, char slotID){
        offerTrack.placePlayer(player, slotID);
    }

    /**
     * Returns the offer slots occupied by players in resolution order.
     *
     * @return the occupied slots in offer resolution order
     */
    public List<OfferSlot> getOfferResolutionOrder(){
        return offerTrack.getResolutionOrder();
    }

    /**
     * Places the player in the first free position of the turn order track.
     *
     * @param player the player to place
     */
    public void returnTotemToTurnOrder(Player player){
        if(turnOrderTrack.getPlayersInOrder().size() == turnOrderTrack.getNumPlayers()){
            turnOrderTrack.clear();
        }
        turnOrderTrack.placeFirstSlot(player);
    }

    /**
     * Returns the current player placement order.
     *
     * @return the players in turn order
     */
    public List<Player> getPlacementOrder(){
        return turnOrderTrack.getPlayersInOrder();
    }

    /**
     * Returns the action associated with the slot occupied by the given player.
     * <p>
     * The returned array contains:
     * <ul>
     *     <li>index 0: upper row selections</li>
     *     <li>index 1: lower row selections</li>
     * </ul>
     *
     * @param player the player whose action must be retrieved
     * @return the action values for the player
     */
    public int[] getActionFor(Player player){
        return offerTrack.getActionFor(player);
    }


    public void removeCardFromUpper(Card card){
        upperRow.removeCard(card);
    }


    public void removeCardFromLower(Card card) {
        lowerRow.removeCard(card);
    }

    /**
     * Returns the food bonus associated with the player's current
     * position in the turn order track.
     * <p>
     * If the player is in the last position, the return value is {@code -1},
     * representing the penalty logic currently modeled by the board.
     *
     * @param player the player whose bonus must be retrieved
     * @return the food bonus, or {@code -1} for the last position
     */
    public int getFoodBonus(Player player){
        int position = turnOrderTrack.getPositionOf(player);

        if (turnOrderTrack.isLastPosition(player)) {
            return -1;
        }

        return turnOrderTrack.getPositionFoodBonus(position);
    }

    /**
     * Returns the event cards currently visible in the lower row.
     *
     * @return the lower-row event cards
     */
    public List<EventCard> getLowerRowEvents() {
        return lowerRow.getEventCards();
    }

    /**
     * Returns the event cards currently visible in the upper row.
     *
     * @return the upper-row event cards
     */
    public List<EventCard> getUpperRowEvents() {
        return upperRow.getEventCards();
    }

    /**
     * Prepares the board state for a new round.
     * <p>
     * This method:
     * <ol>
     *     <li>resets the offer track,</li>
     *     <li>clears the turn order track,</li>
     *     <li>discards lower-row tribe cards,</li>
     *     <li>moves upper-row tribe cards to the lower row,</li>
     *     <li>discards lower-row building cards,</li>
     *     <li>moves upper-row building cards to the lower row,</li>
     *     <li>updates the current era if needed,</li>
     *     <li>refills the upper tribe row,</li>
     *     <li>reveals the building cards for the current era.</li>
     * </ol>
     */
    public RoundEndedDTO setupNewRound(int newRound, List<String> newTurnOrder, String firstPlayerNickname) {
        offerTrack.reset();

        List<String> discardedLowerTribeIDs = lowerRow.getTribeCards().stream()
                .filter(Card::isPickable).map(Card::getId).toList();
        List<String> discardedLowerEventIDs = lowerRow.getEventCards().stream()
                .map(Card::getId).toList();
        List<String> discardedLowerBuildingIDs = lowerRow.getBuildingCardsInternal().stream()
                .map(Card::getId).toList();
        List<String> movedUpperToLowerTribeIDs = upperRow.getTribeCards().stream()
                .map(Card::getId).toList();
        List<String> movedUpperToLowerBuildingIDs = upperRow.getBuildingCardsInternal().stream()
                .map(Card::getId).toList();

        clearLowerRowTribeCards();
        moveUpperTribeCardsToLower();
        clearLowerRowBuildings();
        moveUpperBuildingsToLower();

        Era previousEra = currentEra;
        currentEra = checkEraTransition(currentEra);

        refillUpperRow(turnOrderTrack.getNumPlayers());
        revealBuildingsForEra(currentEra);

        List<String> newUpperRowIDs = upperRow.getAllCards().stream()
                .map(Card::getId).toList();

        List<String> revealedBuildingIDs = previousEra != currentEra ?
                upperRow.getBuildingCardsInternal().stream().map(Card::getId).toList() :
                List.of();

        return new RoundEndedDTO(
                discardedLowerTribeIDs,
                discardedLowerEventIDs,
                movedUpperToLowerTribeIDs,
                discardedLowerBuildingIDs,
                movedUpperToLowerBuildingIDs,
                newUpperRowIDs,
                revealedBuildingIDs,
                currentEra,
                newRound,
                newTurnOrder,
                firstPlayerNickname,
                tribeDeck.remaining()
        );
    }

    /**
     * Refills the upper row with tribe cards until the target number
     * of visible tribe cards is reached, or until the deck becomes empty.
     *
     * @param numPlayers the number of players in the game
     */
    private void refillUpperRow(int numPlayers) {
        int targetTribeCards = numPlayers + 4;
        int cardsToDraw = targetTribeCards - upperRow.getTribeCards().size();

        for (int i = 0; i < cardsToDraw && !tribeDeck.isEmpty(); i++) {
            upperRow.addTribeCard(tribeDeck.draw());
        }
    }

    /**
     * Updates the current era if the tribe deck has already advanced to a later era.
     *
     * @return the era that should now be considered current
     */
    private Era checkEraTransition(Era currentEra) {
        Era deckEra = tribeDeck.getCurrentEra();

        if (deckEra.ordinal() > currentEra.ordinal()) {
            return deckEra;
        }

        return currentEra;
    }

    /**
     * Reveals all building cards of the specified era into the upper row.
     *
     * @param era the era to reveal
     */
    private void revealBuildingsForEra(Era era) {
        upperRow.addBuildingCards(buildingDeck.revealAll(era));
    }

    /**
     * Removes all tribe cards from the lower row.
     */
    private void clearLowerRowTribeCards() {
        lowerRow.clearTribeCards();
    }

    /**
     * Moves all upper-row tribe cards to the lower row.
     */
    private void moveUpperTribeCardsToLower() {
        upperRow.moveTribeCardsTo(lowerRow);
    }

    /**
     * Removes all building cards from the lower row.
     */
    private void clearLowerRowBuildings() {
        lowerRow.clearBuildingCards();
    }

    /**
     * Moves all upper-row building cards to the lower row.
     */
    private void moveUpperBuildingsToLower() {
        upperRow.moveBuildingCardsTo(lowerRow);
    }

    /**
     * Returns the number of cards currently remaining in the tribe deck.
     *
     * @return the amount of cards left in the tribe deck
     */
    public int getTribeDeckRemaining() {
        return tribeDeck.remaining();
    }

    /**
     * Builds and returns a list of data transfer objects representing the current state of the offer track.
     * Delegates the creation of the data to the underlying {@link OfferTrack}.
     *
     * @return a list of {@link OfferSlotData} representing all the offer slots
     */
    public List<OfferSlotData> buildOfferSlotsData(){
        return offerTrack.buildOfferSlotsData();
    }
}
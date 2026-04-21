package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.card.EventCard;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a visible row of cards on the board.
 * A row can contain tribe cards and building cards.
 *
 * The class provides dedicated operations for adding, removing
 * and reading cards without exposing its internal mutable state.
 *
 * @author Diana
 */

public class CardRow {
    private final List<TribeCard> tribeCards;
    private final List<BuildingCard> buildingCards;

    /**
     * Creates a new card row with the given initial cards.
     *
     * @param tribeCards the initial tribe cards in the row
     * @param buildingCards the initial building cards in the row
     */
    public CardRow(List<TribeCard> tribeCards, List<BuildingCard> buildingCards){
        this.tribeCards = new ArrayList<>(tribeCards);
        this.buildingCards = new ArrayList<>(buildingCards);
    }

    /**
     * Adds a tribe card to the row.
     *
     * @param card the tribe card to add
     */
    public void addTribeCard(TribeCard card) {
        tribeCards.add(card);
    }

    /**
     * Adds a building card to the row.
     *
     * @param card the building card to add
     */
    public void addBuildingCard(BuildingCard card){
        buildingCards.add(card);
    }

    /**
     * Adds multiple building cards to the row.
     * <p>
     * @param cards the building cards to add
     */
    public void addBuildingCards(List<BuildingCard> cards){
        for(BuildingCard card : cards){
            addBuildingCard(card);
        }
    }

    /**
     * Removes the given card from this row.
     * <p>
     * The removal logic is delegated to the card itself,
     * so that the correct internal list is updated without
     * using type checks.
     *
     * @param card the card to remove
     */
    public void removeCard(Card card){
        card.removeFrom(this);
    }

    /**
     * Returns all tribe cards currently in the row.
     *
     * @return a copy of the tribe cards in the row
     */
    public List<TribeCard> getTribeCards() {
        return new ArrayList<>(tribeCards);
    }

    /**
     * Returns the event cards currently in the row.
     *
     * @return a list containing only the event cards in the row
     */
    public List<EventCard> getEventCards(){
        List<EventCard> events = new ArrayList<>();

        for(TribeCard card : tribeCards){
            if(!card.isPickable()){
                events.add((EventCard) card);
            }
        }

        return events;
    }


    /**
     * Returns the internal tribe-card list.
     * <p>
     * This method is intentionally package-private and must only be used
     * by trusted model classes that need direct access for internal logic.
     *
     * @return the internal tribe-card list
     */
    public List<TribeCard> getTribeCardsInternal() {
        return tribeCards;
    }

    /**
     * Returns the internal building-card list.
     * <p>
     * This method is intentionally package-private and must only be used
     * by trusted model classes that need direct access for internal logic.
     *
     * @return the internal building-card list
     */
    public List<BuildingCard> getBuildingCardsInternal() {
        return buildingCards;
    }

    /**
     * Returns all cards currently in the row, preserving the row order:
     * tribe cards first, then building cards.
     *
     * @return a copy of all cards in the row
     */
    public List<Card> getAllCards(){
        List<Card> allCards = new ArrayList<>();

        allCards.addAll(tribeCards);
        allCards.addAll(buildingCards);

        return allCards;
    }

    /**
     * Removes all tribe cards from the row.
     * Building cards remain unchanged.
     */
    public void clearTribeCards(){
        tribeCards.clear();
    }

    /**
     * Removes all building cards from the row.
     * Tribe cards remain unchanged.
     */
    public void clearBuildingCards(){
        buildingCards.clear();
    }


    /**
     * Moves all tribe cards from this row to the destination row.
     *
     * @param destination the destination row
     */
    public void moveTribeCardsTo(CardRow destination) {
        for (TribeCard card : tribeCards) {
            destination.addTribeCard(card);
        }
        clearTribeCards();
    }

    /**
     * Moves all building cards from this row to the destination row.
     *
     * @param destination the destination row
     */
    public void moveBuildingCardsTo(CardRow destination) {
        for (BuildingCard card : buildingCards) {
            destination.addBuildingCard(card);
        }
        clearBuildingCards();
    }
}
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
    public void addTribeCard(TribeCard card){
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
     * Removes a tribe card from the row.
     *
     * @param card the tribe card to remove
     * @throws IllegalArgumentException if the card is not present
     */
    public void removeTribeCard(TribeCard card){
        if(!tribeCards.remove(card)){
            throw new IllegalArgumentException("Tribe card is not present in this row");
        }
    }

    /**
     * Removes a building card from the row.
     *
     * @param card the building card to remove
     * @throws IllegalArgumentException if the card is not present
     */
    public void removeBuildingCard(BuildingCard card){
        if(!buildingCards.remove(card)){
            throw new IllegalArgumentException("Building card is not present in this row");
        }
    }

    /**
     * Returns all tribe cards currently in the row.
     *
     * @return a copy of the tribe cards in the row
     */
    public List<TribeCard> getTribeCards(){
        return new ArrayList<>(tribeCards);
    }

    /**
     * Returns all building cards currently in the row.
     *
     * @return a copy of the building cards in the row
     */
    public List<BuildingCard> getBuildingCards(){
        return new ArrayList<>(buildingCards);
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
     * Checks whether the row contains the specified tribe card.
     *
     * @param card the card to search for
     * @return true if the card is present
     */
    public boolean containsTribeCard(TribeCard card){
        return tribeCards.contains(card);
    }

    /**
     * Checks whether the row contains the specified building card.
     *
     * @param card the card to search for
     * @return true if the card is present
     */
    public boolean containsBuildingCard(BuildingCard card){
        return buildingCards.contains(card);
    }

    /**
     * Checks whether the row contains no cards at all.
     *
     * @return true if both groups are empty
     */
    public boolean isEmpty(){
        return tribeCards.isEmpty() && buildingCards.isEmpty();
    }
}
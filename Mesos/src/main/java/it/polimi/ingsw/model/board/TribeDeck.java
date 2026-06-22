package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.game.Era;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Represents the Tribe deck of the game.
 * It stores all tribe cards in draw order and keeps track
 * of the current era reached by the deck.
 *
 * @author Diana
 */

public class TribeDeck {
    private final Deque<TribeCard> cards;
    private Era currentEra;

    /**
     * Creates a new tribe deck with the given cards and starting era.
     *
     * @param cards the cards in draw order
     * @param currentEra the inital era of the deck
     */
    public TribeDeck(Deque<TribeCard> cards, Era currentEra) {
        this.cards = new ArrayDeque<>(cards);
        this.currentEra = currentEra;
    }

    /**
     * Draws the top card from the deck.
     * If the drawn card belongs to a later era than the current one,
     * the deck era is updated accordingly.
     *
     * @return the drawn tribe card
     * @throws IllegalStateException if the deck is empty
     */
    public TribeCard draw(){
        if (cards.isEmpty()){
            throw new IllegalStateException("Cannot draw from an empty deck");
        }

        TribeCard drawnCard = cards.removeFirst();

        if(drawnCard.getEra().ordinal() > currentEra.ordinal()){
            currentEra = drawnCard.getEra();
        }

        return drawnCard;
    }

    /**
     * Checks whether the deck is empty.
     *
     * @return true if the deck has no cards left, false otherwise
     */
    public boolean isEmpty(){
        return cards.isEmpty();
    }

    /**
     * Returns the current era of the deck.
     *
     * @return the current era
     */
    public Era getCurrentEra(){
        return currentEra;
    }

    /**
     * Returns the number of cards currently remaining in the deck.
     *
     * @return the amount of cards left to be drawn
     */
    public int remaining(){
        return cards.size();
    }
}
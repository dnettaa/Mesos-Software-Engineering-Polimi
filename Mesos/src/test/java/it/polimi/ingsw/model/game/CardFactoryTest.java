package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link CardFactory}.
 * This class verifies the correct creation of {@link TribeDeck}
 * and {@link BuildingDeck} instances starting from JSON data.
 * The tests validate:
 * Correct initialization (non-null, non-empty decks)
 * Filtering logic based on number of players
 * Correct number of building cards per era
 * Structural consistency of generated decks
 * Encapsulation guarantees (defensive copies)
 *
 * @author Andrea Markvukaj
 */
class CardFactoryTest {
    // TRIBE DECK
    /**
     * Verifies that {@link CardFactory#createTribeDeck(int)} returns a non-null deck.
     */
    @Test
    void testCreateTribeDeck_notNull() {
        TribeDeck deck = CardFactory.createTribeDeck(3);
        assertNotNull(deck);
    }

    /**
     * Verifies that the created {@link TribeDeck} is not empty.
     */
    @Test
    void testCreateTribeDeck_notEmpty() {
        TribeDeck deck = CardFactory.createTribeDeck(3);
        assertFalse(deck.isEmpty());
    }

    /**
     * Verifies that the {@link TribeDeck} actually contains cards.
     */
    @Test
    void testCreateTribeDeck_containsCards() {
        TribeDeck deck = CardFactory.createTribeDeck(3);

        int count = 0;
        while (!deck.isEmpty()) {
            deck.draw();
            count++;
        }

        assertTrue(count > 0);
    }

    /**
     * Verifies that increasing the number of players results in a deck
     * with at least the same or greater number of cards.
     */
    @Test
    void testCreateTribeDeck_respectsMinPlayers() {
        TribeDeck deck2 = CardFactory.createTribeDeck(2);
        TribeDeck deck5 = CardFactory.createTribeDeck(5);

        int size2 = countDeck(deck2);
        int size5 = countDeck(deck5);

        assertTrue(size5 >= size2);
    }

    /**
     * Verifies that the initial era of the {@link TribeDeck} is Era1.
     */
    @Test
    void testCreateTribeDeck_initialEraIsEra1() {
        TribeDeck deck = CardFactory.createTribeDeck(3);
        assertEquals(Era.Era1, deck.getCurrentEra());
    }
    // BUILDING DECK
    /**
     * Verifies that {@link CardFactory#createBuildingDeck(int)} returns a non-null deck.
     */
    @Test
    void testCreateBuildingDeck_notNull() {
        BuildingDeck deck = CardFactory.createBuildingDeck(3);
        assertNotNull(deck);
    }

    /**
     * Verifies that the number of cards per era is correct for 2 players.
     */
    @Test
    void testCreateBuildingDeck_correctSize_2Players() {
        BuildingDeck deck = CardFactory.createBuildingDeck(2);

        assertEquals(1, deck.revealAll(Era.Era1).size());
        assertEquals(2, deck.revealAll(Era.Era2).size());
        assertEquals(3, deck.revealAll(Era.Era3).size());
    }

    /**
     * Verifies that the number of cards per era is correct for 5 players.
     */
    @Test
    void testCreateBuildingDeck_correctSize_5Players() {
        BuildingDeck deck = CardFactory.createBuildingDeck(5);

        assertEquals(2, deck.revealAll(Era.Era1).size());
        assertEquals(3, deck.revealAll(Era.Era2).size());
        assertEquals(5, deck.revealAll(Era.Era3).size());
    }

    /**
     * Verifies that no era list in the {@link BuildingDeck} is empty.
     */
    @Test
    void testCreateBuildingDeck_noEmptyLists() {
        BuildingDeck deck = CardFactory.createBuildingDeck(4);

        assertFalse(deck.revealAll(Era.Era1).isEmpty());
        assertFalse(deck.revealAll(Era.Era2).isEmpty());
        assertFalse(deck.revealAll(Era.Era3).isEmpty());
    }

    /**
     * Verifies that {@link BuildingDeck#revealAll(Era)} returns a defensive copy.
     * Modifying the returned list must not affect the internal state of the deck.
     */
    @Test
    void testRevealAll_defensiveCopy() {
        BuildingDeck deck = CardFactory.createBuildingDeck(3);

        var list = deck.revealAll(Era.Era1);
        list.clear();

        assertFalse(deck.revealAll(Era.Era1).isEmpty());
    }

    /**
     * Counts the number of cards in a {@link TribeDeck} by drawing all cards.
     *
     * @param deck the deck to count
     * @return total number of cards in the deck
     */
    private int countDeck(TribeDeck deck) {
        int count = 0;
        while (!deck.isEmpty()) {
            deck.draw();
            count++;
        }
        return count;
    }
}
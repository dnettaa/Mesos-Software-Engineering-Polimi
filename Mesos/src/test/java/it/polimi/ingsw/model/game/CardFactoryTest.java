package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.BuildingDeck;
import it.polimi.ingsw.model.board.TribeDeck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests deck creation from configuration data through {@link CardFactory}.
 */
class CardFactoryTest {

    /**
     * Setup: the factory is asked to create a tribe deck for three players.
     * Action: inspect the returned deck.
     * Expected behavior: the deck exists, starts from Era 1, and contains drawable cards.
     * Edge case: configuration loading must produce a usable deck, not only a non-null object.
     */
    @Test
    void createTribeDeckShouldReturnUsableEraOneDeck() {
        TribeDeck deck = CardFactory.createTribeDeck(3);

        assertNotNull(deck);
        assertEquals(Era.Era1, deck.getCurrentEra());
        assertFalse(deck.isEmpty());
        assertTrue(countDeck(deck) > 0);
    }

    /**
     * Setup: tribe decks are created for the minimum and maximum supported player counts.
     * Action: count the drawable cards in both decks.
     * Expected behavior: a larger player count includes at least as many eligible cards.
     * Edge case: min-player filtering must not remove cards needed by larger games.
     */
    @Test
    void createTribeDeckShouldRespectMinimumPlayerFiltering() {
        int twoPlayerCardCount = countDeck(CardFactory.createTribeDeck(2));
        int fivePlayerCardCount = countDeck(CardFactory.createTribeDeck(5));

        assertTrue(fivePlayerCardCount >= twoPlayerCardCount);
    }

    /**
     * Setup: the factory creates building decks for two and five players.
     * Action: inspect the number of configured buildings in each era.
     * Expected behavior: each player count receives the expected era distribution.
     * Edge case: era-specific filtering is verified independently for small and large games.
     */
    @Test
    void createBuildingDeckShouldUseExpectedEraDistributionByPlayerCount() {
        BuildingDeck twoPlayerDeck = CardFactory.createBuildingDeck(2);
        BuildingDeck fivePlayerDeck = CardFactory.createBuildingDeck(5);

        assertEquals(1, twoPlayerDeck.revealAll(Era.Era1).size());
        assertEquals(2, twoPlayerDeck.revealAll(Era.Era2).size());
        assertEquals(3, twoPlayerDeck.revealAll(Era.Era3).size());

        assertEquals(2, fivePlayerDeck.revealAll(Era.Era1).size());
        assertEquals(3, fivePlayerDeck.revealAll(Era.Era2).size());
        assertEquals(5, fivePlayerDeck.revealAll(Era.Era3).size());
    }

    /**
     * Setup: a building deck is created for a mid-sized game.
     * Action: reveal all building cards for every era.
     * Expected behavior: each era list contains at least one configured building.
     * Edge case: no era should silently disappear from the configured deck.
     */
    @Test
    void createBuildingDeckShouldPopulateEveryEra() {
        BuildingDeck deck = CardFactory.createBuildingDeck(4);

        assertNotNull(deck);
        assertFalse(deck.revealAll(Era.Era1).isEmpty());
        assertFalse(deck.revealAll(Era.Era2).isEmpty());
        assertFalse(deck.revealAll(Era.Era3).isEmpty());
    }

    /**
     * Setup: a building deck is created and an era list is revealed.
     * Action: clear the returned list.
     * Expected behavior: the deck internal state remains unchanged.
     * Edge case: callers cannot mutate the deck through the revealed collection.
     */
    @Test
    void revealAllShouldReturnDefensiveCopy() {
        BuildingDeck deck = CardFactory.createBuildingDeck(3);
        var revealedCards = deck.revealAll(Era.Era1);

        revealedCards.clear();

        assertFalse(deck.revealAll(Era.Era1).isEmpty());
    }

    private int countDeck(TribeDeck deck) {
        int count = 0;
        while (!deck.isEmpty()) {
            deck.draw();
            count++;
        }
        return count;
    }
}

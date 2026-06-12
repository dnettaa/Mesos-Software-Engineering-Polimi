package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.ArtistCard;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.GathererCard;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.ShamanCard;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link TribeDeck}, verifying draw order, era progression, and empty-deck behavior.
 *
 * @author Diana
 */
class TribeDeckTest {

    private TribeDeck tribeDeck;

    @BeforeEach
    void setUp() {
        Deque<TribeCard> cards = new ArrayDeque<>();
        cards.addLast(new HunterCard(Era.Era1, "H1", false));
        cards.addLast(new ArtistCard(Era.Era1, "A1"));
        cards.addLast(new ShamanCard(Era.Era2, "S1", 2));
        cards.addLast(new BuilderCard(Era.Era2, "B1", 1, 3));
        cards.addLast(new GathererCard(Era.Era3, "G1"));

        tribeDeck = new TribeDeck(cards, Era.Era1);
    }

    /**
     * Verifies that drawing returns the first card in deck order.
     * Setup: a deck whose first card is H1.
     * Action: draw one card.
     * Expected behavior: H1 is returned and the deck is no longer at full size.
     * Edge case covered: deck order starts from the front of the configured deque.
     */
    @Test
    void drawShouldReturnFirstCard() {
        assertEquals("H1", tribeDeck.draw().getId());
        assertEquals(4, tribeDeck.remaining());
    }

    /**
     * Verifies that repeated draws preserve the configured card order.
     * Setup: a five-card deck spanning three eras.
     * Action: draw all cards.
     * Expected behavior: IDs are returned in insertion order.
     * Edge case covered: no internal shuffling occurs during deterministic test setup.
     */
    @Test
    void drawShouldPreserveDeckOrder() {
        List<String> drawnIds = List.of(
                tribeDeck.draw().getId(),
                tribeDeck.draw().getId(),
                tribeDeck.draw().getId(),
                tribeDeck.draw().getId(),
                tribeDeck.draw().getId()
        );

        assertEquals(List.of("H1", "A1", "S1", "B1", "G1"), drawnIds);
    }

    /**
     * Verifies that drawing a card from a later era updates the deck's current era.
     * Setup: the deck starts in Era 1 and contains Era 2 cards after two Era 1 cards.
     * Action: draw cards until the first Era 2 card is drawn.
     * Expected behavior: the current era changes from Era 1 to Era 2.
     * Edge case covered: board setup uses deck era progression to reveal new buildings.
     */
    @Test
    void drawShouldUpdateCurrentEraWhenLaterEraAppears() {
        tribeDeck.draw();
        tribeDeck.draw();
        assertEquals(Era.Era1, tribeDeck.getCurrentEra());

        tribeDeck.draw();

        assertEquals(Era.Era2, tribeDeck.getCurrentEra());
    }

    /**
     * Verifies that drawing from an empty deck fails clearly.
     * Setup: all cards have already been drawn.
     * Action: draw one additional card.
     * Expected behavior: an {@link IllegalStateException} is thrown.
     * Edge case covered: board refill must not silently draw from an empty deck.
     */
    @Test
    void drawShouldThrowWhenDeckIsEmpty() {
        while (!tribeDeck.isEmpty()) {
            tribeDeck.draw();
        }

        assertThrows(IllegalStateException.class, () -> tribeDeck.draw());
    }

    /**
     * Verifies empty-state reporting before and after all cards are drawn.
     * Setup: a non-empty deck.
     * Action: draw every card.
     * Expected behavior: {@link TribeDeck#isEmpty()} changes from false to true.
     * Edge case covered: consumers can reliably stop drawing when the deck is exhausted.
     */
    @Test
    void isEmptyShouldReflectDeckExhaustion() {
        assertFalse(tribeDeck.isEmpty());

        while (!tribeDeck.isEmpty()) {
            tribeDeck.draw();
        }

        assertTrue(tribeDeck.isEmpty());
        assertEquals(0, tribeDeck.remaining());
    }
}

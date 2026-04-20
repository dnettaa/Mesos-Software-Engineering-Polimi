package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShamanCard symbols.
 */
class ShamanCardTest {
    @Test
    void testShamanSymbols() {
        ShamanCard card = new ShamanCard(Era.Era1, "S1", 3);
        assertEquals(3, card.getShamanSymbols());
    }
}
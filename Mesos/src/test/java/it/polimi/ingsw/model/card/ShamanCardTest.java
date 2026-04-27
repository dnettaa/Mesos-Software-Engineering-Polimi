package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShamanCard properties and acquisition logic.
 */
class ShamanCardTest {

    @Test
    void testShamanSymbols() {
        ShamanCard card = new ShamanCard(Era.Era1, "S1", 3);
        assertEquals(3, card.getShamanSymbols());
    }

    /**
     * Verifies that the applyTo method correctly adds the Shaman to the player's tribe.
     */
    @Test
    void testApplyTo() {
        // Setup
        ShamanCard card = new ShamanCard(Era.Era1, "S1", 3);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Execution
        card.applyTo(player);

        // Verification: The tribe should now have exactly 1 Shaman
        assertEquals(1, player.getTribe().countByType(CharacterType.SHAMAN));
    }
}
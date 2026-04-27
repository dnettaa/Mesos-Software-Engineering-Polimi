package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HunterCard acquisition and food bonus logic.
 */
class HunterCardTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("HunterTester", null, new Tribe(), 0, 0);
    }

    @Test
    void testHunterFoodBonusEnabled() {
        // First hunter (no bonus)
        new HunterCard(Era.Era1, "H1", false).applyTo(player);

        // Second hunter with bonus enabled
        HunterCard bonusCard = new HunterCard(Era.Era1, "H2", true);
        bonusCard.applyTo(player);

        // Should gain 2 food (1 for each hunter in the tribe)
        assertEquals(2, player.getFood());
    }

    @Test
    void testHunterFoodBonusDisabled() {
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        HunterCard noBonusCard = new HunterCard(Era.Era1, "H2", false);
        noBonusCard.applyTo(player);

        assertEquals(0, player.getFood());
    }
}
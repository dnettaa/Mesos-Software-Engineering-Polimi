package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TurnOrderBonusCard immediate food reward.
 */
class TurnOrderBonusCardTest {

    /**
     * Verifies that the building grants the correct food bonus when the player is placed in the turn order track.
     */
    @Test
    void testOnTurnOrderPlaced() {
        Player player = new Player("P1", null, new Tribe(), 0, 0);
        // Food bonus is 3
        TurnOrderBonusCard building = new TurnOrderBonusCard(Era.Era1, "B1", 0, 0, 3);

        // Execute the method with a dummy slot index (e.g., 1)
        building.onTurnOrderPlaced(1, player);

        // The player must have gained the 3 food
        assertEquals(3, player.getFood());
    }
}
package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.card.InventionType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TriggerInventorCard pair counting and bonus triggering.
 */
class TriggerInventorCardTest {

    /**
     * Verifies that the bonus is triggered only when a NEW pair of identical inventions is formed.
     */
    @Test
    void testTriggerOnNewPair() {
        Player player = new Player("P1", null, new Tribe(), 10, 0); // Starts with 10 food

        // Add 1 inventor BEFORE acquiring the building (no pair formed)
        new InventorCard(Era.Era1, "I1", InventionType.TYPE_1).applyTo(player);

        // Acquire the building. Cost: 4, Bonus food per pair: 3
        TriggerInventorCard building = new TriggerInventorCard(Era.Era1, "B1", 4, 2, 3);
        building.applyTo(player);

        // 10 food - 4 cost = 6 food left
        assertEquals(6, player.getFood());

        // Add a different inventor -> No pair -> No bonus
        new InventorCard(Era.Era1, "I2", InventionType.TYPE_2).applyTo(player);
        assertEquals(6, player.getFood());

        // Add an inventor of the SAME type as the first -> Pair formed!
        new InventorCard(Era.Era1, "I3", InventionType.TYPE_1).applyTo(player);

        // Expected food: 6 + 3 (pair bonus) = 9
        assertEquals(9, player.getFood());
    }

    /**
     * Verifies that pairs formed before acquiring the building do not trigger the bonus.
     */
    @Test
    void testExistingPairsIgnored() {
        Player player = new Player("P1", null, new Tribe(), 10, 0);

        // Form a pair BEFORE having the building
        new InventorCard(Era.Era1, "I1", InventionType.TYPE_1).applyTo(player);
        new InventorCard(Era.Era1, "I2", InventionType.TYPE_1).applyTo(player);

        // Acquire the building (Cost: 0, Bonus: 3)
        TriggerInventorCard building = new TriggerInventorCard(Era.Era1, "B1", 0, 0, 3);
        building.applyTo(player);

        // Food should remain 10, because the pair already existed prior to acquisition
        assertEquals(10, player.getFood());
    }
}
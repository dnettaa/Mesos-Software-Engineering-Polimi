package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sustenance Event food consumption and penalties.
 */
class SustenanceEventCardTest {
    @Test
    void testSustenancePenalty() {
        // 3 PP penalty per unpaid food
        SustenanceEventCard event = new SustenanceEventCard(Era.Era1, "SU1", false, 3);
        Player player = new Player("P1", null, new Tribe(), 1, 10); // Starts with 1 food

        // Add 3 characters that DO NOT provide sustenance discounts -> Cost is 3 food
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new ShamanCard(Era.Era1, "S1", 1).applyTo(player);
        new ShamanCard(Era.Era1, "S2", 1).applyTo(player);

        event.resolveEvent(List.of(player));

        // Cost 3, Discount 0, Paid 1, Unpaid 2. Penalty = 2 * 3 = 6 PP.
        // 10 - 6 = 4 PP.
        assertEquals(0, player.getFood());
        assertEquals(4, player.getPrestigePoints());
    }
}
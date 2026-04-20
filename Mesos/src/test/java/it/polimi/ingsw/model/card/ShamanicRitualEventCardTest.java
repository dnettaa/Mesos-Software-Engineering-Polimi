package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Shamanic Ritual majorities and minorities.
 */
class ShamanicRitualEventCardTest {
    @Test
    void testRitualMajorityAndMinority() {
        ShamanicRitualEventCard event = new ShamanicRitualEventCard(Era.Era1, "SR1", false, 10, 5);

        Player p1 = new Player("Winner", null, new Tribe(), 0, 0);
        Player p2 = new Player("Loser", null, new Tribe(), 0, 0);

        // P1 has 3 symbols, P2 has 1
        new ShamanCard(Era.Era1, "S1", 3).applyTo(p1);
        new ShamanCard(Era.Era1, "S2", 1).applyTo(p2);

        event.resolveEvent(List.of(p1, p2));

        assertEquals(10, p1.getPrestigePoints()); // Majority reward
        assertEquals(-5, p2.getPrestigePoints()); // Minority penalty
    }
}
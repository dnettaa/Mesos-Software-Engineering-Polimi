package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Hunt Event resolution.
 */
class HuntEventCardTest {
    @Test
    void testHuntResolution() {
        HuntEventCard event = new HuntEventCard(Era.Era1, "HE1", false, 2);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Add 2 hunters
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", false).applyTo(player);

        event.resolveEvent(List.of(player));

        // 2 Hunters -> 2 Food, 4 PP (2*2)
        assertEquals(2, player.getFood());
        assertEquals(4, player.getPrestigePoints());
    }
}
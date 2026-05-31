package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link HuntEventCard}, verifying hunt rewards based on hunter count.
 *
 * @author Diana
 */
class HuntEventCardTest {

    /**
     * Verifies that hunt resolution grants food and prestige for each hunter.
     * Setup: a player has two hunter cards and the hunt event awards two prestige points per hunter.
     * Action: resolve the hunt event for that player.
     * Expected behavior: the player gains two food and four prestige points.
     * Edge case covered: event resolution scales rewards with the current tribe composition.
     */
    @Test
    void resolveEventShouldRewardFoodAndPrestigeForHunters() {
        HuntEventCard event = new HuntEventCard(Era.Era1, "HE1", false, 2);
        Player player = new Player("P1", TotemColor.RED, new Tribe(), 0, 0);
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", false).applyTo(player);

        event.resolveEvent(List.of(player));

        assertEquals(2, player.getFood());
        assertEquals(4, player.getPrestigePoints());
    }
}

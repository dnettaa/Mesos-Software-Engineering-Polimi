package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link HunterCard}, verifying acquisition and optional food bonus logic.
 *
 * @author Diana
 */
class HunterCardTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("HunterTester", TotemColor.RED, new Tribe(), 0, 0);
    }

    /**
     * Verifies that a hunter with food bonus grants food based on total hunters after acquisition.
     * Setup: the player already has one hunter without bonus.
     * Action: acquire a second hunter with bonus enabled.
     * Expected behavior: the player gains two food, one for each hunter now in the tribe.
     * Edge case covered: the acquired card itself is included in the hunter count.
     */
    @Test
    void applyToShouldGrantFoodWhenHunterBonusIsEnabled() {
        new HunterCard(Era.Era1, "H1", false).applyTo(player);

        new HunterCard(Era.Era1, "H2", true).applyTo(player);

        assertEquals(2, player.getFood());
        assertEquals(2, player.getTribe().countByType(CharacterType.HUNTER));
    }

    /**
     * Verifies that a hunter without food bonus only joins the tribe.
     * Setup: the player has one hunter and zero food.
     * Action: acquire another hunter with bonus disabled.
     * Expected behavior: food remains zero while hunter count increases.
     * Edge case covered: not every hunter acquisition grants resources.
     */
    @Test
    void applyToShouldNotGrantFoodWhenHunterBonusIsDisabled() {
        new HunterCard(Era.Era1, "H1", false).applyTo(player);

        new HunterCard(Era.Era1, "H2", false).applyTo(player);

        assertEquals(0, player.getFood());
        assertEquals(2, player.getTribe().countByType(CharacterType.HUNTER));
    }
}

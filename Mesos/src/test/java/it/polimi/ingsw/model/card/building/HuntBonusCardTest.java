package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the hunt-event bonuses exposed by {@link HuntBonusCard}.
 */
class HuntBonusCardTest {

    /**
     * Setup: a hunt-bonus building is created with separate food and prestige bonuses.
     * Action: query both hunt-related bonus accessors.
     * Expected behavior: each accessor returns the configured value without mixing the two effects.
     * Edge case: food and prestige bonuses can be different values.
     */
    @Test
    void huntBonusAccessorsShouldReturnConfiguredValues() {
        HuntBonusCard building = new HuntBonusCard(Era.Era1, "B1", 2, 2, 1, 2);

        assertEquals(1, building.getHunterBonusFood());
        assertEquals(2, building.getHuntBonusPP());
    }
}

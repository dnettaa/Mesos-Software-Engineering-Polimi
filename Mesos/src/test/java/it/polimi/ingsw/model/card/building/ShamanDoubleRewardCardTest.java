package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the ritual winner reward multiplier flag exposed by {@link ShamanDoubleRewardCard}.
 */
class ShamanDoubleRewardCardTest {

    /**
     * Setup: a double-reward shaman building is created.
     * Action: query whether it doubles the ritual winner reward.
     * Expected behavior: the flag is enabled for this building type.
     * Edge case: the effect is represented as a capability flag, not as immediate prestige.
     */
    @Test
    void doublesWinnerRewardShouldReturnTrue() {
        ShamanDoubleRewardCard building = new ShamanDoubleRewardCard(Era.Era1, "B1", 0, 0);

        assertTrue(building.doublesWinnerReward());
    }
}

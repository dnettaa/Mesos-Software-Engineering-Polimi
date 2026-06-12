package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the ritual minority protection flag exposed by {@link ShamanNoPenaltyCard}.
 */
class ShamanNoPenaltyCardTest {

    /**
     * Setup: a no-penalty shaman building is created.
     * Action: query whether it avoids the ritual minority penalty.
     * Expected behavior: the flag is enabled for this building type.
     * Edge case: the effect is represented as a capability flag and is consumed by event resolution.
     */
    @Test
    void avoidsMinorityPenaltyShouldReturnTrue() {
        ShamanNoPenaltyCard building = new ShamanNoPenaltyCard(Era.Era1, "B1", 0, 0);

        assertTrue(building.avoidsMinorityPenalty());
    }
}

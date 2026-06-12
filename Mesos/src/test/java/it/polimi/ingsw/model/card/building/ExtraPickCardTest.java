package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the extra-card phase flag exposed by {@link ExtraPickCard}.
 */
class ExtraPickCardTest {

    /**
     * Setup: an extra-pick building is created with ordinary cost and prestige values.
     * Action: query whether the building requires an extra card phase.
     * Expected behavior: the flag is enabled for this building type.
     * Edge case: the effect is independent from cost and prestige values.
     */
    @Test
    void requiresExtraCardPhaseShouldReturnTrue() {
        ExtraPickCard building = new ExtraPickCard(Era.Era1, "B1", 2, 2);

        assertTrue(building.requiresExtraCardPhase());
    }
}

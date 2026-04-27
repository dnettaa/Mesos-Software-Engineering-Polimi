package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShamanNoPenaltyCard property.
 */
class ShamanNoPenaltyCardTest {
    @Test
    void testAvoidsMinorityPenalty() {
        ShamanNoPenaltyCard building = new ShamanNoPenaltyCard(Era.Era1, "B1", 0, 0);
        assertTrue(building.avoidsMinorityPenalty()); // Must always be true
    }
}
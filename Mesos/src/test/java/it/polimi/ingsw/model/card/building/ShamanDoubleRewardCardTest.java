package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShamanDoubleRewardCard property.
 */
class ShamanDoubleRewardCardTest {
    @Test
    void testDoublesWinnerReward() {
        ShamanDoubleRewardCard building = new ShamanDoubleRewardCard(Era.Era1, "B1", 0, 0);
        assertTrue(building.doublesWinnerReward()); // Must always be true
    }
}
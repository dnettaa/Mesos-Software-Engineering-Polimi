package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExtraPickCardTest {
    @Test
    void testRequiresExtraCardPhase() {
        ExtraPickCard building = new ExtraPickCard(Era.Era1, "B1", 2, 2);
        assertTrue(building.requiresExtraCardPhase()); // Must be true
    }
}
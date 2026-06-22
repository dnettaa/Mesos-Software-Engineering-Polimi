package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the cave-painting food bonus exposed by {@link PaintingsFoodCard}.
 */
class PaintingsFoodCardTest {

    /**
     * Setup: a paintings-food building is created with a fixed bonus.
     * Action: query the paintings bonus value.
     * Expected behavior: the configured food bonus is returned unchanged.
     * Edge case: this building has no conditional state in the accessor.
     */
    @Test
    void paintingsBonusFoodShouldReturnConfiguredValue() {
        PaintingsFoodCard building = new PaintingsFoodCard(Era.Era1, "B1", 2, 2, 3);

        assertEquals(3, building.getPaintingsBonusFood());
    }
}

package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaintingsFoodCardTest {
    @Test
    void testPaintingsBonusFood() {
        PaintingsFoodCard building = new PaintingsFoodCard(Era.Era1, "B1", 2, 2, 3);
        assertEquals(3, building.getPaintingsBonusFood()); //
    }
}
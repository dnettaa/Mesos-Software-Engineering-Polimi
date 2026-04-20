package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BuilderCard properties and discount calculation.
 */
class BuilderCardTest {
    @Test
    void testBuilderProperties() {
        BuilderCard card = new BuilderCard(Era.Era1, "B1", 2, 5);
        assertEquals(2, card.getBuilderDiscount());
        assertEquals(5, card.getBuilderPrestige());
    }
}
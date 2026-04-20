package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
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

    /**
     * Verifies that the applyTo method correctly adds the Builder to the player's tribe.
     */
    @Test
    void testApplyTo() {
        BuilderCard card = new BuilderCard(Era.Era1, "B1", 2, 5);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        card.applyTo(player);

        assertEquals(1, player.getTribe().countByType(CharacterType.BUILDER));
    }
}
package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EndDoubleBuilderCard bonus calculation.
 */
class EndDoubleBuilderCardTest {

    @Test
    void testCalculateEndGameBonus() {
        EndDoubleBuilderCard building = new EndDoubleBuilderCard(Era.Era1, "B1", 0, 0);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Add two builders with prestige points
        new BuilderCard(Era.Era1, "BLD1", 1, 3).applyTo(player); // 3 PP
        new BuilderCard(Era.Era1, "BLD2", 1, 4).applyTo(player); // 4 PP

        // Total builder prestige is 7, so the bonus should double it (returning 7 as extra bonus)
        assertEquals(7, building.calculateEndGameBonus(player));
    }
}
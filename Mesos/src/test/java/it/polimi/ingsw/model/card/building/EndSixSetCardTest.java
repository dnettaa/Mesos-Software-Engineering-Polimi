package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EndSixSetCard bonus calculation.
 */
class EndSixSetCardTest {

    @Test
    void testCalculateEndGameBonus() {
        EndSixSetCard building = new EndSixSetCard(Era.Era1, "B1", 0, 0);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Initially 0 sets, bonus should be 0
        assertEquals(0, building.calculateEndGameBonus(player));

        // Add 1 of each of the 6 Character types to complete exactly 1 set
        new HunterCard(Era.Era1, "C1", false).applyTo(player);
        new BuilderCard(Era.Era1, "C2", 0, 0).applyTo(player);
        new ShamanCard(Era.Era1, "C3", 1).applyTo(player);
        new InventorCard(Era.Era1, "C4", null).applyTo(player);
        new ArtistCard(Era.Era1, "C5").applyTo(player);
        new GathererCard(Era.Era1, "C6").applyTo(player);

        // 1 complete set * 6 PP = 6 PP expected
        assertEquals(6, building.calculateEndGameBonus(player));
    }
}
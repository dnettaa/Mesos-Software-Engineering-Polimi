package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TriggerSetCard full set detection and bonus triggering.
 */
class TriggerSetCardTest {

    /**
     * Verifies that the bonus is triggered only when a new complete set is formed after acquisition.
     */
    @Test
    void testTriggerOnNewSet() {
        Player player = new Player("P1", null, new Tribe(), 10, 0);

        // Acquire the building (Cost 0, Set completion bonus: 5 food)
        TriggerSetCard building = new TriggerSetCard(Era.Era1, "B1", 0, 0, 5);
        building.applyTo(player);

        // Add 5 different types of cards
        new HunterCard(Era.Era1, "C1", false).applyTo(player);
        new BuilderCard(Era.Era1, "C2", 0, 0).applyTo(player);
        new ShamanCard(Era.Era1, "C3", 1).applyTo(player);
        new InventorCard(Era.Era1, "C4", null).applyTo(player);
        new ArtistCard(Era.Era1, "C5").applyTo(player);

        // Food should still be 10, set is not complete (5/6)
        assertEquals(10, player.getFood());

        // Add the 6th type (Gatherer) to complete the set
        new GathererCard(Era.Era1, "C6").applyTo(player);

        // Food should increase by the bonus: 10 + 5 = 15
        assertEquals(15, player.getFood());
    }
}
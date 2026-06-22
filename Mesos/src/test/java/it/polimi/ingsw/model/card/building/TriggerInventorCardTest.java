package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.InventionType;
import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the inventor-pair trigger provided by {@link TriggerInventorCard}.
 */
class TriggerInventorCardTest {

    /**
     * Setup: a player owns one inventor, then acquires the trigger building.
     * Action: add a different inventor and then a matching inventor.
     * Expected behavior: only the newly completed pair grants the configured food bonus.
     * Edge case: unrelated invention types must not trigger the pair bonus.
     */
    @Test
    void triggerShouldGrantFoodOnlyWhenNewInventorPairIsCompleted() {
        Player player = new Player("P1", null, new Tribe(), 10, 0);
        new InventorCard(Era.Era1, "I1", InventionType.TYPE_1).applyTo(player);
        TriggerInventorCard building = new TriggerInventorCard(Era.Era1, "B1", 4, 2, 3);

        building.applyTo(player);
        assertEquals(6, player.getFood());

        new InventorCard(Era.Era1, "I2", InventionType.TYPE_2).applyTo(player);
        assertEquals(6, player.getFood());

        new InventorCard(Era.Era1, "I3", InventionType.TYPE_1).applyTo(player);
        assertEquals(9, player.getFood());
    }

    /**
     * Setup: a player already has a completed inventor pair before acquiring the trigger building.
     * Action: apply the trigger building to the player.
     * Expected behavior: existing pairs do not retroactively grant food.
     * Edge case: the trigger tracks future changes, not historical tribe contents.
     */
    @Test
    void triggerShouldIgnorePairsCompletedBeforeBuildingWasAcquired() {
        Player player = new Player("P1", null, new Tribe(), 10, 0);
        new InventorCard(Era.Era1, "I1", InventionType.TYPE_1).applyTo(player);
        new InventorCard(Era.Era1, "I2", InventionType.TYPE_1).applyTo(player);
        TriggerInventorCard building = new TriggerInventorCard(Era.Era1, "B1", 0, 0, 3);

        building.applyTo(player);

        assertEquals(10, player.getFood());
    }
}

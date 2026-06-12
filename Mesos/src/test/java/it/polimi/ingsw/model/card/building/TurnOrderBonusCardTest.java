package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the immediate food reward provided by {@link TurnOrderBonusCard}.
 */
class TurnOrderBonusCardTest {

    /**
     * Setup: a player starts with no food and owns no other effects.
     * Action: trigger the turn-order placement hook on a food-bonus building.
     * Expected behavior: the player gains exactly the configured food amount.
     * Edge case: the slot index does not change the fixed reward value.
     */
    @Test
    void onTurnOrderPlacedShouldGrantConfiguredFoodBonus() {
        Player player = new Player("P1", null, new Tribe(), 0, 0);
        TurnOrderBonusCard building = new TurnOrderBonusCard(Era.Era1, "B1", 0, 0, 3);

        building.onTurnOrderPlaced(1, player);

        assertEquals(3, player.getFood());
    }
}

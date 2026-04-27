package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SustenanceDiscountCard dynamic discount calculation.
 */
class SustenanceDiscountCardTest {

    /**
     * Verifies that the building correctly calculates the discount based on the target character type count.
     */
    @Test
    void testGetSustenanceDiscount() {
        // Card providing 1 food discount for each HUNTER owned
        SustenanceDiscountCard building = new SustenanceDiscountCard(Era.Era1, "B1", 0, 0, CharacterType.HUNTER);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Zero hunters, zero discount
        assertEquals(0, building.getSustenanceDiscount(player));

        // Add 2 Hunters
        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", false).applyTo(player);

        // Discount should be updated to 2
        assertEquals(2, building.getSustenanceDiscount(player));
    }
}
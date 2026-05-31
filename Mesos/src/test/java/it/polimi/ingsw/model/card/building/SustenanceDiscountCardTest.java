package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the character-count based discount provided by {@link SustenanceDiscountCard}.
 */
class SustenanceDiscountCardTest {

    /**
     * Setup: a sustenance discount building targets hunters, and the player initially owns no hunters.
     * Action: query the discount before and after adding hunter cards to the tribe.
     * Expected behavior: the discount equals the number of owned cards matching the configured type.
     * Edge case: a player with no matching cards receives no discount.
     */
    @Test
    void getSustenanceDiscountShouldCountConfiguredCharacterType() {
        SustenanceDiscountCard building = new SustenanceDiscountCard(Era.Era1, "B1", 0, 0, CharacterType.HUNTER);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        assertEquals(0, building.getSustenanceDiscount(player));

        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", false).applyTo(player);

        assertEquals(2, building.getSustenanceDiscount(player));
    }
}

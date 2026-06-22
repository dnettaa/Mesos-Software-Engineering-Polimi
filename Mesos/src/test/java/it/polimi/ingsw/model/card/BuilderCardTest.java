package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link BuilderCard}, verifying builder-specific properties and
 * acquisition behavior.
 *
 * @author Diana
 */
class BuilderCardTest {

    /**
     * Verifies that a builder exposes its building discount and end-game prestige value.
     * Setup: a builder card is created with discount two and prestige five.
     * Action: read the builder-specific getters.
     * Expected behavior: both configured values are returned unchanged.
     * Edge case covered: builder cards carry both an in-game discount and a final-scoring value.
     */
    @Test
    void builderShouldExposeDiscountAndPrestigeValues() {
        BuilderCard card = new BuilderCard(Era.Era1, "B1", 2, 5);

        assertEquals(2, card.getBuilderDiscount());
        assertEquals(5, card.getBuilderPrestige());
    }

    /**
     * Verifies that acquiring a builder adds it to the player's tribe.
     * Setup: a player starts with an empty tribe.
     * Action: apply a builder card to that player.
     * Expected behavior: the tribe contains exactly one builder.
     * Edge case covered: builder acquisition must update future building discounts.
     */
    @Test
    void applyToShouldAddBuilderToPlayerTribe() {
        BuilderCard card = new BuilderCard(Era.Era1, "B1", 2, 5);
        Player player = new Player("P1", TotemColor.RED, new Tribe(), 0, 0);

        card.applyTo(player);

        assertEquals(1, player.getTribe().countByType(CharacterType.BUILDER));
        assertEquals(2, player.getTribe().getBuildingDiscount());
    }
}

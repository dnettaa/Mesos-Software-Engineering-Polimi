package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.ArtistCard;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.GathererCard;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.card.ShamanCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the complete-six-type end-game bonus provided by {@link EndSixSetCard}.
 */
class EndSixSetCardTest {

    /**
     * Setup: a player starts with no character cards, then gains one card of each character type.
     * Action: calculate the complete-set end-game bonus before and after completing the set.
     * Expected behavior: no bonus is returned without a complete set, and one complete set grants six points.
     * Edge case: all six distinct character types are required.
     */
    @Test
    void calculateEndGameBonusShouldRewardCompleteSixTypeSets() {
        EndSixSetCard building = new EndSixSetCard(Era.Era1, "B1", 0, 0);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        assertEquals(0, building.calculateEndGameBonus(player));

        new HunterCard(Era.Era1, "C1", false).applyTo(player);
        new BuilderCard(Era.Era1, "C2", 0, 0).applyTo(player);
        new ShamanCard(Era.Era1, "C3", 1).applyTo(player);
        new InventorCard(Era.Era1, "C4", null).applyTo(player);
        new ArtistCard(Era.Era1, "C5").applyTo(player);
        new GathererCard(Era.Era1, "C6").applyTo(player);

        assertEquals(6, building.calculateEndGameBonus(player));
    }
}

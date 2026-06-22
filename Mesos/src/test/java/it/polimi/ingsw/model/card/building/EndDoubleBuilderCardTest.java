package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the builder-prestige end-game bonus provided by {@link EndDoubleBuilderCard}.
 */
class EndDoubleBuilderCardTest {

    /**
     * Setup: a player owns two builders with different prestige values.
     * Action: calculate the end-game bonus of the double-builder building.
     * Expected behavior: the returned bonus equals the total prestige printed on owned builders.
     * Edge case: the building returns the extra bonus only, allowing final scoring to add base builder prestige separately.
     */
    @Test
    void calculateEndGameBonusShouldMatchOwnedBuilderPrestige() {
        EndDoubleBuilderCard building = new EndDoubleBuilderCard(Era.Era1, "B1", 0, 0);
        Player player = new Player("P1", null, new Tribe(), 0, 0);
        new BuilderCard(Era.Era1, "BLD1", 1, 3).applyTo(player);
        new BuilderCard(Era.Era1, "BLD2", 1, 4).applyTo(player);

        assertEquals(7, building.calculateEndGameBonus(player));
    }
}

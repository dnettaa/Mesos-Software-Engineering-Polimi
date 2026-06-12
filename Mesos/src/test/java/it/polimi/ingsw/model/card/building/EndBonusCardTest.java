package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests base building acquisition behavior and the fixed end-game bonus of {@link EndBonusCard}.
 */
class EndBonusCardTest {

    private Player player;

    /**
     * Creates a player with enough food to buy buildings in each test.
     */
    @BeforeEach
    void setUp() {
        player = new Player("P1", null, new Tribe(), 10, 0);
    }

    /**
     * Setup: a player has enough food to buy a fixed-bonus building.
     * Action: apply the building to the player.
     * Expected behavior: the effective cost is spent and the building is stored in the tribe.
     * Edge case: building prestige points are not immediately added as player prestige during acquisition.
     */
    @Test
    void applyToShouldSpendFoodAndAddBuildingToTribe() {
        EndBonusCard building = new EndBonusCard(Era.Era1, "B01", 4, 2, 5);

        building.applyTo(player);

        assertEquals(6, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
        assertEquals(2, building.getPrestigePoints());
        assertEquals(0, player.getPrestigePoints());
    }

    /**
     * Setup: the player owns a builder that discounts future building purchases.
     * Action: query and then apply a building with a higher base cost.
     * Expected behavior: the cost is reduced by the builder discount before food is spent.
     * Edge case: discounts are applied through {@code getCostFor(Player)}, not hard-coded in the test.
     */
    @Test
    void getCostForShouldApplyBuilderDiscountBeforeSpendingFood() {
        new BuilderCard(Era.Era1, "BLD1", 3, 0).applyTo(player);
        EndBonusCard building = new EndBonusCard(Era.Era1, "B02", 5, 2, 5);

        assertEquals(2, building.getCostFor(player));

        building.applyTo(player);

        assertEquals(8, player.getFood());
    }

    /**
     * Setup: a fixed end-bonus building is created.
     * Action: calculate its end-game bonus for a player.
     * Expected behavior: the configured fixed bonus is returned.
     * Edge case: the fixed bonus does not depend on the player's current tribe contents.
     */
    @Test
    void calculateEndGameBonusShouldReturnConfiguredFixedBonus() {
        EndBonusCard building = new EndBonusCard(Era.Era1, "B03", 4, 2, 8);

        assertEquals(8, building.calculateEndGameBonus(player));
    }
}

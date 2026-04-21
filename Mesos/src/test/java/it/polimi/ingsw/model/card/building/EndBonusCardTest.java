package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EndBonusCard and base BuildingCard logic.
 */
class EndBonusCardTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("P1", null, new Tribe(), 10, 0); // Player starts with 10 food
    }

    /**
     * Verifies that acquiring a building correctly subtracts food and adds the card to the tribe.
     */
    @Test
    void testApplyToAndCost() {
        // Cost: 4, Prestige: 2, Bonus: 5
        EndBonusCard building = new EndBonusCard(Era.Era1, "B01", 4, 2, 5);

        building.applyTo(player);

        // 10 initial food - 4 cost = 6 food left
        assertEquals(6, player.getFood());
        assertEquals(1, player.getTribe().getBuildings().size());
        assertEquals(2, building.getPrestigePoints());
    }

    /**
     * Verifies that Builder cards correctly discount the building cost.
     */
    @Test
    void testBuildingDiscount() {
        // Add a builder that provides a discount of 3
        new BuilderCard(Era.Era1, "BLD1", 3, 0).applyTo(player);

        EndBonusCard building = new EndBonusCard(Era.Era1, "B02", 5, 2, 5);

        // Base cost is 5, discount is 3 -> Effective cost should be 2
        assertEquals(2, building.getCostFor(player));

        building.applyTo(player);
        // 10 initial food - 2 effective cost = 8 food left
        assertEquals(8, player.getFood());
    }

    @Test
    void testCalculateEndGameBonus() {
        EndBonusCard building = new EndBonusCard(Era.Era1, "B03", 4, 2, 8);
        assertEquals(8, building.calculateEndGameBonus(player)); // Expects the fixed bonus of 8
    }
}
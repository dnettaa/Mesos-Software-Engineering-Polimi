package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.card.ArtistCard;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.InventionType;
import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the final scoring rules applied by {@link FinalScoringCalculator}.
 */
class FinalScoringCalculatorTest {

    private Player createPlayer() {
        return new Player("test", TotemColor.RED, new Tribe(), 0, 0);
    }

    /**
     * Setup: a player owns two builders with fixed printed prestige values.
     * Action: calculate final scoring for the player.
     * Expected behavior: builder printed prestige is added to the player's prestige points.
     * Edge case: builder discounts do not affect their final printed prestige value.
     */
    @Test
    void calculateShouldAddBuilderPrintedPrestige() {
        Player player = createPlayer();
        new BuilderCard(Era.Era1, "b1", 0, 5).applyTo(player);
        new BuilderCard(Era.Era1, "b2", 2, 5).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(10, player.getPrestigePoints());
    }

    /**
     * Setup: a player owns three inventors with two distinct invention icons.
     * Action: calculate final scoring for the player.
     * Expected behavior: inventor scoring is card count multiplied by distinct icon count.
     * Edge case: duplicated invention icons increase inventor count but not distinct icon count.
     */
    @Test
    void calculateShouldScoreInventorsByCountAndDistinctIcons() {
        Player player = createPlayer();
        new InventorCard(Era.Era1, "i1", InventionType.TYPE_1).applyTo(player);
        new InventorCard(Era.Era1, "i2", InventionType.TYPE_2).applyTo(player);
        new InventorCard(Era.Era1, "i3", InventionType.TYPE_1).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(6, player.getPrestigePoints());
    }

    /**
     * Setup: a player owns five artists.
     * Action: calculate final scoring for the player.
     * Expected behavior: every complete pair of artists grants ten prestige points.
     * Edge case: the unpaired fifth artist does not grant a partial bonus.
     */
    @Test
    void calculateShouldScoreOnlyCompleteArtistPairs() {
        Player player = createPlayer();
        for (int i = 0; i < 5; i++) {
            new ArtistCard(Era.Era1, "a" + i).applyTo(player);
        }

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(20, player.getPrestigePoints());
    }

    /**
     * Setup: a player owns three buildings with only printed prestige values.
     * Action: calculate final scoring for the player.
     * Expected behavior: building printed prestige is included in the final score.
     * Edge case: buildings without end-game bonus still contribute their printed prestige.
     */
    @Test
    void calculateShouldAddBuildingPrintedPrestige() {
        Player player = createPlayer();
        new DummyBuilding(4).applyTo(player);
        new DummyBuilding(4).applyTo(player);
        new DummyBuilding(4).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(12, player.getPrestigePoints());
    }

    /**
     * Setup: a player owns builders, artists, and a building.
     * Action: calculate final scoring with all supported scoring categories present.
     * Expected behavior: the final score is the sum of each independent scoring category.
     * Edge case: the calculator must aggregate different card families without overwriting previous points.
     */
    @Test
    void calculateShouldAggregateMultipleScoringCategories() {
        Player player = createPlayer();
        new BuilderCard(Era.Era1, "b1", 0, 5).applyTo(player);
        new BuilderCard(Era.Era1, "b2", 0, 5).applyTo(player);
        new ArtistCard(Era.Era1, "a1").applyTo(player);
        new ArtistCard(Era.Era1, "a2").applyTo(player);
        new DummyBuilding(8).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(28, player.getPrestigePoints());
    }

    /**
     * Minimal building implementation used to isolate generic building prestige scoring.
     */
    static class DummyBuilding extends BuildingCard {

        DummyBuilding(int prestigePoints) {
            super(Era.Era1, "dummy", 0, prestigePoints);
        }
    }
}

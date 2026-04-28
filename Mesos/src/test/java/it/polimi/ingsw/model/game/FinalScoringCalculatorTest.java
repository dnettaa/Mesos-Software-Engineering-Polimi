package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link FinalScoringCalculator}.
 * Verifies end-game scoring logic using real card application flow.
 *
 * @author Andrea Markvukaj
 */
class FinalScoringCalculatorTest {

    private Player createPlayer() {
        return new Player("test", TotemColor.RED, new Tribe(), 0, 0);
    }

    /**
     * Verifies Builder scoring.
     */
    @Test
    void testBuilderScoring() {

        Player player = createPlayer();
        new BuilderCard(Era.Era1, "b1", 0, 5).applyTo(player);
        new BuilderCard(Era.Era1, "b2", 0, 5).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(10, player.getPrestigePoints());
    }

    /**
     * Verifies Inventor scoring: (#inventors * distinct icons).
     */
    @Test
    void testInventorScoring() {

        Player player = createPlayer();

        new InventorCard(Era.Era1, "i1", InventionType.TYPE_1).applyTo(player);
        new InventorCard(Era.Era1, "i2", InventionType.TYPE_2).applyTo(player);
        new InventorCard(Era.Era1, "i3", InventionType.TYPE_1).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(6, player.getPrestigePoints());
    }

    /**
     * Verifies Artist scoring (10 PP every 2 artists).
     */
    @Test
    void testArtistScoring() {

        Player player = createPlayer();

        for (int i = 0; i < 5; i++) {
            new ArtistCard(Era.Era1, "a" + i).applyTo(player);
        }

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(20, player.getPrestigePoints());
    }

    /**
     * Verifies Building scoring (base PP only).
     */
    @Test
    void testBuildingScoring() {

        Player player = createPlayer();

        new DummyBuilding(4).applyTo(player);
        new DummyBuilding(4).applyTo(player);
        new DummyBuilding(4).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(12, player.getPrestigePoints());
    }

    /**
     * Verifies combined scoring logic.
     */
    @Test
    void testCombinedScoring() {

        Player player = createPlayer();

        // Builders give 10
        new BuilderCard(Era.Era1, "b1", 0, 5).applyTo(player);
        new BuilderCard(Era.Era1, "b2", 0, 5).applyTo(player);

        // Artists give 10
        new ArtistCard(Era.Era1, "a1").applyTo(player);
        new ArtistCard(Era.Era1, "a2").applyTo(player);

        // Building give 8
        new DummyBuilding(8).applyTo(player);

        FinalScoringCalculator.calculate(List.of(player));

        assertEquals(28, player.getPrestigePoints());
    }

    /**
     * Dummy building used for testing.
     */
    static class DummyBuilding extends BuildingCard {

        DummyBuilding(int pp) {
            super(Era.Era1, "dummy", 0, pp);
        }
    }
}
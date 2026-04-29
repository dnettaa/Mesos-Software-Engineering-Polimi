package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;

import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EndGamePhase.
 *
 * @author Andrea Markvukaj
 */
class EndGamePhaseTest {

    /**
     * Test ensures final scoring is triggered without errors and
     * the game state is correctly set to Finished
     *
     * @author Andrea Markvukaj
     */
    @Test
    void testEndGameSetsFinished() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(Map.of("P1", TotemColor.RED), 1);

        game.setCurrentPhase(new EndGamePhase());

        game.endGame();

        assertEquals(GameState.Finished, game.getState());
    }
}
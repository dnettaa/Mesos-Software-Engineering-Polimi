package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;

import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for EndRoundPhase.
 *
 * @author Andrea Markvukaj
 */
class EndRoundPhaseTest {

    /**
     * Test ensures that round number is incremented when the game is not at the last round
     */
    @Test
    void testRoundIncrement() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(Map.of("P1", TotemColor.RED), 1);

        game.setCurrentPhase(new EndRoundPhase());

        game.endRound();

        assertEquals(2, game.getCurrentRound());
    }

    /**
     * Test ensures that the game transitions to the end state when the final round is reached
     */
    @Test
    void testTransitionToEndGame() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(Map.of("P1", TotemColor.RED), 1);

        game.setCurrentRound(10);
        game.setCurrentPhase(new EndRoundPhase());

        game.endRound();

        assertTrue(game.isGameEnded());
    }
}
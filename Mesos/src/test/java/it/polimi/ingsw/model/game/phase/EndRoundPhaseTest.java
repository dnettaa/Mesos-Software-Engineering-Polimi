package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.TotemColor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for EndRoundPhase.
 * Verifies round progression, end-game transition,
 * and correct DTO notifications through GameListener.
 *
 * @author Andrea Markvukaj
 */
class EndRoundPhaseTest {

    /**
     * Test ensures that round number is incremented
     * when the game is not at the last round.
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
     * Test ensures that the game transitions to the end state
     * when the final round is reached.
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

    /**
     * Verifies that ending a round triggers a RoundEndedDTO notification.
     */
    @Test
    void testRoundEndedDTOFired() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(Map.of("P1", TotemColor.RED), 1);

        class TestListener implements GameListener {
            boolean roundEnded = false;

            @Override
            public void onRoundEnded(RoundEndedDTO dto) {
                roundEnded = true;
            }

            @Override public void onGameStarted(GameStateSnapshot s) {}
            @Override public void onTotemPlaced(TotemPlacedDTO dto) {}
            @Override public void onCardsTaken(CardsTakenDTO dto) {}
            @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
            @Override public void onEventResolved(EventResolvedDTO dto) {}
            @Override public void onGameEnded(GameEndedDTO dto) {}
        }

        TestListener listener = new TestListener();
        game.addListener(listener);

        game.setCurrentPhase(new EndRoundPhase());
        game.endRound();

        assertTrue(listener.roundEnded);
    }

    /**
     * Verifies that reaching the final round triggers a GameEndedDTO notification.
     */
    @Test
    void testGameEndedDTOFired() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(Map.of("P1", TotemColor.RED), 1);

        class TestListener implements GameListener {
            boolean gameEnded = false;

            @Override
            public void onGameEnded(GameEndedDTO dto) {
                gameEnded = true;
            }

            @Override public void onGameStarted(GameStateSnapshot s) {}
            @Override public void onTotemPlaced(TotemPlacedDTO dto) {}
            @Override public void onCardsTaken(CardsTakenDTO dto) {}
            @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
            @Override public void onEventResolved(EventResolvedDTO dto) {}
            @Override public void onRoundEnded(RoundEndedDTO dto) {}
        }

        TestListener listener = new TestListener();
        game.addListener(listener);

        game.setCurrentRound(10);
        game.setCurrentPhase(new EndRoundPhase());

        game.endRound();

        assertTrue(listener.gameEnded);
    }
}
package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.TotemColor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EndGamePhase.
 * Verifies final scoring, state transition and DTO notifications.
 *
 * @author Andrea Markvukaj
 */
class EndGamePhaseTest {

    /**
     * Test ensures final scoring is triggered and
     * the game state is correctly set to Finished.
     */
    @Test
    void testEndGameSetsFinished() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(
                Map.of(
                        "P1", TotemColor.RED,
                        "P2", TotemColor.BLUE
                ),
                1
        );

        game.setCurrentPhase(new EndGamePhase());
        game.endGame();

        assertEquals(GameState.Finished, game.getState());
    }

    /**
     * Verifies that ending the game triggers GameEndedDTO notification.
     */
    @Test
    void testGameEndedDTOFired() {
        GameSetupService setup = new GameSetupService();
        Game game = setup.createNewGame(
                Map.of(
                        "P1", TotemColor.RED,
                        "P2", TotemColor.BLUE
                ),
                1
        );

        class TestListener implements GameListener {
            boolean called = false;
            GameEndedDTO dto;

            @Override
            public void onGameEnded(GameEndedDTO dto) {
                called = true;
                this.dto = dto;
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

        game.setCurrentPhase(new EndGamePhase());
        game.endGame();

        assertTrue(listener.called);
        assertNotNull(listener.dto);

        // controlli coerenti col nuovo DTO
        assertNotNull(listener.dto.finalPPByPlayer());
        assertNotNull(listener.dto.endGameBonusByPlayer());
        assertNotNull(listener.dto.ranking());

        // almeno un giocatore presente
        assertFalse(listener.dto.finalPPByPlayer().isEmpty());
        assertFalse(listener.dto.ranking().isEmpty());
    }
}
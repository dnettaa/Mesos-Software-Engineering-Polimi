package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameListener;
import it.polimi.ingsw.model.game.GameSetupService;
import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests round progression, final-round handling, and notifications in {@link EndRoundPhase}.
 */
class EndRoundPhaseTest {

    private Game createGame() {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        players.put("P1", TotemColor.RED);
        players.put("P2", TotemColor.BLUE);

        return new GameSetupService().createNewGame(players, 1);
    }

    /**
     * Setup: a game is not on the final round and is set to end-round phase.
     * Action: end the current round.
     * Expected behavior: the round counter advances by one.
     * Edge case: ordinary round progression must not end the game.
     */
    @Test
    void endRoundShouldIncrementRoundBeforeFinalRound() {
        Game game = createGame();

        game.setCurrentPhase(new EndRoundPhase());
        game.endRound();

        assertEquals(2, game.getCurrentRound());
    }

    /**
     * Setup: the game is set to round ten, the final round.
     * Action: end the round.
     * Expected behavior: the game reaches the finished state.
     * Edge case: final-round completion skips ordinary next-round setup.
     */
    @Test
    void endRoundShouldFinishGameOnFinalRound() {
        Game game = createGame();
        game.setCurrentRound(10);
        game.setCurrentPhase(new EndRoundPhase());

        game.endRound();

        assertTrue(game.isGameEnded());
    }

    /**
     * Setup: a listener is registered during a non-final end-round action.
     * Action: end the current round.
     * Expected behavior: a round-ended notification is emitted.
     * Edge case: ordinary round progression should notify listeners before the next placement phase.
     */
    @Test
    void endRoundShouldEmitRoundEndedDtoBeforeFinalRound() {
        Game game = createGame();
        class TestListener implements GameListener {
            boolean roundEnded = false;

            @Override public void onRoundEnded(RoundEndedDTO dto) { roundEnded = true; }
            @Override public void onGameStarted(GameStateSnapshot snapshot) {}
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
     * Setup: a listener is registered and the game is already on round ten.
     * Action: end the final round.
     * Expected behavior: a game-ended notification is emitted.
     * Edge case: final scoring notification is part of final-round transition.
     */
    @Test
    void endRoundShouldEmitGameEndedDtoOnFinalRound() {
        Game game = createGame();
        class TestListener implements GameListener {
            boolean gameEnded = false;

            @Override public void onGameEnded(GameEndedDTO dto) { gameEnded = true; }
            @Override public void onGameStarted(GameStateSnapshot snapshot) {}
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

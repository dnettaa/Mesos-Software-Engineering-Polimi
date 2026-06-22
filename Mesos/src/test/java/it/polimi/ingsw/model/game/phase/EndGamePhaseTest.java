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
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests final state transition and end-game notification payloads in {@link EndGamePhase}.
 */
class EndGamePhaseTest {

    private Game createGame() {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        players.put("P1", TotemColor.RED);
        players.put("P2", TotemColor.BLUE);

        return new GameSetupService().createNewGame(players, 1);
    }

    /**
     * Setup: a game is explicitly set to end-game phase.
     * Action: end the game.
     * Expected behavior: the game state becomes finished.
     * Edge case: final scoring and state transition are triggered through the public action.
     */
    @Test
    void endGameShouldSetFinishedState() {
        Game game = createGame();

        game.setCurrentPhase(new EndGamePhase());
        game.endGame();

        assertEquals(GameState.Finished, game.getState());
    }

    /**
     * Setup: a listener is registered before ending the game.
     * Action: execute the end-game phase.
     * Expected behavior: the listener receives final points, end-game bonuses, and ranking data.
     * Edge case: DTO collections must be populated for at least the participating players.
     */
    @Test
    void endGameShouldEmitGameEndedDtoWithScoreData() {
        Game game = createGame();
        class TestListener implements GameListener {
            boolean called = false;
            GameEndedDTO dto;

            @Override public void onGameEnded(GameEndedDTO dto) {
                called = true;
                this.dto = dto;
            }
            @Override public void onGameStarted(GameStateSnapshot snapshot) {}
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
        assertFalse(listener.dto.finalPPByPlayer().isEmpty());
        assertNotNull(listener.dto.endGameBonusByPlayer());
        assertFalse(listener.dto.ranking().isEmpty());
    }
}

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests event resolution transitions, invalid state handling, and listener notifications.
 */
class EventResolutionPhaseTest {

    private final GameSetupService setup = new GameSetupService();

    private Game createGame(int playerCount) {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < playerCount; i++) {
            players.put("P" + i, colors[i]);
        }

        return setup.createNewGame(players, 1);
    }

    private char getFreeSlot(Game game) {
        return game.getBoard()
                .buildOfferSlotsData()
                .stream()
                .filter(slot -> slot.occupantNickname() == null)
                .findFirst()
                .orElseThrow()
                .slotID();
    }

    /**
     * Captures event-resolution and round-ended callbacks.
     */
    static class TestListener implements GameListener {
        int eventsResolved = 0;
        boolean roundEnded = false;

        @Override public void onEventResolved(EventResolvedDTO dto) { eventsResolved++; }
        @Override public void onRoundEnded(RoundEndedDTO dto) { roundEnded = true; }
        @Override public void onGameStarted(GameStateSnapshot snapshot) {}
        @Override public void onTotemPlaced(TotemPlacedDTO dto) {}
        @Override public void onCardsTaken(CardsTakenDTO dto) {}
        @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
        @Override public void onGameEnded(GameEndedDTO dto) {}
    }

    /**
     * Setup: a game is manually placed in event-resolution phase.
     * Action: resolve events.
     * Expected behavior: event resolution completes and moves the game away from event-resolution phase.
     * Edge case: the test does not depend on a specific next phase beyond leaving the current phase.
     */
    @Test
    void resolveEventsShouldTransitionAwayFromEventResolutionPhase() {
        Game game = createGame(3);
        game.setCurrentPhase(new EventResolutionPhase());

        assertDoesNotThrow(game::resolveEvents);

        assertNotEquals("EventResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Setup: a game is on the final round and enters event resolution.
     * Action: resolve events.
     * Expected behavior: the phase transition remains valid for final-round flow.
     * Edge case: final-round event resolution may either prepare another placement step or reach end-game flow.
     */
    @Test
    void resolveEventsShouldSupportFinalRoundTransition() {
        Game game = createGame(3);
        game.setCurrentRound(10);
        game.setCurrentPhase(new EventResolutionPhase());

        assertDoesNotThrow(game::resolveEvents);

        assertTrue(
                game.getCurrentPhaseName().equals("TotemPlacementPhase")
                        || game.getCurrentPhaseName().equals("EndGamePhase")
        );
    }

    /**
     * Setup: the game is marked finished before event resolution.
     * Action: resolve events while in an invalid state.
     * Expected behavior: the operation is rejected.
     * Edge case: phase object alone is not enough to allow actions when the game state is finished.
     */
    @Test
    void resolveEventsShouldRejectFinishedGameState() {
        Game game = createGame(3);
        game.setState(GameState.Finished);
        game.setCurrentPhase(new EventResolutionPhase());

        assertThrows(Exception.class, game::resolveEvents);
    }

    /**
     * Setup: all players place totems, the round is ended, and a listener observes event callbacks.
     * Action: resolve lower-row events.
     * Expected behavior: event-resolution and round-ended callbacks are emitted.
     * Edge case: this exercises the event path after realistic totem placement and end-round setup.
     */
    @Test
    void resolveEventsShouldEmitEventAndRoundNotifications() {
        Game game = createGame(3);
        TestListener listener = new TestListener();
        game.addListener(listener);

        for (int i = 0; i < game.getPlayers().size(); i++) {
            game.placeTotem(game.getCurrentPlayerNickname(), getFreeSlot(game));
        }

        game.setCurrentPhase(new EndRoundPhase());
        game.endRound();
        game.setCurrentPhase(new EventResolutionPhase());
        game.resolveEvents();

        assertTrue(listener.eventsResolved > 0);
        assertTrue(listener.roundEnded);
    }
}

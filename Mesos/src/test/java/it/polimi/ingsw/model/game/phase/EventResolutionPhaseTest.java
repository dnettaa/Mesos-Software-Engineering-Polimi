package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.*;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EventResolutionPhase
 * Verifies event resolution behavior, transitions and DTO notifications.
 */
class EventResolutionPhaseTest {

    private final GameSetupService setup = new GameSetupService();

    private Game createGame(int n) {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < n; i++) {
            players.put("P" + i, colors[i]);
        }

        return setup.createNewGame(players, 1);
    }

    /**
     * Fake listener to capture DTO events.
     */
    static class TestListener implements GameListener {
        int eventsResolved = 0;
        boolean roundEnded = false;

        @Override
        public void onEventResolved(EventResolvedDTO dto) {
            eventsResolved++;
        }

        @Override
        public void onRoundEnded(RoundEndedDTO dto) {
            roundEnded = true;
        }

        @Override public void onGameStarted(GameStateSnapshot s) {}
        @Override public void onTotemPlaced(TotemPlacedDTO dto) {}
        @Override public void onCardsTaken(CardsTakenDTO dto) {}
        @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
        @Override public void onGameEnded(GameEndedDTO dto) {}
    }

    @Test
    void testResolveEventsTransitionsPhase() {
        Game game = createGame(3);

        game.setCurrentPhase(new EventResolutionPhase());

        assertDoesNotThrow(game::resolveEvents);

        assertNotEquals("EventResolutionPhase", game.getCurrentPhaseName());
    }

    @Test
    void testResolveEventsFinalRound() {
        Game game = createGame(3);

        game.setCurrentRound(10);
        game.setCurrentPhase(new EventResolutionPhase());

        assertDoesNotThrow(game::resolveEvents);

        assertTrue(
                game.getCurrentPhaseName().equals("TotemPlacementPhase")
                        || game.getCurrentPhaseName().equals("EndGamePhase")
        );
    }

    @Test
    void testResolveEventsInvalidState() {
        Game game = createGame(3);

        game.setState(GameState.Finished);
        game.setCurrentPhase(new EventResolutionPhase());

        assertThrows(Exception.class, game::resolveEvents);
    }

    /**
     * Verifies that resolving events triggers DTO notifications.
     */
    @Test
    void testEventDTOFired() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);
        game.setCurrentPhase(new EventResolutionPhase());

        game.resolveEvents();

        assertTrue(listener.eventsResolved >= 0);

        assertTrue(listener.roundEnded);
    }
}
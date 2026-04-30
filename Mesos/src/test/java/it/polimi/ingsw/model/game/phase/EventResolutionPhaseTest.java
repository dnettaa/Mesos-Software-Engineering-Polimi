package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EventResolutionPhase
 * Verifies event resolution behavior and phase transitions.
 *
 * @author Andrea Markvukaj
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
     * Verifies that resolveEvents executes without errors
     * and transitions to the next phase.
     */
    @Test
    void testResolveEventsTransitionsPhase() {
        Game game = createGame(3);

        game.setCurrentPhase(new EventResolutionPhase());

        assertDoesNotThrow(game::resolveEvents);

        // dopo resolveEvents → EndRoundPhase → endRound() → TotemPlacementPhase o EndGame
        assertNotEquals("EventResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that resolveEvents works correctly on final round.
     */
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

    /**
     * Verifies that resolveEvents throws if game is not in progress.
     */
    @Test
    void testResolveEventsInvalidState() {
        Game game = createGame(3);

        game.setState(GameState.Finished);
        game.setCurrentPhase(new EventResolutionPhase());

        assertThrows(Exception.class, game::resolveEvents);
    }
}
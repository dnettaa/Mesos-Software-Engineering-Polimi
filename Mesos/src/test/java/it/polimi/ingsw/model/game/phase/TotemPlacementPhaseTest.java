package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.exception.GameException;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for TotemPlacementPhase.
 *
 * @author Andrea Markvukaj
 */
class TotemPlacementPhaseTest {

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
     * Helper method to retrieve a valid free slot dynamically.
     */
    private char getFreeSlot(Game game) {
        return game.getBoard()
                .buildOfferSlotsData()
                .stream()
                .filter(s -> s.occupantNickname() == null)
                .findFirst()
                .orElseThrow()
                .slotID();
    }

    /**
     * Fake listener to capture DTO events.
     */
    static class TestListener implements GameListener {
        boolean called = false;
        TotemPlacedDTO dto;

        @Override
        public void onTotemPlaced(TotemPlacedDTO dto) {
            this.called = true;
            this.dto = dto;
        }

        @Override public void onGameStarted(GameStateSnapshot s) {}
        @Override public void onCardsTaken(CardsTakenDTO dto) {}
        @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
        @Override public void onEventResolved(EventResolvedDTO dto) {}
        @Override public void onRoundEnded(RoundEndedDTO dto) {}
        @Override public void onGameEnded(GameEndedDTO dto) {}
    }

    /**
     * Verifies correct player can place totem.
     */
    @Test
    void testCorrectPlayerPlacesTotem() {
        Game game = createGame(3);

        String current = game.getCurrentPlayerNickname();
        char slotID = getFreeSlot(game);

        assertDoesNotThrow(() -> game.placeTotem(current, slotID));
    }

    /**
     * Verifies wrong player cannot place totem.
     */
    @Test
    void testWrongPlayerThrows() {
        Game game = createGame(3);

        List<Player> players = game.getPlayers();

        String wrong = players.get(1).getNickname();
        char slotID = getFreeSlot(game);

        assertThrows(GameException.class,
                () -> game.placeTotem(wrong, slotID));
    }

    /**
     * Verifies phase transition after all players place totems.
     */
    @Test
    void testTransitionToOfferResolutionPhase() {
        Game game = createGame(3);

        for (int i = 0; i < game.getPlayers().size(); i++) {
            String current = game.getCurrentPlayerNickname();
            char slotID = getFreeSlot(game);

            game.placeTotem(current, slotID);
        }

        assertEquals("OfferResolutionPhase", game.getCurrentPhaseName());
    }

    /**
     * Verifies that placing a totem triggers a DTO notification.
     */
    @Test
    void testTotemPlacedDTOFired() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);

        String current = game.getCurrentPlayerNickname();
        char slotID = getFreeSlot(game);

        game.placeTotem(current, slotID);

        assertTrue(listener.called);
        assertNotNull(listener.dto);

        assertEquals(current, listener.dto.placerNickname());
        assertEquals(slotID, listener.dto.slotID());

        assertNotNull(listener.dto.nextPlayerNickname());
        assertNotNull(listener.dto.nextPhaseName());
    }
}
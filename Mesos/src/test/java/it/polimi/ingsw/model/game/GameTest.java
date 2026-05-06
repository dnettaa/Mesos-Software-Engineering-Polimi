package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.game.DTO.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.exception.GameException;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Game}.
 * Verifies core logic and listener/DTO behavior.
 *
 * @author Andrea Markvukaj
 */
class GameTest {

    private final GameSetupService setup = new GameSetupService();

    /**
     * Creates a new game instance with the specified number of players.
     *
     * @param n number of players
     * @return a new initialized Game
     */
    private Game createGame(int n) {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < n; i++) {
            players.put("P" + i, colors[i]);
        }

        return setup.createNewGame(players, 1);
    }

    /**
     * Helper: retrieves a valid free slot dynamically from the board.
     *
     * @param game the game instance
     * @return the ID of a free offer slot
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
     * Fake listener to capture events.
     */
    static class TestListener implements GameListener {
        boolean started = false;
        GameStateSnapshot snapshot;

        boolean totemCalled = false;
        TotemPlacedDTO totemDTO;

        /**
         * Captures the game start event.
         *
         * @param snapshot the initial game state snapshot
         */
        @Override
        public void onGameStarted(GameStateSnapshot snapshot) {
            this.started = true;
            this.snapshot = snapshot;
        }

        /**
         * Captures totem placement events.
         *
         * @param dto the totem placement data
         */
        @Override
        public void onTotemPlaced(TotemPlacedDTO dto) {
            this.totemCalled = true;
            this.totemDTO = dto;
        }

        @Override public void onCardsTaken(CardsTakenDTO dto) {}
        @Override public void onExtraCardTaken(ExtraCardTakenDTO dto) {}
        @Override public void onEventResolved(EventResolvedDTO dto) {}
        @Override public void onRoundEnded(RoundEndedDTO dto) {}
        @Override public void onGameEnded(GameEndedDTO dto) {}
    }

    /**
     * Verifies initial game state is correctly set.
     */
    @Test
    void testInitialState() {
        Game game = createGame(3);
        assertEquals(GameState.InProgress, game.getState());
        assertFalse(game.isGameEnded());
    }

    /**
     * Verifies validateState throws when game is not in progress.
     */
    @Test
    void testValidateStateThrows() {
        Game game = createGame(3);
        game.setState(GameState.Finished);

        assertThrows(GameException.class, game::validateState);
    }

    /**
     * Verifies correct and incorrect player validation during totem placement.
     */
    @Test
    void testValidateActivePlayerTotemPlacement() {
        Game game = createGame(3);

        Player correct = game.getPlacementOrder().get(0);
        Player wrong = game.getPlacementOrder().get(1);

        game.setCurrentPlayerIndex(0);

        assertDoesNotThrow(() -> game.validateActivePlayerTotemPlacement(correct));
        assertThrows(GameException.class, () -> game.validateActivePlayerTotemPlacement(wrong));
    }

    /**
     * Verifies that placing a totem with an unknown player throws an exception.
     */
    @Test
    void testUnknownPlayerThrows() {
        Game game = createGame(3);
        char slot = getFreeSlot(game);

        assertThrows(GameException.class,
                () -> game.placeTotem("UNKNOWN", slot));
    }

    /**
     * Verifies full totem placement flow and DTO firing.
     */
    @Test
    void testPlaceTotemFlow() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);

        String current = game.getCurrentPlayerNickname();
        char slot = getFreeSlot(game);

        game.placeTotem(current, slot);

        assertTrue(listener.totemCalled);
        assertEquals(current, listener.totemDTO.placerNickname());
        assertEquals(slot, listener.totemDTO.slotID());
    }

    /**
     * Verifies turn order bonus is correctly applied
     * (either food gain or penalty).
     */
    @Test
    void testApplyTurnOrderBonus() {
        Game game = createGame(3);

        Player player = game.getPlayers().getFirst();

        int beforeFood = player.getFood();
        int beforePP = player.getPrestigePoints();

        int bonus = game.getBoard().getFoodBonus(player);

        game.applyTurnOrderBonus(player);

        if (bonus > 0) {
            assertEquals(beforeFood + bonus, player.getFood());
        } else if (bonus < 0) {
            assertTrue(
                    player.getFood() < beforeFood ||
                            player.getPrestigePoints() < beforePP
            );
        }
    }

    /**
     * Verifies that the turn advances to the next player after placement.
     */
    @Test
    void testTurnAdvances() {
        Game game = createGame(3);

        String first = game.getCurrentPlayerNickname();
        char slot = getFreeSlot(game);

        game.placeTotem(first, slot);

        String second = game.getCurrentPlayerNickname();

        assertNotEquals(first, second);
    }

    /**
     * Verifies that starting the game fires a snapshot event.
     */
    @Test
    void testStartGameFiresSnapshot() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);
        game.startGame();

        assertTrue(listener.started);
        assertNotNull(listener.snapshot);

        assertEquals(game.getCurrentRound(), listener.snapshot.currentRound());
        assertEquals(game.getPlayers().size(), listener.snapshot.players().size());
    }

    /**
     * Verifies that removing a listener prevents it from receiving events.
     */
    @Test
    void testRemoveListener() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);
        game.removeListener(listener);

        game.startGame();

        assertFalse(listener.started);
    }

    /**
     * Verifies manual firing of TotemPlaced event.
     */
    @Test
    void testFireTotemPlaced() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);

        TotemPlacedDTO dto = new TotemPlacedDTO(
                "P1",
                'B',
                "P2",
                "TotemPlacementPhase"
        );

        game.fireTotemPlaced(dto);

        assertTrue(listener.totemCalled);
        assertEquals("P1", listener.totemDTO.placerNickname());
    }
}
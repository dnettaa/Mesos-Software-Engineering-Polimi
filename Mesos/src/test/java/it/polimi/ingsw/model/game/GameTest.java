package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests core game state, active-player validation, and listener notifications in {@link Game}.
 */
class GameTest {

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
     * Captures game listener callbacks used by these tests.
     */
    static class TestListener implements GameListener {
        boolean started = false;
        GameStateSnapshot snapshot;
        boolean totemCalled = false;
        TotemPlacedDTO totemDTO;

        @Override
        public void onGameStarted(GameStateSnapshot snapshot) {
            this.started = true;
            this.snapshot = snapshot;
        }

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
     * Setup: a new three-player game is created through the setup service.
     * Action: inspect the initial state flags.
     * Expected behavior: the game starts in progress and is not ended.
     * Edge case: a newly created game must not be marked as finished before any phase action.
     */
    @Test
    void createGameShouldStartInProgress() {
        Game game = createGame(3);

        assertEquals(GameState.InProgress, game.getState());
        assertFalse(game.isGameEnded());
    }

    /**
     * Setup: a game state is manually changed to finished.
     * Action: validate the current game state.
     * Expected behavior: validation rejects actions outside an in-progress game.
     * Edge case: state validation does not depend on the current phase object.
     */
    @Test
    void validateStateShouldRejectFinishedGame() {
        Game game = createGame(3);
        game.setState(GameState.Finished);

        assertThrows(GameException.class, game::validateState);
    }

    /**
     * Setup: the current placement index points to the first player in placement order.
     * Action: validate both the expected player and another player.
     * Expected behavior: only the active placement player is accepted.
     * Edge case: validation compares player identity from placement order, not nickname strings supplied by the client.
     */
    @Test
    void validateActivePlayerTotemPlacementShouldAcceptOnlyCurrentPlacementPlayer() {
        Game game = createGame(3);
        Player correct = game.getPlacementOrder().getFirst();
        Player wrong = game.getPlacementOrder().get(1);

        game.setCurrentPlayerIndex(0);

        assertDoesNotThrow(() -> game.validateActivePlayerTotemPlacement(correct));
        assertThrows(GameException.class, () -> game.validateActivePlayerTotemPlacement(wrong));
    }

    /**
     * Setup: a player nickname that does not belong to the game is used.
     * Action: request to place a totem in a valid free slot.
     * Expected behavior: the game rejects the action with a game exception.
     * Edge case: slot validity must not hide the unknown-player error.
     */
    @Test
    void placeTotemShouldRejectUnknownPlayer() {
        Game game = createGame(3);
        char slot = getFreeSlot(game);

        assertThrows(GameException.class, () -> game.placeTotem("UNKNOWN", slot));
    }

    /**
     * Setup: a listener is registered and the current player has a valid free slot.
     * Action: the current player places a totem.
     * Expected behavior: the placement event is emitted with the acting player and slot.
     * Edge case: listener notification is verified through the public game action, not by calling the fire method directly.
     */
    @Test
    void placeTotemShouldEmitTotemPlacedDto() {
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
     * Setup: a player is on a turn-order position with a board-defined bonus or penalty.
     * Action: apply the turn-order effect.
     * Expected behavior: positive bonuses add food, while negative values spend food or prestige.
     * Edge case: the test accepts both penalty branches because the branch depends on the player's available food.
     */
    @Test
    void applyTurnOrderBonusShouldApplyBoardFoodBonusOrPenalty() {
        Game game = createGame(3);
        Player player = game.getPlayers().getFirst();
        int beforeFood = player.getFood();
        int beforePrestige = player.getPrestigePoints();
        int bonus = game.getBoard().getFoodBonus(player);

        game.applyTurnOrderBonus(player);

        if (bonus > 0) {
            assertEquals(beforeFood + bonus, player.getFood());
        } else if (bonus < 0) {
            assertTrue(player.getFood() < beforeFood || player.getPrestigePoints() < beforePrestige);
        } else {
            assertEquals(beforeFood, player.getFood());
            assertEquals(beforePrestige, player.getPrestigePoints());
        }
    }

    /**
     * Setup: the first current player has not yet placed a totem.
     * Action: the current player places a totem in a valid slot.
     * Expected behavior: the active player advances to the next placement player.
     * Edge case: the current-player nickname changes after exactly one placement in a multiplayer game.
     */
    @Test
    void placeTotemShouldAdvanceCurrentPlayer() {
        Game game = createGame(3);
        String first = game.getCurrentPlayerNickname();
        char slot = getFreeSlot(game);

        game.placeTotem(first, slot);

        assertNotEquals(first, game.getCurrentPlayerNickname());
    }

    /**
     * Setup: a listener is registered before starting the game.
     * Action: start the game.
     * Expected behavior: the listener receives a complete snapshot matching the game state.
     * Edge case: start notification uses the current state without mutating the player count or round.
     */
    @Test
    void startGameShouldEmitInitialSnapshot() {
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
     * Setup: a listener is added and then removed.
     * Action: start the game.
     * Expected behavior: the removed listener does not receive the start callback.
     * Edge case: listener removal is verified on a real callback path.
     */
    @Test
    void removeListenerShouldPreventFutureCallbacks() {
        Game game = createGame(3);
        TestListener listener = new TestListener();
        game.addListener(listener);
        game.removeListener(listener);

        game.startGame();

        assertFalse(listener.started);
    }

    /**
     * Setup: a listener is registered and a DTO is built manually.
     * Action: fire the totem-placed notification directly.
     * Expected behavior: the listener receives exactly the supplied DTO data.
     * Edge case: direct fire methods preserve DTO payloads without recomputing them.
     */
    @Test
    void fireTotemPlacedShouldForwardDtoToListeners() {
        Game game = createGame(3);
        TestListener listener = new TestListener();
        game.addListener(listener);
        TotemPlacedDTO dto = new TotemPlacedDTO("P1", 'B', "P2", "TotemPlacementPhase");

        game.fireTotemPlaced(dto);

        assertTrue(listener.totemCalled);
        assertEquals("P1", listener.totemDTO.placerNickname());
        assertEquals('B', listener.totemDTO.slotID());
    }
}

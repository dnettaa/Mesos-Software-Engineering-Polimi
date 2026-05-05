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

    private Game createGame(int n) {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < n; i++) {
            players.put("P" + i, colors[i]);
        }

        return setup.createNewGame(players, 1);
    }

    /**
     * Fake listener to capture events.
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

    @Test
    void testInitialState() {
        Game game = createGame(3);
        assertEquals(GameState.InProgress, game.getState());
    }

    @Test
    void testGameNotEndedInitially() {
        Game game = createGame(3);
        assertFalse(game.isGameEnded());
    }

    @Test
    void testValidateStateThrows() {
        Game game = createGame(3);
        game.setState(GameState.Finished);
        assertThrows(GameException.class, game::validateState);
    }

    @Test
    void testValidateActivePlayerTotemPlacement() {
        Game game = createGame(3);

        Player correct = game.getPlacementOrder().get(0);
        Player wrong = game.getPlacementOrder().get(1);

        game.setCurrentPlayerIndex(0);

        assertDoesNotThrow(() -> game.validateActivePlayerTotemPlacement(correct));
        assertThrows(GameException.class, () -> game.validateActivePlayerTotemPlacement(wrong));
    }

    @Test
    void testApplyTurnOrderBonusPositive() {
        Game game = createGame(3);

        Player player = game.getPlayers().getFirst();
        int before = player.getFood();

        game.applyTurnOrderBonus(player);

        assertTrue(player.getFood() >= before);
    }

    @Test
    void testApplyTurnOrderBonusNegative() {
        Game game = createGame(3);

        Player player = game.getPlayers().getFirst();
        player.spendFood(player.getFood());

        int beforePP = player.getPrestigePoints();

        game.applyTurnOrderBonus(player);

        assertTrue(player.getPrestigePoints() <= beforePP);
    }

    @Test
    void testFindPlayerUnknown() {
        Game game = createGame(3);

        assertThrows(GameException.class,
                () -> game.placeTotem("UNKNOWN", 'A'));
    }

    @Test
    void testStartGameFiresSnapshot() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);
        game.startGame();

        assertTrue(listener.started);
        assertNotNull(listener.snapshot);

        // record accessor
        assertEquals(game.getCurrentRound(), listener.snapshot.currentRound());
        assertEquals(game.getPlayers().size(), listener.snapshot.players().size());
    }

    @Test
    void testRemoveListener() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);
        game.removeListener(listener);

        game.startGame();

        assertFalse(listener.started);
    }

    @Test
    void testFireTotemPlaced() {
        Game game = createGame(3);
        TestListener listener = new TestListener();

        game.addListener(listener);

        TotemPlacedDTO dto = new TotemPlacedDTO(
                "P1",
                'A',
                "P2",
                "OfferResolutionPhase"
        );

        game.fireTotemPlaced(dto);

        assertTrue(listener.totemCalled);

        assertEquals("P1", listener.totemDTO.placerNickname());
        assertEquals('A', listener.totemDTO.slotID());
        assertEquals("P2", listener.totemDTO.nextPlayerNickname());
        assertEquals("OfferResolutionPhase", listener.totemDTO.nextPhaseName());
    }
}
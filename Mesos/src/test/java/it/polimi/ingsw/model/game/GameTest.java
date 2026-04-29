package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.exception.GameException;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Game}.
 * Verifies core logic independent from Phase behavior.
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
     * Verifies that the game starts in "InProgress" state.
     */
    @Test
    void testInitialState() {
        Game game = createGame(3);

        assertEquals(GameState.InProgress, game.getState());
    }

    /**
     * Verifies that the game is not ended at start.
     */
    @Test
    void testGameNotEndedInitially() {
        Game game = createGame(3);

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
     * Verifies active player validation for totem placement.
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
     * Verifies applyTurnOrderBonus gives food when positive.
     */
    @Test
    void testApplyTurnOrderBonusPositive() {
        Game game = createGame(3);

        Player player = game.getPlayers().getFirst();

        int before = player.getFood();

        game.applyTurnOrderBonus(player);

        assertTrue(player.getFood() >= before);
    }

    /**
     * Verifies applyTurnOrderBonus handles penalty.
     */
    @Test
    void testApplyTurnOrderBonusNegative() {
        Game game = createGame(3);

        Player player = game.getPlayers().getFirst();

        player.spendFood(player.getFood()); // portalo a 0

        int beforePP = player.getPrestigePoints();

        game.applyTurnOrderBonus(player);

        // può perdere PP se non ha cibo
        assertTrue(player.getPrestigePoints() <= beforePP);
    }

    /**
     * Verifies that buildGameStateMessage returns a valid object.
     */
    @Test
    void testBuildGameStateMessage() {
        Game game = createGame(3);

        var message = game.buildGameStateMessage();

        assertNotNull(message);
    }

    /**
     * Verifies that unknown player throws exception.
     */
    @Test
    void testFindPlayerUnknown() {
        Game game = createGame(3);

        assertThrows(GameException.class,
                () -> game.placeTotem("UNKNOWN", 'A'));
    }
}
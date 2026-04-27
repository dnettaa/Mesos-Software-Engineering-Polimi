package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.board.*;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link GameSetupService}.
 * This class verifies the correct initialization of a {@link Game}
 * and all its core components, including players, board, and initial
 * visible card rows.
 *
 * @author Andrea Markvukaj
 */
class GameSetupServiceTest {

    private final GameSetupService service = new GameSetupService();

    /**
     * Utility method to generate a player selection map.
     */
    private Map<String, TotemColor> createPlayers(int n) {
        Map<String, TotemColor> map = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < n; i++) {
            map.put("P" + i, colors[i]);
        }

        return map;
    }

    /**
     * Verifies that a new Game instance is created.
     */
    @Test
    void testCreateNewGame_notNull() {
        Game game = service.createNewGame(createPlayers(3), 1);
        assertNotNull(game);
    }

    /**
     * Verifies that all players are correctly initialized with a tribe.
     */
    @Test
    void testPlayersInitialization() {
        Game game = service.createNewGame(createPlayers(3), 1);

        assertEquals(3, game.getPlayers().size());

        for (Player p : game.getPlayers()) {
            assertNotNull(p.getTribe());
        }
    }

    /**
     * Verifies exact initial food distribution according to rules.
     * Sorted check avoids dependency on shuffle order.
     */
    @Test
    void testInitialFoodDistribution() {
        Game game = service.createNewGame(createPlayers(5), 1);

        List<Integer> foods = game.getPlayers()
                .stream()
                .map(Player::getFood)
                .sorted()
                .toList();

        assertEquals(List.of(2, 3, 3, 4, 4), foods);
    }

    /**
     * Verifies that the Board is properly initialized.
     */
    @Test
    void testBoardInitialization() {
        Board board = service.createNewGame(createPlayers(3), 1).getBoard();

        assertNotNull(board);
        assertNotNull(board.getTurnOrderTrack());
        assertNotNull(board.getPlacementOrder());
    }

    /**
     * Verifies that the lower row contains exactly (numPlayers + 1) cards.
     */
    @Test
    void testLowerRowSize() {
        int n = 3;
        Board board = service.createNewGame(createPlayers(n), 1).getBoard();

        assertEquals(n + 1, board.getLowerRowCards().size());
    }

    /**
     * Verifies that the upper row contains exactly (numPlayers + 4) tribe cards.
     */
    @Test
    void testUpperRowTribeCardCount() {
        int n = 3;
        Board board = service.createNewGame(createPlayers(n), 1).getBoard();

        long tribeCards = board.getUpperRowCards().stream()
                .filter(c -> c instanceof TribeCard)
                .count();

        assertEquals(n + 4, tribeCards);
    }

    /**
     * Verifies that building cards are present in the upper row.
     */
    @Test
    void testBuildingsAddedToUpperRow() {
        Board board = service.createNewGame(createPlayers(3), 1).getBoard();

        boolean hasBuilding = board.getUpperRowCards().stream()
                .anyMatch(c -> c instanceof BuildingCard);

        assertTrue(hasBuilding);
    }

    /**
     * Verifies that no event cards are placed in the lower row.
     */
    @Test
    void testNoEventsInLowerRow() {
        Board board = service.createNewGame(createPlayers(3), 1).getBoard();

        assertTrue(board.getLowerRowEvents().isEmpty());
    }

    /**
     * Verifies that accessing upper row events does not return null.
     */
    @Test
    void testUpperRowEventsNotNull() {
        Board board = service.createNewGame(createPlayers(3), 1).getBoard();

        assertNotNull(board.getUpperRowEvents());
    }

    /**
     * Verifies that the turn order track contains all players.
     */
    @Test
    void testTurnOrderTrackPlayers() {
        int n = 4;
        Board board = service.createNewGame(createPlayers(n), 1).getBoard();

        assertEquals(n, board.getPlacementOrder().size());
    }

    /**
     * Verifies that the tribe deck is not empty after setup.
     */
    @Test
    void testTribeDeckNotEmpty() {
        Board board = service.createNewGame(createPlayers(3), 1).getBoard();

        assertTrue(board.getTribeDeckRemaining() > 0);
    }
}
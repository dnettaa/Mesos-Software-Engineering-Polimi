package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests game creation and initial board setup through {@link GameSetupService}.
 */
class GameSetupServiceTest {

    private final GameSetupService service = new GameSetupService();

    private Map<String, TotemColor> createPlayers(int playerCount) {
        Map<String, TotemColor> players = new LinkedHashMap<>();
        TotemColor[] colors = TotemColor.values();

        for (int i = 0; i < playerCount; i++) {
            players.put("P" + i, colors[i]);
        }

        return players;
    }

    /**
     * Setup: three nicknames and colors are provided to the setup service.
     * Action: create a new game.
     * Expected behavior: the game and all player tribes are initialized.
     * Edge case: setup must not leave players with null tribe references.
     */
    @Test
    void createNewGameShouldInitializePlayersAndTribes() {
        Game game = service.createNewGame(createPlayers(3), 1);

        assertNotNull(game);
        assertEquals(3, game.getPlayers().size());
        for (Player player : game.getPlayers()) {
            assertNotNull(player.getTribe());
        }
    }

    /**
     * Setup: a five-player game is created.
     * Action: read and sort all initial food values.
     * Expected behavior: the configured starting food distribution is applied.
     * Edge case: sorting removes dependency on randomized player order.
     */
    @Test
    void createNewGameShouldApplyInitialFoodDistribution() {
        Game game = service.createNewGame(createPlayers(5), 1);

        List<Integer> foods = game.getPlayers()
                .stream()
                .map(Player::getFood)
                .sorted()
                .toList();

        assertEquals(List.of(2, 3, 3, 4, 4), foods);
    }

    /**
     * Setup: a three-player game is created.
     * Action: inspect the board and turn-order structures.
     * Expected behavior: board, turn order track, and placement order are available.
     * Edge case: the placement order must contain every player exactly once.
     */
    @Test
    void createNewGameShouldInitializeBoardAndTurnOrderStructures() {
        Game game = service.createNewGame(createPlayers(3), 1);
        Board board = game.getBoard();

        assertNotNull(board);
        assertNotNull(board.getTurnOrderTrack());
        assertNotNull(board.getPlacementOrder());
        assertEquals(game.getPlayers().size(), board.getPlacementOrder().size());
    }

    /**
     * Setup: a three-player game is created from fresh decks.
     * Action: inspect lower-row and upper-row visible cards.
     * Expected behavior: the lower row has player count plus one cards, and the upper row has player count plus four tribe cards.
     * Edge case: building cards are also present in the upper row without reducing the tribe-card quota.
     */
    @Test
    void createNewGameShouldPopulateVisibleRowsWithExpectedCardCounts() {
        int playerCount = 3;
        Board board = service.createNewGame(createPlayers(playerCount), 1).getBoard();

        long upperTribeCards = board.getUpperRowCards().stream()
                .filter(card -> card instanceof TribeCard)
                .count();
        boolean hasUpperBuilding = board.getUpperRowCards().stream()
                .anyMatch(card -> card instanceof BuildingCard);

        assertEquals(playerCount + 1, board.getLowerRowCards().size());
        assertEquals(playerCount + 4, upperTribeCards);
        assertTrue(hasUpperBuilding);
    }

    /**
     * Setup: a new game is created.
     * Action: inspect event rows and the tribe deck after initial setup.
     * Expected behavior: no event is in the lower row, upper events are accessible, and the tribe deck still has cards.
     * Edge case: setup should not consume or expose event collections incorrectly.
     */
    @Test
    void createNewGameShouldExposeConsistentEventRowsAndRemainingDeck() {
        Board board = service.createNewGame(createPlayers(3), 1).getBoard();

        assertTrue(board.getLowerRowEvents().isEmpty());
        assertNotNull(board.getUpperRowEvents());
        assertTrue(board.getTribeDeckRemaining() > 0);
        assertFalse(board.getUpperRowCards().isEmpty());
    }
}

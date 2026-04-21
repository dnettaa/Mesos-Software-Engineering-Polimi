package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.card.building.EndBonusCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Board class.
 * Verifies complex interactions between tracks, rows, and decks, focusing on round setup.
 */
class BoardTest {

    private Board board;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player("P1", null, new Tribe(), 0, 0);

        // 1. Setup OfferTrack
        List<OfferSlot> slots = new ArrayList<>();
        // Using the correct OfferSlot constructor
        slots.add(new OfferSlot('A', 1, 0, 0));
        OfferTrack offerTrack = new OfferTrack(slots);

        // 2. Setup TurnOrderTrack
        TurnOrderTrack turnTrack = new TurnOrderTrack(new ArrayList<>(), new int[]{2, 1}, 2);

        // 3. Setup TribeDeck (with 3 cards to allow drawing during refill)
        ArrayDeque<TribeCard> tribeCards = new ArrayDeque<>();
        tribeCards.addLast(new HunterCard(Era.Era1, "H1", false));
        tribeCards.addLast(new HunterCard(Era.Era1, "H2", false));
        tribeCards.addLast(new HunterCard(Era.Era1, "H3", false));
        TribeDeck tribeDeck = new TribeDeck(tribeCards, Era.Era1);

        // 4. Setup BuildingDeck
        List<BuildingCard> bEra1 = new ArrayList<>();
        bEra1.add(new EndBonusCard(Era.Era1, "B1", 0, 0, 0));
        BuildingDeck buildingDeck = new BuildingDeck(bEra1, new ArrayList<>(), new ArrayList<>());

        // 5. Setup CardRows
        CardRow upperRow = new CardRow(new ArrayList<>(), new ArrayList<>());
        CardRow lowerRow = new CardRow(new ArrayList<>(), new ArrayList<>());

        // Let's manually place 1 tribe card in the upper row BEFORE setup
        upperRow.addTribeCard(new HunterCard(Era.Era1, "H_OLD", false));

        // Assemble the Board
        board = new Board(offerTrack, turnTrack, tribeDeck, buildingDeck, upperRow, lowerRow, Era.Era1);
    }

    /**
     * Verifies that setting up a new round correctly resets tracks and moves/refills cards.
     */
    @Test
    void testSetupNewRound() {
        // Pre-condition: place player on the offer track and turn track to verify reset
        board.placeTotemOnOffer(testPlayer, 'A');
        board.getTurnOrderTrack().placeFirstSlot(testPlayer);

        assertFalse(board.getOfferTrack().isSlotFree('A'));
        assertEquals(1, board.getPlacementOrder().size());

        // Execute the round setup
        board.setupNewRound();

        // 1. Tracks should be cleared/reset
        assertTrue(board.getOfferTrack().isSlotFree('A'));
        assertEquals(0, board.getPlacementOrder().size());

        // 2. The old upper row card ("H_OLD") should have been moved to the lower row
        assertEquals(1, board.getLowerRowCards().size());
        assertEquals("H_OLD", board.getLowerRowCards().getFirst().getId());

        // 3. Upper row should have been refilled from the TribeDeck and BuildingDeck
        // Target tribe cards = NumPlayers(2) + 4 = 6.
        // We put 3 cards in the deck, so it should draw all 3.
        assertEquals(3, board.getUpperRowCards().stream().filter(c -> c instanceof TribeCard).count());

        // Target buildings = revealed from Era 1 (1 building)
        assertEquals(1, board.getUpperRowCards().stream().filter(c -> c instanceof BuildingCard).count());
    }

    /**
     * Verifies simple delegating methods that interact directly with other components.
     */
    @Test
    void testDelegatingMethods() {
        // Test getActionFor
        board.placeTotemOnOffer(testPlayer, 'A');
        assertArrayEquals(new int[]{1, 0}, board.getActionFor(testPlayer));

        // Test returnTotemToTurnOrder
        board.returnTotemToTurnOrder(testPlayer);
        assertEquals(1, board.getPlacementOrder().size());

        // Chiamiamo setupNewRound() PRIMA delle rimozioni.
        // Questo sposta "H_OLD" nella riga inferiore e riempie la riga superiore con nuove carte.
        board.setupNewRound();

        // Ora la riga inferiore contiene "H_OLD". Testiamo removeCardFromLower.
        Card lowerCard = board.getLowerRowCards().getFirst();
        board.removeCardFromLower(lowerCard);
        assertEquals(0, board.getLowerRowCards().stream().filter(c -> c.equals(lowerCard)).count());

        // Ora la riga superiore contiene le carte appena pescate. Testiamo removeCardFromUpper.
        Card upperCard = board.getUpperRowCards().getFirst();
        board.removeCardFromUpper(upperCard);
        assertEquals(0, board.getUpperRowCards().stream().filter(c -> c.equals(upperCard)).count());
    }

    /**
     * Verifies the retrieval of event cards from upper and lower rows.
     */
    @Test
    void testEventGetters() {
        CardRow up = new CardRow(new ArrayList<>(), new ArrayList<>());
        CardRow down = new CardRow(new ArrayList<>(), new ArrayList<>());

        up.addTribeCard(new it.polimi.ingsw.model.card.HuntEventCard(Era.Era1, "E1", false, 1));
        down.addTribeCard(new it.polimi.ingsw.model.card.HuntEventCard(Era.Era1, "E2", false, 1));

        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()),
                new TurnOrderTrack(new ArrayList<>(), new int[]{0}, 1),
                new TribeDeck(new ArrayDeque<>(), Era.Era1),
                new BuildingDeck(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                up, down, Era.Era1
        );

        assertEquals(1, customBoard.getUpperRowEvents().size());
        assertEquals("E1", customBoard.getUpperRowEvents().getFirst().getId());

        assertEquals(1, customBoard.getLowerRowEvents().size());
        assertEquals("E2", customBoard.getLowerRowEvents().getFirst().getId());
    }

    /**
     * Verifies the food bonus calculation, especially the -1 penalty for the last position.
     */
    @Test
    void testGetFoodBonus() {
        Player p1 = new Player("P1", null, new Tribe(), 0, 0);
        Player p2 = new Player("P2", null, new Tribe(), 0, 0);

        // 2 players: 1st gets 5 food, 2nd (last) gets penalized
        TurnOrderTrack track = new TurnOrderTrack(new ArrayList<>(), new int[]{5, 3}, 2);
        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()), track,
                new TribeDeck(new ArrayDeque<>(), Era.Era1),
                new BuildingDeck(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()), Era.Era1
        );

        customBoard.returnTotemToTurnOrder(p1);
        customBoard.returnTotemToTurnOrder(p2);

        // p1 is first (index 0) -> not last, gets bonus 5
        assertEquals(5, customBoard.getFoodBonus(p1));

        // p2 is second (index 1) -> last, gets -1 penalty
        assertEquals(-1, customBoard.getFoodBonus(p2));
    }

    /**
     * Verifies the unused private method validateRowBelongsToBoard via Reflection.
     */
    @Test
    void testValidateRowBelongsToBoard() throws Exception {
        java.lang.reflect.Method method = Board.class.getDeclaredMethod("validateRowBelongsToBoard", CardRow.class);
        method.setAccessible(true);

        // Test valid row: Retrieve upperRow via reflection to pass a valid reference
        java.lang.reflect.Field upperRowField = Board.class.getDeclaredField("upperRow");
        upperRowField.setAccessible(true);
        CardRow validRow = (CardRow) upperRowField.get(board);

        // Should not throw any exception
        assertDoesNotThrow(() -> method.invoke(board, validRow));

        // Test invalid row: create a completely new row
        CardRow invalidRow = new CardRow(new ArrayList<>(), new ArrayList<>());

        java.lang.reflect.InvocationTargetException ex = assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(board, invalidRow)
        );
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    /**
     * Verifies that the board transitions to a new Era if the deck provides it during setup.
     */
    @Test
    void testCheckEraTransition() {
        List<BuildingCard> era2Buildings = new ArrayList<>();
        era2Buildings.add(new EndBonusCard(Era.Era2, "B2", 0, 0, 0));
        BuildingDeck bDeck = new BuildingDeck(new ArrayList<>(), era2Buildings, new ArrayList<>());

        // Deck is at Era 2, Board is at Era 1
        TribeDeck tDeck = new TribeDeck(new ArrayDeque<>(), Era.Era2);

        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()), new TurnOrderTrack(new ArrayList<>(), new int[]{0}, 1),
                tDeck, bDeck,
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()), Era.Era1
        );

        customBoard.setupNewRound();

        // If era transitioned to Era 2, the Era 2 building should now be in the upper row
        assertEquals(1, customBoard.getUpperRowCards().size());
        assertEquals("B2", customBoard.getUpperRowCards().getFirst().getId());
    }

    /**
     * Verifies the loop termination in refillUpperRow when there are plenty of cards in the deck.
     */
    @Test
    void testRefillUpperRowWithSufficientCards() {
        ArrayDeque<TribeCard> deckCards = new ArrayDeque<>();
        for(int i = 0; i < 10; i++) {
            deckCards.add(new HunterCard(Era.Era1, "H" + i, false));
        }
        TribeDeck abundantDeck = new TribeDeck(deckCards, Era.Era1);

        // 1 player game
        TurnOrderTrack turnTrack = new TurnOrderTrack(new ArrayList<>(), new int[]{0}, 1);

        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()), turnTrack, abundantDeck,
                new BuildingDeck(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()), Era.Era1
        );

        customBoard.setupNewRound();

        // NumPlayers is 1. Target = 1 + 4 = 5.
        // Upper row should have exactly 5 cards. Deck should have 5 left (loop stopped by i < cardsToDraw)
        assertEquals(5, customBoard.getUpperRowCards().size());
        assertFalse(abundantDeck.isEmpty());
    }

    /**
     * Verifies that the board correctly retrieves the offer resolution order from the track.
     */
    @Test
    void testGetOfferResolutionOrder() {
        // Initially, no players are placed, so the order should be empty
        assertTrue(board.getOfferResolutionOrder().isEmpty());

        // Place a player on slot 'A'
        board.placeTotemOnOffer(testPlayer, 'A');

        // Verify the resolution order updates accordingly
        java.util.List<OfferSlot> order = board.getOfferResolutionOrder();
        assertEquals(1, order.size());
        assertEquals('A', order.getFirst().getSlotID());
    }
}
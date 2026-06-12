package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.TribeCard;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.card.building.EndBonusCard;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
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
 *
 * @author Diana
 */
class BoardTest {

    private Board board;
    private Player testPlayer;

    @BeforeEach
    void setUp() {

        testPlayer = new Player("P1", TotemColor.RED, new Tribe(), 0, 0);

        List<OfferSlot> slots = new ArrayList<>();
        slots.add(new OfferSlot('A', 1, 0, 0));
        OfferTrack offerTrack = new OfferTrack(slots);

        TurnOrderTrack turnTrack = new TurnOrderTrack(new ArrayList<>(), new int[]{2, 1}, 2);

        ArrayDeque<TribeCard> tribeCards = new ArrayDeque<>();
        tribeCards.addLast(new HunterCard(Era.Era1, "H1", false));
        tribeCards.addLast(new HunterCard(Era.Era1, "H2", false));
        tribeCards.addLast(new HunterCard(Era.Era1, "H3", false));
        TribeDeck tribeDeck = new TribeDeck(tribeCards, Era.Era1);

        List<BuildingCard> bEra1 = new ArrayList<>();
        bEra1.add(new EndBonusCard(Era.Era1, "B1", 0, 0, 0));
        BuildingDeck buildingDeck = new BuildingDeck(bEra1, new ArrayList<>(), new ArrayList<>());

        CardRow upperRow = new CardRow(new ArrayList<>(), new ArrayList<>());
        CardRow lowerRow = new CardRow(new ArrayList<>(), new ArrayList<>());

        upperRow.addTribeCard(new HunterCard(Era.Era1, "H_OLD", false));

        board = new Board(offerTrack, turnTrack, tribeDeck, buildingDeck, upperRow, lowerRow, Era.Era1);
    }

    /**
     * Verifies that setting up a new round correctly resets tracks and moves/refills cards.
     * Setup: a board with a placed totem, an existing upper-row tribe card, and a non-empty tribe deck.
     * Action: set up round 2.
     * Expected behavior: offer slots are reset, the old upper card moves to lower row, and DTO data matches the board.
     * Edge case covered: round setup must keep board state and notification DTOs consistent.
     */
    @Test
    void setupNewRoundShouldResetTrackMoveCardsAndReturnConsistentDto() {
        board.placeTotemOnOffer(testPlayer, 'A');
        board.getTurnOrderTrack().placeFirstSlot(testPlayer);

        RoundEndedDTO dto = board.setupNewRound(
                2,
                List.of("P1"),
                "P1"
        );

        assertDoesNotThrow(() -> board.placeTotemOnOffer(testPlayer, 'A'));

        assertEquals("H_OLD", board.getLowerRowCards().getFirst().getId());

        assertNotNull(dto);

        assertEquals(2, dto.newRound());
        assertEquals(board.getCurrentEra(), dto.newEra());

        assertEquals(List.of("P1"), dto.newTurnOrder());
        assertEquals("P1", dto.firstPlayerNickname());

        assertEquals(
                board.getUpperRowCards().stream().map(Card::getId).toList(),
                dto.newUpperRowIDs()
        );

        assertTrue(dto.movedUpperToLowerTribeIDs().contains("H_OLD"));

        assertEquals(board.getTribeDeckRemaining(), dto.tribeDeckRemaining());

        assertNotNull(dto.discardedLowerTribeIDs());
        assertNotNull(dto.discardedLowerEventIDs());
        assertNotNull(dto.movedUpperToLowerTribeIDs());
        assertNotNull(dto.discardedLowerBuildingIDs());
        assertNotNull(dto.movedUpperToLowerBuildingIDs());
        assertNotNull(dto.revealedBuildingIDs());
    }

    /**
     * Verifies simple delegating methods that interact directly with other components.
     * Setup: a board with one offer slot and at least one upper-row card.
     * Action: place a totem, return it to turn order, and remove an upper card.
     * Expected behavior: each board method delegates to the matching component.
     * Edge case covered: board-level facade methods preserve component state changes.
     */
    @Test
    void boardDelegatingMethodsShouldUpdateUnderlyingComponents() {
        board.placeTotemOnOffer(testPlayer, 'A');
        assertArrayEquals(new int[]{1, 0}, board.getActionFor(testPlayer));

        board.returnTotemToTurnOrder(testPlayer);
        assertEquals(1, board.getPlacementOrder().size());

        if (!board.getLowerRowCards().isEmpty()) {
            Card lowerCard = board.getLowerRowCards().getFirst();
            board.removeCardFromLower(lowerCard);
            assertFalse(board.getLowerRowCards().contains(lowerCard));
        }

        assertFalse(board.getUpperRowCards().isEmpty());
        Card upperCard = board.getUpperRowCards().getFirst();
        board.removeCardFromUpper(upperCard);
        assertFalse(board.getUpperRowCards().contains(upperCard));
    }

    /**
     * Verifies the retrieval of event cards from upper and lower rows.
     * Setup: custom upper and lower rows each contain one event card.
     * Action: request upper-row and lower-row events through the board.
     * Expected behavior: each getter returns the event from the corresponding row.
     * Edge case covered: event resolution can collect events from both rows when needed.
     */
    @Test
    void eventGettersShouldReturnEventsFromEachRow() {
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

        assertEquals("E1", customBoard.getUpperRowEvents().getFirst().getId());
        assertEquals("E2", customBoard.getLowerRowEvents().getFirst().getId());
    }

    /**
     * Verifies the food bonus calculation, especially the -1 penalty for the last position.
     * Setup: two players are returned to a two-position turn-order track.
     * Action: read food bonuses for both players.
     * Expected behavior: the first player receives configured food and the last receives the penalty marker.
     * Edge case covered: the last position is represented by -1 for penalty handling.
     */
    @Test
    void getFoodBonusShouldReturnConfiguredBonusOrLastPositionPenalty() {
        Player p1 = new Player("P1", null, new Tribe(), 0, 0);
        Player p2 = new Player("P2", null, new Tribe(), 0, 0);

        TurnOrderTrack track = new TurnOrderTrack(new ArrayList<>(), new int[]{5, 3}, 2);

        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()), track,
                new TribeDeck(new ArrayDeque<>(), Era.Era1),
                new BuildingDeck(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                Era.Era1
        );

        customBoard.returnTotemToTurnOrder(p1);
        customBoard.returnTotemToTurnOrder(p2);

        assertEquals(5, customBoard.getFoodBonus(p1));
        assertEquals(-1, customBoard.getFoodBonus(p2));
    }

    /**
     * Verifies that the board transitions to a new Era if the deck provides it during setup.
     * Setup: board current era is Era 1 while tribe deck has advanced to Era 2.
     * Action: set up the next round.
     * Expected behavior: the board and DTO report Era 2 and reveal Era 2 buildings.
     * Edge case covered: era transition triggers building reveal for the new era.
     */
    @Test
    void setupNewRoundShouldUpdateEraAndRevealBuildingsWhenDeckAdvances() {
        List<BuildingCard> era2Buildings = new ArrayList<>();
        era2Buildings.add(new EndBonusCard(Era.Era2, "B2", 0, 0, 0));

        BuildingDeck bDeck = new BuildingDeck(new ArrayList<>(), era2Buildings, new ArrayList<>());
        TribeDeck tDeck = new TribeDeck(new ArrayDeque<>(), Era.Era2);

        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()),
                new TurnOrderTrack(new ArrayList<>(), new int[]{0}, 1),
                tDeck, bDeck,
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                Era.Era1
        );

        RoundEndedDTO dto = customBoard.setupNewRound(2, List.of("P1"), "P1");

        assertEquals(Era.Era2, dto.newEra());

        assertFalse(dto.revealedBuildingIDs().isEmpty());
        assertEquals("B2", dto.revealedBuildingIDs().getFirst());
    }

    /**
     * Verifies the loop termination in refillUpperRow when there are plenty of cards in the deck.
     * Setup: an empty upper row and a tribe deck with more cards than needed.
     * Action: set up a new round for one player.
     * Expected behavior: the upper row is refilled to five tribe cards and the deck still has cards.
     * Edge case covered: refill stops at the target row size without exhausting an abundant deck.
     */
    @Test
    void setupNewRoundShouldRefillUpperRowOnlyToTargetSize() {
        ArrayDeque<TribeCard> deckCards = new ArrayDeque<>();
        for (int i = 0; i < 10; i++) {
            deckCards.add(new HunterCard(Era.Era1, "H" + i, false));
        }

        TribeDeck abundantDeck = new TribeDeck(deckCards, Era.Era1);

        Board customBoard = new Board(
                new OfferTrack(new ArrayList<>()),
                new TurnOrderTrack(new ArrayList<>(), new int[]{0}, 1),
                abundantDeck,
                new BuildingDeck(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                new CardRow(new ArrayList<>(), new ArrayList<>()),
                Era.Era1
        );

        customBoard.setupNewRound(2, List.of("P1"), "P1");

        assertEquals(5, customBoard.getUpperRowCards().size());
        assertFalse(abundantDeck.isEmpty());
    }

    /**
     * Verifies that the board correctly retrieves the offer resolution order from the track.
     * Setup: a board with one offer slot.
     * Action: request resolution order before and after placing a totem.
     * Expected behavior: order is initially empty and then contains the occupied slot.
     * Edge case covered: resolution order reflects current offer-track occupancy.
     */
    @Test
    void getOfferResolutionOrderShouldReflectOccupiedOfferSlots() {
        assertTrue(board.getOfferResolutionOrder().isEmpty());

        board.placeTotemOnOffer(testPlayer, 'A');

        List<OfferSlot> order = board.getOfferResolutionOrder();

        assertEquals(1, order.size());
        assertEquals('A', order.getFirst().getSlotID());
    }
}

package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the OfferTrack class.
 * Verifies player placement, slot availability, and action retrieval.
 */
class OfferTrackTest {

    private OfferTrack offerTrack;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = new Player("P1", null, new Tribe(), 0, 0);
        player2 = new Player("P2", null, new Tribe(), 0, 0);

        List<OfferSlot> slots = new ArrayList<>();
        slots.add(new OfferSlot('A', 1, 0, 2));
        slots.add(new OfferSlot('B', 0, 1, 0));

        offerTrack = new OfferTrack(slots);
    }

    /**
     * Verifies that a player is correctly placed in a slot.
     */
    @Test
    void testPlacePlayer() {
        offerTrack.placePlayer(player1, 'A');

        assertEquals(player1, offerTrack.getSlot('A').getOccupant());
    }

    /**
     * Verifies that placing on an occupied slot throws.
     */
    @Test
    void testPlaceOnOccupiedSlotThrows() {
        offerTrack.placePlayer(player1, 'A');

        assertThrows(GameException.class,
                () -> offerTrack.placePlayer(player2, 'A'));
    }

    /**
     * Verifies that resolution order returns only occupied slots in correct order.
     */
    @Test
    void testGetResolutionOrder() {
        offerTrack.placePlayer(player2, 'B');
        offerTrack.placePlayer(player1, 'A');

        List<OfferSlot> order = offerTrack.getResolutionOrder();

        assertEquals(2, order.size());
        assertEquals('A', order.get(0).getSlotID());
        assertEquals('B', order.get(1).getSlotID());
    }

    /**
     * Verifies action retrieval for a placed player.
     */
    @Test
    void testGetActionFor() {
        offerTrack.placePlayer(player1, 'A');

        int[] action = offerTrack.getActionFor(player1);

        assertEquals(1, action[0]);
        assertEquals(0, action[1]);
    }

    /**
     * Verifies exception when player is not on any slot.
     */
    @Test
    void testGetActionForUnplacedPlayerThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> offerTrack.getActionFor(player2));
    }

    /**
     * Verifies reset frees all slots.
     */
    @Test
    void testReset() {
        offerTrack.placePlayer(player1, 'A');
        offerTrack.placePlayer(player2, 'B');

        offerTrack.reset();

        assertNull(offerTrack.getSlot('A').getOccupant());
        assertNull(offerTrack.getSlot('B').getOccupant());
    }

    /**
     * Verifies getSlot returns correct slot.
     */
    @Test
    void testGetSlot() {
        OfferSlot slot = offerTrack.getSlot('A');

        assertEquals('A', slot.getSlotID());
    }

    /**
     * Verifies exception when slot does not exist.
     */
    @Test
    void testGetSlotException() {
        assertThrows(GameException.class,
                () -> offerTrack.getSlot('Z'));
    }

    /**
     * Verifies that buildOfferSlotsData correctly maps slot state into DTOs.
     */
    @Test
    void testBuildOfferSlotsData() {
        offerTrack.placePlayer(player1, 'A');

        var dtoList = offerTrack.buildOfferSlotsData();

        assertEquals(2, dtoList.size());

        // DTO slot A (occupato)
        var slotA = dtoList.stream()
                .filter(s -> s.slotID() == 'A')
                .findFirst()
                .orElseThrow();

        assertEquals(1, slotA.upSel());
        assertEquals(0, slotA.downSel());
        assertEquals(2, slotA.foodReward());
        assertEquals("P1", slotA.occupantNickname());

        // DTO slot B (libero)
        var slotB = dtoList.stream()
                .filter(s -> s.slotID() == 'B')
                .findFirst()
                .orElseThrow();

        assertEquals(0, slotB.upSel());
        assertEquals(1, slotB.downSel());
        assertEquals(0, slotB.foodReward());
        assertNull(slotB.occupantNickname());
    }
}
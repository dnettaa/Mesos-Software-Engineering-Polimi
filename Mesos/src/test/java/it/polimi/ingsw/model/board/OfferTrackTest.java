package it.polimi.ingsw.model.board;

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

        // Using the correct OfferSlot constructor: (slotID, upSel, downSel, foodReward)
        slots.add(new OfferSlot('A', 1, 0, 2));
        slots.add(new OfferSlot('B', 0, 1, 0));

        offerTrack = new OfferTrack(slots); //
    }

    /**
     * Verifies that a player is correctly placed in a slot and the slot is no longer free.
     */
    @Test
    void testPlacePlayerAndSlotAvailability() {
        assertTrue(offerTrack.isSlotFree('A'));

        offerTrack.placePlayer(player1, 'A');

        assertFalse(offerTrack.isSlotFree('A'));
        assertTrue(offerTrack.isSlotFree('B'));

        // 'A' is occupied, so there should be only 1 free slot left ('B')
        assertEquals(1, offerTrack.getFreeSlots().size());
        assertEquals('B', offerTrack.getFreeSlots().getFirst().getSlotID());
    }

    /**
     * Verifies that the resolution order returns only occupied slots in the correct order.
     */
    @Test
    void testGetResolutionOrder() {
        offerTrack.placePlayer(player2, 'B');
        offerTrack.placePlayer(player1, 'A');

        List<OfferSlot> resolutionOrder = offerTrack.getResolutionOrder();

        // Should return slots that are occupied, in track order (A then B)
        assertEquals(2, resolutionOrder.size());
        assertEquals('A', resolutionOrder.get(0).getSlotID());
        assertEquals('B', resolutionOrder.get(1).getSlotID());
    }

    /**
     * Verifies that the correct action values (upper and lower selections) are retrieved for a placed player.
     */
    @Test
    void testGetActionFor() {
        offerTrack.placePlayer(player1, 'A');

        int[] action = offerTrack.getActionFor(player1);

        // Slot 'A' gives 1 upper selection and 0 lower selections
        assertEquals(1, action[0]);
        assertEquals(0, action[1]);

        // Unplaced player should throw an exception
        assertThrows(IllegalArgumentException.class, () -> offerTrack.getActionFor(player2));
    }

    /**
     * Verifies that resetting the track frees all slots.
     */
    @Test
    void testReset() {
        offerTrack.placePlayer(player1, 'A');
        offerTrack.placePlayer(player2, 'B');

        offerTrack.reset();

        // All slots should be free again
        assertTrue(offerTrack.isSlotFree('A'));
        assertTrue(offerTrack.isSlotFree('B'));
        assertEquals(2, offerTrack.getFreeSlots().size());
    }
    /**
     * Verifies that getSlots returns the correct list of all slots.
     */
    @Test
    void testGetSlots() {
        List<OfferSlot> allSlots = offerTrack.getSlots();

        // We expect exactly 2 slots as initialized in setUp
        assertEquals(2, allSlots.size());
        assertEquals('A', allSlots.get(0).getSlotID());
        assertEquals('B', allSlots.get(1).getSlotID());
    }

    /**
     * Verifies that getSlot throws an exception when requesting a non-existent slot ID.
     */
    @Test
    void testGetSlotException() {
        // Attempting to get a slot that doesn't exist (e.g., 'Z') should throw an exception
        assertThrows(IllegalArgumentException.class, () -> offerTrack.getSlot('Z'));

        // This also covers the exception path for placePlayer and isSlotFree,
        // since they both rely on getSlot internally
        assertThrows(IllegalArgumentException.class, () -> offerTrack.placePlayer(player1, 'Z'));
        assertThrows(IllegalArgumentException.class, () -> offerTrack.isSlotFree('Z'));
    }
}
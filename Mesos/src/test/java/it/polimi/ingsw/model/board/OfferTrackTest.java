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
 *
 * @author Diana
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
     * Setup: a track with two free slots.
     * Action: place P1 on slot A.
     * Expected behavior: slot A stores P1 as its occupant.
     * Edge case covered: placement delegates to the correct slot by identifier.
     */
    @Test
    void placePlayerShouldOccupyRequestedSlot() {
        offerTrack.placePlayer(player1, 'A');

        assertEquals(player1, offerTrack.getSlot('A').getOccupant());
    }

    /**
     * Verifies that placing on an occupied slot throws.
     * Setup: P1 already occupies slot A.
     * Action: P2 attempts to occupy the same slot.
     * Expected behavior: a {@link GameException} is thrown.
     * Edge case covered: offer slots enforce exclusive occupancy through the track API.
     */
    @Test
    void placePlayerShouldThrowWhenSlotIsOccupied() {
        offerTrack.placePlayer(player1, 'A');

        assertThrows(GameException.class,
                () -> offerTrack.placePlayer(player2, 'A'));
    }

    /**
     * Verifies that resolution order returns only occupied slots in correct order.
     * Setup: slot B is occupied before slot A.
     * Action: request the resolution order.
     * Expected behavior: occupied slots are returned in track order, A before B.
     * Edge case covered: resolution order is independent from placement chronology.
     */
    @Test
    void getResolutionOrderShouldReturnOccupiedSlotsInTrackOrder() {
        offerTrack.placePlayer(player2, 'B');
        offerTrack.placePlayer(player1, 'A');

        List<OfferSlot> order = offerTrack.getResolutionOrder();

        assertEquals(2, order.size());
        assertEquals('A', order.get(0).getSlotID());
        assertEquals('B', order.get(1).getSlotID());
    }

    /**
     * Verifies action retrieval for a placed player.
     * Setup: P1 occupies slot A, configured as one upper and zero lower selections.
     * Action: retrieve P1's action.
     * Expected behavior: the returned array is {1, 0}.
     * Edge case covered: action lookup is based on occupant identity.
     */
    @Test
    void getActionForShouldReturnActionForOccupyingPlayer() {
        offerTrack.placePlayer(player1, 'A');

        int[] action = offerTrack.getActionFor(player1);

        assertEquals(1, action[0]);
        assertEquals(0, action[1]);
    }

    /**
     * Verifies exception when player is not on any slot.
     * Setup: P2 is not occupying any slot.
     * Action: retrieve P2's action.
     * Expected behavior: an {@link IllegalArgumentException} is thrown.
     * Edge case covered: unplaced players cannot resolve offers.
     */
    @Test
    void getActionForShouldThrowWhenPlayerIsNotPlaced() {
        assertThrows(IllegalArgumentException.class,
                () -> offerTrack.getActionFor(player2));
    }

    /**
     * Verifies reset frees all slots.
     * Setup: both slots are occupied.
     * Action: reset the offer track.
     * Expected behavior: all slot occupants are removed.
     * Edge case covered: round cleanup must free the full offer track.
     */
    @Test
    void resetShouldClearAllSlotOccupants() {
        offerTrack.placePlayer(player1, 'A');
        offerTrack.placePlayer(player2, 'B');

        offerTrack.reset();

        assertNull(offerTrack.getSlot('A').getOccupant());
        assertNull(offerTrack.getSlot('B').getOccupant());
    }

    /**
     * Verifies getSlot returns correct slot.
     * Setup: a track containing slot A.
     * Action: request slot A.
     * Expected behavior: the returned slot has identifier A.
     * Edge case covered: direct slot lookup supports placement and validation logic.
     */
    @Test
    void getSlotShouldReturnMatchingSlot() {
        OfferSlot slot = offerTrack.getSlot('A');

        assertEquals('A', slot.getSlotID());
    }

    /**
     * Verifies exception when slot does not exist.
     * Setup: a track without slot Z.
     * Action: request slot Z.
     * Expected behavior: a {@link GameException} is thrown.
     * Edge case covered: invalid slot identifiers are rejected explicitly.
     */
    @Test
    void getSlotShouldThrowWhenSlotDoesNotExist() {
        assertThrows(GameException.class,
                () -> offerTrack.getSlot('Z'));
    }

    /**
     * Verifies that buildOfferSlotsData correctly maps slot state into DTOs.
     * Setup: P1 occupies slot A while slot B remains free.
     * Action: build DTOs for all offer slots.
     * Expected behavior: slot A includes P1's nickname and slot B has no occupant.
     * Edge case covered: network snapshots distinguish occupied and free slots.
     */
    @Test
    void buildOfferSlotsDataShouldMapOccupiedAndFreeSlots() {
        offerTrack.placePlayer(player1, 'A');

        var dtoList = offerTrack.buildOfferSlotsData();

        assertEquals(2, dtoList.size());

        var slotA = dtoList.stream()
                .filter(s -> s.slotID() == 'A')
                .findFirst()
                .orElseThrow();

        assertEquals(1, slotA.upSel());
        assertEquals(0, slotA.downSel());
        assertEquals(2, slotA.foodReward());
        assertEquals("P1", slotA.occupantNickname());

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

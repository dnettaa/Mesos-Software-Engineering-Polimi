package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link OfferSlot}, verifying slot configuration, occupation, removal,
 * and DTO conversion.
 *
 * @author Diana
 */
class OfferSlotTest {

    private OfferSlot slot;
    private Player firstPlayer;
    private Player secondPlayer;

    @BeforeEach
    void setUp() {
        slot = new OfferSlot('B', 0, 1, 2);
        firstPlayer = new Player("Alice", TotemColor.RED, new Tribe(), 0, 0);
        secondPlayer = new Player("Bob", TotemColor.BLUE, new Tribe(), 0, 0);
    }

    /**
     * Verifies that the slot exposes its immutable configuration values.
     * Setup: a slot configured with id B, zero upper selections, one lower selection, and two food.
     * Action: read every configuration getter.
     * Expected behavior: each getter returns the constructor value.
     * Edge case covered: slot action values must stay stable throughout the game.
     */
    @Test
    void gettersShouldReturnConfiguredSlotValues() {
        assertEquals('B', slot.getSlotID());
        assertEquals(0, slot.getUpSel());
        assertEquals(1, slot.getDownSel());
        assertEquals(2, slot.getFoodReward());
    }

    /**
     * Verifies that a new slot starts without an occupant.
     * Setup: a newly constructed offer slot.
     * Action: inspect occupation state.
     * Expected behavior: the slot is free and returns no occupant.
     * Edge case covered: slots must be available before totem placement.
     */
    @Test
    void newSlotShouldStartUnoccupied() {
        assertFalse(slot.isOccupied());
        assertNull(slot.getOccupant());
    }

    /**
     * Verifies that placing a player occupies the slot.
     * Setup: a free slot and one player.
     * Action: place the player on the slot.
     * Expected behavior: the slot becomes occupied by that exact player.
     * Edge case covered: occupation state must preserve the player reference for resolution order.
     */
    @Test
    void placeShouldSetOccupant() {
        slot.place(firstPlayer);

        assertTrue(slot.isOccupied());
        assertEquals(firstPlayer, slot.getOccupant());
    }

    /**
     * Verifies that placing a second player on an occupied slot is rejected.
     * Setup: a slot already occupied by Alice.
     * Action: Bob attempts to occupy the same slot.
     * Expected behavior: a {@link GameException} is thrown and Alice remains the occupant.
     * Edge case covered: offer slots cannot contain more than one totem.
     */
    @Test
    void placeShouldThrowWhenSlotIsAlreadyOccupied() {
        slot.place(firstPlayer);

        assertThrows(GameException.class, () -> slot.place(secondPlayer));
        assertEquals(firstPlayer, slot.getOccupant());
    }

    /**
     * Verifies that removing an occupied slot clears the occupant.
     * Setup: a slot occupied by Alice.
     * Action: remove the occupant.
     * Expected behavior: the slot becomes free.
     * Edge case covered: offer-track reset relies on idempotent slot cleanup.
     */
    @Test
    void removeShouldClearOccupant() {
        slot.place(firstPlayer);

        slot.remove();

        assertFalse(slot.isOccupied());
        assertNull(slot.getOccupant());
    }

    /**
     * Verifies that removing an already empty slot is safe.
     * Setup: a slot with no occupant.
     * Action: call remove.
     * Expected behavior: the slot remains free without throwing.
     * Edge case covered: track reset may call remove on empty slots.
     */
    @Test
    void removeShouldBeSafeWhenSlotIsEmpty() {
        slot.remove();

        assertFalse(slot.isOccupied());
        assertNull(slot.getOccupant());
    }

    /**
     * Verifies that slot DTOs expose both static action data and current occupant.
     * Setup: a slot occupied by Alice.
     * Action: build the DTO representation.
     * Expected behavior: the DTO contains slot configuration and Alice's nickname.
     * Edge case covered: network snapshots must not expose direct player references.
     */
    @Test
    void buildOfferSlotDataShouldIncludeConfigurationAndOccupantNickname() {
        slot.place(firstPlayer);

        var slotData = slot.buildOfferSlotData();

        assertEquals('B', slotData.slotID());
        assertEquals(0, slotData.upSel());
        assertEquals(1, slotData.downSel());
        assertEquals(2, slotData.foodReward());
        assertEquals("Alice", slotData.occupantNickname());
    }
}

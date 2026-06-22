package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link TurnOrderTrack}, verifying player order, food bonuses, capacity,
 * and reset behavior.
 *
 * @author Diana
 */
class TurnOrderTrackTest {

    private TurnOrderTrack track;
    private Player firstPlayer;
    private Player secondPlayer;
    private Player thirdPlayer;

    @BeforeEach
    void setUp() {
        firstPlayer = new Player("Alice", TotemColor.RED, new Tribe(), 0, 0);
        secondPlayer = new Player("Bob", TotemColor.BLUE, new Tribe(), 0, 0);
        thirdPlayer = new Player("Charlie", TotemColor.YELLOW, new Tribe(), 0, 0);

        track = new TurnOrderTrack(
                new ArrayList<>(List.of(firstPlayer, secondPlayer, thirdPlayer)),
                new int[]{2, 0, -1},
                3
        );
    }

    /**
     * Verifies that food bonuses are read by track position.
     * Setup: a three-position track with bonuses 2, 0, and -1.
     * Action: read each configured bonus.
     * Expected behavior: the values match the configured array.
     * Edge case covered: the last-position penalty marker is preserved as a negative value.
     */
    @Test
    void getPositionFoodBonusShouldReturnConfiguredBonus() {
        assertEquals(2, track.getPositionFoodBonus(0));
        assertEquals(0, track.getPositionFoodBonus(1));
        assertEquals(-1, track.getPositionFoodBonus(2));
    }

    /**
     * Verifies that player positions match the stored order.
     * Setup: a track initialized with Alice, Bob, and Charlie.
     * Action: read each player's position.
     * Expected behavior: positions are zero-based and match insertion order.
     * Edge case covered: turn-order logic depends on stable player indexes.
     */
    @Test
    void getPositionOfShouldReturnPlayerIndex() {
        assertEquals(0, track.getPositionOf(firstPlayer));
        assertEquals(1, track.getPositionOf(secondPlayer));
        assertEquals(2, track.getPositionOf(thirdPlayer));
    }

    /**
     * Verifies that asking for a missing player's position fails clearly.
     * Setup: a track that does not contain Gary.
     * Action: request Gary's position.
     * Expected behavior: an {@link IllegalArgumentException} is thrown.
     * Edge case covered: callers cannot silently treat absent players as valid positions.
     */
    @Test
    void getPositionOfShouldThrowWhenPlayerIsAbsent() {
        Player missingPlayer = new Player("Gary", TotemColor.BLACK, new Tribe(), 0, 0);

        assertThrows(IllegalArgumentException.class, () -> track.getPositionOf(missingPlayer));
    }

    /**
     * Verifies last-position detection.
     * Setup: a full three-player track.
     * Action: test each player for last-position status.
     * Expected behavior: only Charlie is considered last.
     * Edge case covered: last-position penalties apply only to the final track slot.
     */
    @Test
    void isLastPositionShouldBeTrueOnlyForLastPlayer() {
        assertTrue(track.isLastPosition(thirdPlayer));
        assertFalse(track.isLastPosition(firstPlayer));
        assertFalse(track.isLastPosition(secondPlayer));
    }

    /**
     * Verifies that reading player order returns a defensive copy.
     * Setup: a track initialized with three players.
     * Action: clear the returned order list.
     * Expected behavior: the track's internal order remains unchanged.
     * Edge case covered: external callers must not mutate turn order through getters.
     */
    @Test
    void getPlayersInOrderShouldReturnDefensiveCopy() {
        List<Player> order = track.getPlayersInOrder();
        order.clear();

        assertEquals(List.of(firstPlayer, secondPlayer, thirdPlayer), track.getPlayersInOrder());
    }

    /**
     * Verifies that the track exposes its configured player capacity.
     * Setup: a three-player track.
     * Action: read the configured number of players.
     * Expected behavior: the capacity is 3.
     * Edge case covered: capacity is independent from the current list size after clearing.
     */
    @Test
    void getNumPlayersShouldReturnConfiguredCapacity() {
        track.clear();

        assertEquals(3, track.getNumPlayers());
    }

    /**
     * Verifies that players are placed into the first available turn-order slots.
     * Setup: an empty track after clearing.
     * Action: place Bob and then Alice.
     * Expected behavior: order follows placement sequence.
     * Edge case covered: returned totems define next-round placement order.
     */
    @Test
    void placeFirstSlotShouldAppendPlayersInPlacementOrder() {
        track.clear();

        track.placeFirstSlot(secondPlayer);
        track.placeFirstSlot(firstPlayer);

        assertEquals(List.of(secondPlayer, firstPlayer), track.getPlayersInOrder());
    }

    /**
     * Verifies that placing into a full track is rejected.
     * Setup: a track already filled to capacity.
     * Action: attempt to place another player.
     * Expected behavior: an {@link IllegalStateException} is thrown.
     * Edge case covered: turn order cannot exceed the number of players.
     */
    @Test
    void placeFirstSlotShouldThrowWhenTrackIsFull() {
        assertThrows(IllegalStateException.class, () -> track.placeFirstSlot(firstPlayer));
    }

    /**
     * Verifies that clearing removes every player but leaves capacity intact.
     * Setup: a populated track.
     * Action: clear the track.
     * Expected behavior: no players remain and capacity is still 3.
     * Edge case covered: round setup can reuse the same track object.
     */
    @Test
    void clearShouldRemoveAllPlayersWithoutChangingCapacity() {
        track.clear();

        assertTrue(track.getPlayersInOrder().isEmpty());
        assertEquals(3, track.getNumPlayers());
    }
}

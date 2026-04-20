package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TurnOrderTrackTest {

    private TurnOrderTrack track;
    private Player p1, p2, p3;

    @BeforeEach
    void setUp(){
        p1 = new Player("Alice", TotemColor.RED, new Tribe(), 0, 0);
        p2 = new Player("Bob", TotemColor.BLUE, new Tribe(), 0, 0);
        p3 = new Player("Charlie", TotemColor.YELLOW, new Tribe(), 0, 0);

        List<Player> players = new ArrayList<>(List.of(p1, p2, p3));
        int[] foodBonus = {2, 0, -1};

        track = new TurnOrderTrack(players, foodBonus, 3);
    }

    @Test
    void testGetPositionFoodBonus(){
        assertEquals(2, track.getPositionFoodBonus(0));
        assertEquals(0, track.getPositionFoodBonus(1));
        assertEquals(-1, track.getPositionFoodBonus(2));
    }

    @Test
    void testGetPositionOf(){
        assertEquals(0, track.getPositionOf(p1));
        assertEquals(1, track.getPositionOf(p2));
        assertEquals(2, track.getPositionOf(p3));
    }

    @Test
    void testIsLastPosition(){
        assertTrue(track.isLastPosition(p3));
        assertFalse(track.isLastPosition(p1));
        assertFalse(track.isLastPosition(p2));
    }

    @Test
    void testGetPlayersInOrder(){
        List<Player> order = track.getPlayersInOrder();
        assertEquals(3, order.size());
        assertEquals(p1, order.get(0));
        assertEquals(p2, order.get(1));
        assertEquals(p3, order.get(2));
    }

    @Test
    void testGetNumPlayers(){
        assertEquals(3, track.getNumPlayers());
    }

    @Test
    void testPlaceFirstSlot(){
        track.clear();
        track.placeFirstSlot(p2);
        track.placeFirstSlot(p1);
        assertEquals(p2, track.getPlayersInOrder().get(0));
        assertEquals(p1, track.getPlayersInOrder().get(1));
    }

    @Test
    void testPlaceFirstSlotFullThrows(){
        assertThrows(IllegalStateException.class, () -> track.placeFirstSlot(p1));
    }

    @Test
    void testClear(){
        track.clear();
        assertEquals(0, track.getPlayersInOrder().size());
    }

    @Test
    void testGetPositionOfNotPresent(){
        Player p4 = new Player("Gary", TotemColor.BLACK, new Tribe(), 0, 0);
        assertThrows(IllegalArgumentException.class, () -> track.getPositionOf(p4));
    }
}
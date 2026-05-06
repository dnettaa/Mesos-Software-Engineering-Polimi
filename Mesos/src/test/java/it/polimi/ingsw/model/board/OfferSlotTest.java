package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OfferSlotTest {

    private OfferSlot slotB, slotC, slotE, slotF;
    private Player p1, p2;

    @BeforeEach
    void setUp(){
        slotB = new OfferSlot('B', 0, 1, 0);
        slotC = new OfferSlot('C', 1, 0, 0);
        slotE = new OfferSlot('E', 1, 1, 0);
        slotF = new OfferSlot('F', 2, 0, 0);

        p1 = new Player("Alice", TotemColor.RED, new Tribe(), 0, 0);
        p2 = new Player("Bob", TotemColor.BLUE, new Tribe(), 0, 0);
    }

    @Test
    void testGetSlotID(){
        assertEquals('B', slotB.getSlotID());
        assertEquals('C', slotC.getSlotID());
        assertEquals('E', slotE.getSlotID());
        assertEquals('F', slotF.getSlotID());
    }

    @Test
    void testGetUpSel(){
        assertEquals(0, slotB.getUpSel());
        assertEquals(1, slotC.getUpSel());
        assertEquals(1, slotE.getUpSel());
        assertEquals(2, slotF.getUpSel());
    }

    @Test
    void testGetDownSel(){
        assertEquals(1, slotB.getDownSel());
        assertEquals(0, slotC.getDownSel());
        assertEquals(1, slotE.getDownSel());
        assertEquals(0, slotF.getDownSel());
    }

    @Test
    void testGetFoodReward(){
        assertEquals(0, slotB.getFoodReward());
    }

    @Test
    void testIsOccupiedInitiallyFalse(){
        assertFalse(slotB.isOccupied());
    }

    @Test
    void testPlace(){
        slotB.place(p1);
        assertTrue(slotB.isOccupied());
        assertEquals(p1, slotB.getOccupant());
    }

    @Test
    void testPlaceAlreadyOccupiedThrows(){
        slotB.place(p1);
        assertThrows(GameException.class, () -> slotB.place(p2));
    }

    @Test
    void testRemove(){
        slotE.place(p1);

        slotE.remove();

        assertFalse(slotE.isOccupied());
        assertNull(slotE.getOccupant());
    }

    @Test
    void testRemoveEmptySlot(){
        slotC.remove();
        assertFalse(slotC.isOccupied());
        assertNull(slotC.getOccupant());
    }
}
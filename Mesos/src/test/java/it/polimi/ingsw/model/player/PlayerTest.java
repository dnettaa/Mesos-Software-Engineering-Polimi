package it.polimi.ingsw.model.player;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Luke", TotemColor.RED, new Tribe(), 0, 0);
    }

    @Test
    void testConstructorWithInitialValues() {
        Player p = new Player("Test", TotemColor.BLUE, new Tribe(), 5, 10);
        assertEquals(5, p.getFood());
        assertEquals(10, p.getPrestigePoints());
    }

    @Test
    void testAddFood() {
        player.addFood(5);
        assertEquals(5, player.getFood());
    }

    @Test
    void testAddFoodNegativeDoesNothing() {
        player.addFood(-3);
        assertEquals(0, player.getFood());
    }

    @Test
    void testSpendFood(){
        player.spendFood(5);
        assertEquals(0, player.getFood());
    }

    @Test
    void testSpendFoodWithInitialAmount(){
        player.addFood(10);
        player.spendFood(6);
        assertEquals(4, player.getFood());
    }

    @Test
    void testSpendFoodWithNegativeAmount(){
        player.addFood(10);
        player.spendFood(-6);
        assertEquals(10, player.getFood());
    }

    @Test
    void testAddPP(){
        player.addPP(10);
        assertEquals(10, player.getPrestigePoints());
    }

    @Test
    void testAddPPWithNegativeAmount(){
        player.addPP(-10);
        assertEquals(0, player.getPrestigePoints());
    }

    @Test
    void testLosePP(){
        player.losePP(10);
        assertEquals(-10, player.getPrestigePoints());
    }

    @Test
    void testLosePPWithInitalAmount(){
        player.addPP(25);
        player.losePP(10);
        assertEquals(15, player.getPrestigePoints());
    }
}
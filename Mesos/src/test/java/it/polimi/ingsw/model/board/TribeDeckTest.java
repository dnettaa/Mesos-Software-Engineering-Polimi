package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;

public class TribeDeckTest {

    private TribeDeck tribeDeck;

    @BeforeEach
    void setUp(){
        Deque<TribeCard> cards = new ArrayDeque<>();
        cards.addLast(new HunterCard(Era.Era1, "H1", false));
        cards.addLast(new ArtistCard(Era.Era1, "A1"));
        cards.addLast(new ShamanCard(Era.Era2, "S1", 2));
        cards.addLast(new BuilderCard(Era.Era2, "B1", 1, 3));
        cards.addLast(new GathererCard(Era.Era3, "G1"));

        tribeDeck = new TribeDeck(cards, Era.Era1);
    }

    @Test
    void testDraw(){
        assertEquals("H1", tribeDeck.draw().getId());
    }

    @Test
    void testDrawOrder(){
        assertEquals("H1", tribeDeck.draw().getId());
        assertEquals("A1", tribeDeck.draw().getId());
        assertEquals("S1", tribeDeck.draw().getId());
        assertEquals("B1", tribeDeck.draw().getId());
        assertEquals("G1", tribeDeck.draw().getId());
    }

    @Test
    void testDrawUpdatesEra(){
        tribeDeck.draw(); // H1 - Era1
        tribeDeck.draw(); // A1 - Era1
        assertEquals(Era.Era1, tribeDeck.getCurrentEra());
        tribeDeck.draw(); // S1 - Era2
        assertEquals(Era.Era2, tribeDeck.getCurrentEra());
    }

    @Test
    void testDrawEmptyDeckThrows(){
        for (int i = 0; i < 5; i++) tribeDeck.draw();
        assertThrows(IllegalStateException.class, () -> tribeDeck.draw());
    }

    @Test
    void testIsEmpty(){
        assertFalse(tribeDeck.isEmpty());
        for (int i = 0; i < 5; i++) tribeDeck.draw();
        assertTrue(tribeDeck.isEmpty());
    }
}
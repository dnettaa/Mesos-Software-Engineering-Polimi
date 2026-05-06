package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.*;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CardRow class.
 * Verifies the correct management, filtering, and movement of Tribe and Building cards.
 */
class CardRowTest {

    private CardRow cardRow;

    @BeforeEach
    void setUp() {
        // Initialize an empty CardRow
        cardRow = new CardRow(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Verifies that adding cards updates the row correctly and that getAllCards returns everything.
     */
    @Test
    void testAddAndGetAllCards() {
        TribeCard tribe = new HunterCard(Era.Era1, "H1", false);
        BuildingCard building = new EndBonusCard(Era.Era1, "B1", 0, 0, 5);

        cardRow.addTribeCard(tribe);
        cardRow.addBuildingCard(building);

        List<Card> all = cardRow.getAllCards();

        assertEquals(2, all.size());
        assertEquals("H1", all.get(0).getId());
        assertEquals("B1", all.get(1).getId());
    }

    /**
     * Verifies that the row correctly filters Characters (pickable) and Events (not pickable).
     */
    @Test
    void testEventFiltering() {
        TribeCard character = new HunterCard(Era.Era1, "H1", false);
        TribeCard event = new HuntEventCard(Era.Era1, "E1", false, 2);

        cardRow.addTribeCard(character);
        cardRow.addTribeCard(event);

        List<EventCard> events = cardRow.getEventCards();

        assertEquals(1, events.size());
        assertEquals("E1", events.getFirst().getId());
    }

    /**
     * Verifies the removal logic via Card.removeFrom().
     */
    @Test
    void testRemoveCard() {
        TribeCard tribe = new HunterCard(Era.Era1, "H1", false);

        cardRow.addTribeCard(tribe);
        cardRow.removeCard(tribe);

        assertTrue(cardRow.getTribeCards().isEmpty());
    }

    /**
     * Verifies exception when removing a card not in the row.
     */
    @Test
    void testRemoveInvalidCardThrows() {
        TribeCard tribe = new HunterCard(Era.Era1, "H1", false);

        assertThrows(IllegalArgumentException.class,
                () -> cardRow.removeCard(tribe));
    }

    /**
     * Verifies clearing tribe and building cards separately.
     */
    @Test
    void testClearMethods() {
        cardRow.addTribeCard(new HunterCard(Era.Era1, "H1", false));
        cardRow.addBuildingCard(new EndBonusCard(Era.Era1, "B1", 0, 0, 5));

        cardRow.clearTribeCards();
        assertTrue(cardRow.getTribeCards().isEmpty());

        cardRow.clearBuildingCards();
        assertTrue(cardRow.getBuildingCardsInternal().isEmpty());
    }

    /**
     * Verifies moving tribe cards to another row.
     */
    @Test
    void testMoveTribeCards() {
        CardRow destination = new CardRow(new ArrayList<>(), new ArrayList<>());

        cardRow.addTribeCard(new HunterCard(Era.Era1, "H1", false));

        cardRow.moveTribeCardsTo(destination);

        assertTrue(cardRow.getTribeCards().isEmpty());
        assertEquals(1, destination.getTribeCards().size());
    }

    /**
     * Verifies moving building cards to another row.
     */
    @Test
    void testMoveBuildingCards() {
        CardRow destination = new CardRow(new ArrayList<>(), new ArrayList<>());

        cardRow.addBuildingCard(new EndBonusCard(Era.Era1, "B1", 0, 0, 5));

        cardRow.moveBuildingCardsTo(destination);

        assertTrue(cardRow.getBuildingCardsInternal().isEmpty());
        assertEquals(1, destination.getBuildingCardsInternal().size());
    }

    /**
     * Verifies adding multiple building cards.
     */
    @Test
    void testAddBuildingCards() {
        List<BuildingCard> list = List.of(
                new EndBonusCard(Era.Era1, "B1", 0, 0, 5),
                new EndBonusCard(Era.Era1, "B2", 0, 0, 5)
        );

        cardRow.addBuildingCards(list);

        assertEquals(2, cardRow.getBuildingCardsInternal().size());
    }
}
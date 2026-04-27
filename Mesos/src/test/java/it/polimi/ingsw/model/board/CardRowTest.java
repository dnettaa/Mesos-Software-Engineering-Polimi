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
        assertTrue(cardRow.isEmpty());

        TribeCard hunter = new HunterCard(Era.Era1, "H1", false);
        BuildingCard building = new EndBonusCard(Era.Era1, "B1", 0, 0, 5);

        cardRow.addTribeCard(hunter);
        cardRow.addBuildingCard(building);

        assertFalse(cardRow.isEmpty());
        assertEquals(1, cardRow.getTribeCards().size());
        assertEquals(1, cardRow.getBuildingCards().size());

        // getAllCards should return Tribe cards first, then Building cards
        List<Card> all = cardRow.getAllCards();
        assertEquals(2, all.size());
        assertEquals(hunter, all.get(0));
        assertEquals(building, all.get(1));
    }

    /**
     * Verifies that the row correctly filters Characters (pickable) and Events (not pickable).
     */
    @Test
    void testFilterCharactersAndEvents() {
        TribeCard hunter = new HunterCard(Era.Era1, "H1", false); // Pickable
        TribeCard event = new HuntEventCard(Era.Era1, "E1", false, 2); // Not Pickable

        cardRow.addTribeCard(hunter);
        cardRow.addTribeCard(event);

        List<CharacterCard> characters = cardRow.getCharacterCards();
        List<EventCard> events = cardRow.getEventCards();

        assertEquals(1, characters.size());
        assertEquals(hunter, characters.getFirst());

        assertEquals(1, events.size());
        assertEquals(event, events.getFirst());
    }

    /**
     * Verifies the removal of specific cards and the clearing of lists.
     */
    @Test
    void testRemoveAndClearCards() {
        TribeCard hunter = new HunterCard(Era.Era1, "H1", false);
        BuildingCard building = new EndBonusCard(Era.Era1, "B1", 0, 0, 5);

        cardRow.addTribeCard(hunter);
        cardRow.addBuildingCard(building);

        // Remove single card
        cardRow.removeCard(hunter);
        assertEquals(0, cardRow.getTribeCards().size());
        assertEquals(1, cardRow.getBuildingCards().size());

        // Clear remaining buildings
        cardRow.clearBuildingCards();
        assertTrue(cardRow.isEmpty());
    }

    /**
     * Verifies the movement of cards from one row to another destination row.
     */
    @Test
    void testMoveCardsToDestination() {
        CardRow destinationRow = new CardRow(new ArrayList<>(), new ArrayList<>());

        TribeCard hunter = new HunterCard(Era.Era1, "H1", false);
        BuildingCard building = new EndBonusCard(Era.Era1, "B1", 0, 0, 5);

        cardRow.addTribeCard(hunter);
        cardRow.addBuildingCard(building);

        // Move Tribe cards
        cardRow.moveTribeCardsTo(destinationRow);
        assertEquals(0, cardRow.getTribeCards().size());
        assertEquals(1, destinationRow.getTribeCards().size());

        // Move Building cards
        cardRow.moveBuildingCardsTo(destinationRow);
        assertEquals(0, cardRow.getBuildingCards().size());
        assertEquals(1, destinationRow.getBuildingCards().size());

        assertTrue(cardRow.isEmpty());
    }
    /**
     * Verifies the addition of multiple building cards at once using a list.
     */
    @Test
    void testAddBuildingCards() {
        List<BuildingCard> newBuildings = new ArrayList<>();
        newBuildings.add(new EndBonusCard(Era.Era1, "B1", 0, 0, 5));
        newBuildings.add(new EndBonusCard(Era.Era1, "B2", 0, 0, 5));

        // Add the entire list
        cardRow.addBuildingCards(newBuildings);

        assertEquals(2, cardRow.getBuildingCards().size());
        assertEquals("B1", cardRow.getBuildingCards().get(0).getId());
        assertEquals("B2", cardRow.getBuildingCards().get(1).getId());
    }

    /**
     * Verifies the removal of a BuildingCard (covering the specific if branch)
     * and the exceptions thrown when removing non-existent cards.
     */
    @Test
    void testRemoveBuildingCardAndExceptions() {
        BuildingCard building = new EndBonusCard(Era.Era1, "B1", 0, 0, 5);
        TribeCard hunter = new HunterCard(Era.Era1, "H1", false);

        cardRow.addBuildingCard(building);

        // 1. Test removal of a BuildingCard
        cardRow.removeCard(building);
        assertTrue(cardRow.getBuildingCards().isEmpty());

        // 2. Test Exception when removing a BuildingCard not in the row
        assertThrows(IllegalArgumentException.class, () -> cardRow.removeCard(building));

        // 3. Test Exception when removing a TribeCard not in the row
        assertThrows(IllegalArgumentException.class, () -> cardRow.removeCard(hunter));
    }

    /**
     * Verifies the explicit clearing of Tribe cards.
     */
    @Test
    void testClearTribeCardsExplicitly() {
        cardRow.addTribeCard(new HunterCard(Era.Era1, "H1", false));
        assertFalse(cardRow.getTribeCards().isEmpty());

        // Clear tribe cards explicitly
        cardRow.clearTribeCards();
        assertTrue(cardRow.getTribeCards().isEmpty());
    }
}
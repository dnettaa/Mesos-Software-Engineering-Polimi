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
 *
 * @author Diana
 */
class CardRowTest {

    private CardRow cardRow;

    @BeforeEach
    void setUp() {
        cardRow = new CardRow(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Verifies that adding cards updates the row correctly and that getAllCards returns everything.
     * Setup: an empty row.
     * Action: add one tribe card and one building card.
     * Expected behavior: both cards are returned in tribe-before-building order.
     * Edge case covered: mixed rows expose a unified ordered view.
     */
    @Test
    void addCardsShouldMakeThemAvailableThroughGetAllCards() {
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
     * Verifies that card-row reads preserve the rendering order required by the
     * lower row: all tribe cards are returned before all building cards.
     * Setup: cards are added in an interleaved order, alternating tribe and building cards.
     * Action: read the row through {@link CardRow#getAllCards()}.
     * Expected behavior: the resulting list contains every tribe card first, then every building card.
     * Regression covered: lower-row rendering must not show buildings before remaining tribe cards.
     */
    @Test
    void getAllCardsShouldReturnTribeCardsBeforeBuildingsForLowerRowRendering() {
        TribeCard firstTribeCard = new HunterCard(Era.Era1, "CH01", false);
        TribeCard secondTribeCard = new BuilderCard(Era.Era1, "CH02", 1, 0);
        BuildingCard firstBuildingCard = new EndBonusCard(Era.Era1, "BU01", 0, 0, 5);
        BuildingCard secondBuildingCard = new EndBonusCard(Era.Era1, "BU02", 0, 0, 3);

        cardRow.addBuildingCard(firstBuildingCard);
        cardRow.addTribeCard(firstTribeCard);
        cardRow.addBuildingCard(secondBuildingCard);
        cardRow.addTribeCard(secondTribeCard);

        List<Card> allCards = cardRow.getAllCards();

        assertEquals(List.of("CH01", "CH02", "BU01", "BU02"),
                allCards.stream().map(Card::getId).toList());
    }

    /**
     * Verifies that the row correctly filters Characters (pickable) and Events (not pickable).
     * Setup: a row containing one character and one event.
     * Action: request event cards.
     * Expected behavior: only the event card is returned.
     * Edge case covered: event resolution must ignore pickable tribe cards.
     */
    @Test
    void getEventCardsShouldReturnOnlyNonPickableEvents() {
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
     * Setup: a row containing one tribe card.
     * Action: remove that card through {@link CardRow#removeCard(Card)}.
     * Expected behavior: the tribe-card list becomes empty.
     * Edge case covered: removal is delegated polymorphically to the card type.
     */
    @Test
    void removeCardShouldRemoveExistingCard() {
        TribeCard tribe = new HunterCard(Era.Era1, "H1", false);

        cardRow.addTribeCard(tribe);
        cardRow.removeCard(tribe);

        assertTrue(cardRow.getTribeCards().isEmpty());
    }

    /**
     * Verifies exception when removing a card not in the row.
     * Setup: an empty row.
     * Action: attempt to remove a tribe card that was never added.
     * Expected behavior: an {@link IllegalArgumentException} is thrown.
     * Edge case covered: invalid removals fail loudly instead of silently changing state.
     */
    @Test
    void removeCardShouldThrowWhenCardIsAbsent() {
        TribeCard tribe = new HunterCard(Era.Era1, "H1", false);

        assertThrows(IllegalArgumentException.class,
                () -> cardRow.removeCard(tribe));
    }

    /**
     * Verifies clearing tribe and building cards separately.
     * Setup: a row containing one tribe card and one building card.
     * Action: clear tribe cards and then building cards.
     * Expected behavior: each clear operation affects only its own category.
     * Edge case covered: round setup can discard tribe and building cards independently.
     */
    @Test
    void clearMethodsShouldClearCardCategoriesIndependently() {
        cardRow.addTribeCard(new HunterCard(Era.Era1, "H1", false));
        cardRow.addBuildingCard(new EndBonusCard(Era.Era1, "B1", 0, 0, 5));

        cardRow.clearTribeCards();
        assertTrue(cardRow.getTribeCards().isEmpty());

        cardRow.clearBuildingCards();
        assertTrue(cardRow.getBuildingCardsInternal().isEmpty());
    }

    /**
     * Verifies moving tribe cards to another row.
     * Setup: a source row with one tribe card and an empty destination row.
     * Action: move tribe cards to the destination.
     * Expected behavior: source tribe cards are cleared and destination receives the card.
     * Edge case covered: upper-row tribe cards can move to the lower row during round setup.
     */
    @Test
    void moveTribeCardsToShouldTransferTribeCardsAndClearSource() {
        CardRow destination = new CardRow(new ArrayList<>(), new ArrayList<>());

        cardRow.addTribeCard(new HunterCard(Era.Era1, "H1", false));

        cardRow.moveTribeCardsTo(destination);

        assertTrue(cardRow.getTribeCards().isEmpty());
        assertEquals(1, destination.getTribeCards().size());
    }

    /**
     * Verifies moving building cards to another row.
     * Setup: a source row with one building card and an empty destination row.
     * Action: move building cards to the destination.
     * Expected behavior: source building cards are cleared and destination receives the card.
     * Edge case covered: building rows can be moved separately from tribe cards.
     */
    @Test
    void moveBuildingCardsToShouldTransferBuildingCardsAndClearSource() {
        CardRow destination = new CardRow(new ArrayList<>(), new ArrayList<>());

        cardRow.addBuildingCard(new EndBonusCard(Era.Era1, "B1", 0, 0, 5));

        cardRow.moveBuildingCardsTo(destination);

        assertTrue(cardRow.getBuildingCardsInternal().isEmpty());
        assertEquals(1, destination.getBuildingCardsInternal().size());
    }

    /**
     * Verifies adding multiple building cards.
     * Setup: an empty row and two building cards.
     * Action: add both buildings through {@link CardRow#addBuildingCards(List)}.
     * Expected behavior: both buildings are stored in order.
     * Edge case covered: era reveal can add a batch of buildings at once.
     */
    @Test
    void addBuildingCardsShouldAppendAllProvidedBuildings() {
        List<BuildingCard> list = List.of(
                new EndBonusCard(Era.Era1, "B1", 0, 0, 5),
                new EndBonusCard(Era.Era1, "B2", 0, 0, 5)
        );

        cardRow.addBuildingCards(list);

        assertEquals(2, cardRow.getBuildingCardsInternal().size());
        assertEquals(List.of("B1", "B2"), cardRow.getBuildingCardsInternal().stream().map(Card::getId).toList());
    }
}

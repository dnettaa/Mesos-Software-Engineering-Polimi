package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.card.building.EndBonusCard;
import it.polimi.ingsw.model.card.building.EndPerTypeCard;
import it.polimi.ingsw.model.card.building.ExtraPickCard;
import it.polimi.ingsw.model.card.building.ShamanBonusIconsCard;
import it.polimi.ingsw.model.card.building.ShamanDoubleRewardCard;
import it.polimi.ingsw.model.card.building.SustenanceDiscountCard;
import it.polimi.ingsw.model.card.building.TriggerSetCard;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link BuildingDeck}, verifying that buildings are grouped and revealed
 * by era without exposing the deck's internal mutable lists.
 *
 * @author Diana
 */
class BuildingDeckTest {

    private BuildingDeck buildingDeck;

    @BeforeEach
    void setUp() {
        List<BuildingCard> era1 = List.of(
                new TriggerSetCard(Era.Era1, "BU01", 4, 3, 5),
                new SustenanceDiscountCard(Era.Era1, "BU02", 4, 4, CharacterType.GATHERER)
        );
        List<BuildingCard> era2 = List.of(
                new ShamanDoubleRewardCard(Era.Era2, "BU07", 7, 0),
                new ShamanBonusIconsCard(Era.Era2, "BU08", 6, 4, 3)
        );
        List<BuildingCard> era3 = List.of(
                new EndPerTypeCard(Era.Era3, "BU14", 8, 8, CharacterType.HUNTER, 3),
                new ExtraPickCard(Era.Era3, "BU20", 9, 3),
                new EndBonusCard(Era.Era3, "BU21", 10, 0, 25)
        );

        buildingDeck = new BuildingDeck(era1, era2, era3);
    }

    /**
     * Verifies that Era 1 buildings are revealed in their configured order.
     * Setup: the deck contains two Era 1 buildings.
     * Action: reveal all Era 1 buildings.
     * Expected behavior: the returned list contains BU01 followed by BU02.
     * Edge case covered: reveal order must remain stable for deterministic board setup.
     */
    @Test
    void revealAllShouldReturnEraOneBuildingsInOrder() {
        List<BuildingCard> era1Cards = buildingDeck.revealAll(Era.Era1);

        assertEquals(List.of("BU01", "BU02"), era1Cards.stream().map(BuildingCard::getId).toList());
    }

    /**
     * Verifies that Era 2 buildings are revealed independently from other eras.
     * Setup: the deck contains cards for all eras.
     * Action: reveal all Era 2 buildings.
     * Expected behavior: only Era 2 cards are returned.
     * Edge case covered: era-specific reveal must not leak buildings from another era.
     */
    @Test
    void revealAllShouldReturnOnlyEraTwoBuildings() {
        List<BuildingCard> era2Cards = buildingDeck.revealAll(Era.Era2);

        assertEquals(2, era2Cards.size());
        assertTrue(era2Cards.stream().allMatch(card -> card.getEra() == Era.Era2));
        assertEquals(List.of("BU07", "BU08"), era2Cards.stream().map(BuildingCard::getId).toList());
    }

    /**
     * Verifies that Era 3 buildings are revealed in their configured order.
     * Setup: the deck contains three Era 3 buildings.
     * Action: reveal all Era 3 buildings.
     * Expected behavior: the returned IDs match the configured Era 3 sequence.
     * Edge case covered: later-era buildings must also preserve deck configuration order.
     */
    @Test
    void revealAllShouldReturnEraThreeBuildingsInOrder() {
        List<BuildingCard> era3Cards = buildingDeck.revealAll(Era.Era3);

        assertEquals(List.of("BU14", "BU20", "BU21"), era3Cards.stream().map(BuildingCard::getId).toList());
    }

    /**
     * Verifies that revealed lists are defensive copies.
     * Setup: Era 1 buildings are revealed and the returned list is cleared.
     * Action: reveal Era 1 buildings again.
     * Expected behavior: the second reveal still returns the original deck contents.
     * Edge case covered: external callers must not mutate the deck by changing a revealed list.
     */
    @Test
    void revealAllShouldReturnDefensiveCopy() {
        List<BuildingCard> revealedCards = buildingDeck.revealAll(Era.Era1);
        revealedCards.clear();

        List<BuildingCard> secondReveal = buildingDeck.revealAll(Era.Era1);

        assertEquals(List.of("BU01", "BU02"), secondReveal.stream().map(BuildingCard::getId).toList());
    }
}

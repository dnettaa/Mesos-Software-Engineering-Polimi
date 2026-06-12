package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.card.ArtistCard;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.GathererCard;
import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.card.InventionType;
import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.card.ShamanCard;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Tribe}, verifying character grouping, icon counting, discounts,
 * distinct-type counting, and complete-set calculation.
 *
 * @author Diana
 */
class TribeTest {

    private Tribe tribe;
    private Player player;

    @BeforeEach
    void setUp() {
        tribe = new Tribe();
        player = new Player("Test", TotemColor.RED, tribe, 0, 0);

        new HunterCard(Era.Era1, "H1", false).applyTo(player);
        new HunterCard(Era.Era1, "H2", true).applyTo(player);
        new ShamanCard(Era.Era1, "S1", 2).applyTo(player);
        new ShamanCard(Era.Era2, "S2", 3).applyTo(player);
        new BuilderCard(Era.Era1, "B1", 1, 3).applyTo(player);
        new InventorCard(Era.Era1, "I1", InventionType.TYPE_1).applyTo(player);
        new InventorCard(Era.Era2, "I2", InventionType.TYPE_3).applyTo(player);
        new ArtistCard(Era.Era1, "A1").applyTo(player);
        new ArtistCard(Era.Era2, "A2").applyTo(player);
        new GathererCard(Era.Era1, "G1").applyTo(player);
    }

    /**
     * Verifies that the tribe counts acquired character cards by type.
     * Setup: the tribe contains hunters, shamans, builders, inventors, artists, and gatherers.
     * Action: count cards for every character type.
     * Expected behavior: each count matches the cards applied during setup.
     * Edge case covered: repeated cards of the same type are counted independently.
     */
    @Test
    void countByTypeShouldReturnNumberOfCardsForEachCharacterType() {
        assertEquals(1, tribe.countByType(CharacterType.BUILDER));
        assertEquals(2, tribe.countByType(CharacterType.SHAMAN));
        assertEquals(2, tribe.countByType(CharacterType.ARTIST));
        assertEquals(1, tribe.countByType(CharacterType.GATHERER));
        assertEquals(2, tribe.countByType(CharacterType.HUNTER));
        assertEquals(2, tribe.countByType(CharacterType.INVENTOR));
    }

    /**
     * Verifies that cards can be retrieved by character type.
     * Setup: the tribe contains two hunter cards.
     * Action: retrieve hunter cards.
     * Expected behavior: both hunters are returned in acquisition order.
     * Edge case covered: callers can inspect grouped cards without reading the full member map.
     */
    @Test
    void getByTypeShouldReturnCardsOfRequestedType() {
        List<CharacterCard> hunters = tribe.getByType(CharacterType.HUNTER);

        assertEquals(List.of("H1", "H2"), hunters.stream().map(CharacterCard::getId).toList());
    }

    /**
     * Verifies that {@link Tribe#getByType(CharacterType)} returns a defensive copy.
     * Setup: the tribe contains two hunters.
     * Action: clear the returned hunter list.
     * Expected behavior: the tribe still reports two hunters.
     * Edge case covered: external callers cannot mutate tribe state through the getter.
     */
    @Test
    void getByTypeShouldReturnDefensiveCopy() {
        List<CharacterCard> hunters = tribe.getByType(CharacterType.HUNTER);

        hunters.clear();

        assertEquals(2, tribe.countByType(CharacterType.HUNTER));
    }

    /**
     * Verifies that distinct inventor icons are counted.
     * Setup: the tribe contains two inventors with different invention types.
     * Action: count distinct invention icons.
     * Expected behavior: the result is two.
     * Edge case covered: final scoring depends on distinct icon types, not inventor quantity alone.
     */
    @Test
    void countDistinctInventionIconsShouldCountUniqueInventorTypes() {
        assertEquals(2, tribe.countDistinctInventionIcons());
    }

    /**
     * Verifies that builder cards provide building discounts.
     * Setup: the tribe contains one builder with discount one.
     * Action: calculate the building discount.
     * Expected behavior: discount is one.
     * Edge case covered: building costs depend on accumulated builder effects.
     */
    @Test
    void getBuildingDiscountShouldSumBuilderDiscounts() {
        assertEquals(1, tribe.getBuildingDiscount());
    }

    /**
     * Verifies that gatherers provide sustenance collection discount.
     * Setup: the tribe contains one gatherer.
     * Action: calculate collector discount.
     * Expected behavior: discount is three.
     * Edge case covered: each gatherer contributes a fixed discount amount.
     */
    @Test
    void getCollectorDiscountShouldReturnThreePerGatherer() {
        assertEquals(3, tribe.getCollectorDiscount());
    }

    /**
     * Verifies that all represented character types are counted once.
     * Setup: the tribe contains at least one card for all six character types.
     * Action: count distinct character types.
     * Expected behavior: the result is six.
     * Edge case covered: duplicate cards do not increase the distinct-type count.
     */
    @Test
    void countDistinctTypesShouldCountRepresentedCharacterTypes() {
        assertEquals(6, tribe.countDistinctTypes());
    }

    /**
     * Verifies complete character set calculation.
     * Setup: the tribe contains at least one card for each character type and duplicates for some types.
     * Action: calculate full sets.
     * Expected behavior: exactly one complete set is available.
     * Edge case covered: complete sets are limited by the least represented character type.
     */
    @Test
    void getFullSetsCountShouldReturnNumberOfCompleteCharacterSets() {
        assertEquals(1, tribe.getFullSetsCount());
    }

    /**
     * Verifies total shaman icon counting.
     * Setup: the tribe contains shamans with two and three symbols.
     * Action: count shaman icons.
     * Expected behavior: total symbols equal five.
     * Edge case covered: ritual resolution depends on symbol totals, not just shaman card count.
     */
    @Test
    void countShamanIconsShouldSumSymbolsOnAllShamans() {
        assertEquals(5, tribe.countShamanIcons());
    }

    /**
     * Verifies that a new tribe starts with no cards or derived bonuses.
     * Setup: a fresh tribe with no applied cards.
     * Action: read counts and aggregate effects.
     * Expected behavior: all counts and discounts are zero.
     * Edge case covered: empty tribes are valid at game start.
     */
    @Test
    void newTribeShouldStartEmpty() {
        Tribe emptyTribe = new Tribe();

        assertTrue(emptyTribe.getBuildings().isEmpty());
        assertEquals(0, emptyTribe.countDistinctTypes());
        assertEquals(0, emptyTribe.getFullSetsCount());
        assertEquals(0, emptyTribe.getBuildingDiscount());
        assertEquals(0, emptyTribe.getCollectorDiscount());
        assertEquals(0, emptyTribe.countShamanIcons());
    }
}

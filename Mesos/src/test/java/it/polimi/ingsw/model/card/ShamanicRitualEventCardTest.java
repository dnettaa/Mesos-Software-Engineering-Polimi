package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.card.building.ShamanBonusIconsCard;
import it.polimi.ingsw.model.card.building.ShamanDoubleRewardCard;
import it.polimi.ingsw.model.card.building.ShamanNoPenaltyCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ShamanicRitualEventCard} majority and minority resolution rules.
 */
class ShamanicRitualEventCardTest {

    /**
     * Setup: two players have different numbers of shaman icons.
     * Action: resolve a shamanic ritual event with a majority reward and a minority penalty.
     * Expected behavior: the player with most icons gains prestige, while the player with fewest icons loses it.
     * Edge case: the minority player can go below zero prestige points.
     */
    @Test
    void resolveEventShouldRewardMajorityAndPenalizeMinority() {
        ShamanicRitualEventCard event = new ShamanicRitualEventCard(Era.Era1, "SR1", false, 10, 5);
        Player majorityPlayer = new Player("Majority", null, new Tribe(), 0, 0);
        Player minorityPlayer = new Player("Minority", null, new Tribe(), 0, 0);

        new ShamanCard(Era.Era1, "S1", 3).applyTo(majorityPlayer);
        new ShamanCard(Era.Era1, "S2", 1).applyTo(minorityPlayer);

        event.resolveEvent(List.of(majorityPlayer, minorityPlayer));

        assertEquals(10, majorityPlayer.getPrestigePoints());
        assertEquals(-5, minorityPlayer.getPrestigePoints());
    }

    /**
     * Setup: one player owns a building that adds virtual shaman icons.
     * Action: resolve the ritual against a player with more printed shaman icons.
     * Expected behavior: bonus building icons are included when determining majority and minority.
     * Edge case: a building effect can change the winner even when printed tribe cards alone would not.
     */
    @Test
    void resolveEventShouldIncludeBonusIconsFromBuildings() {
        ShamanicRitualEventCard event = new ShamanicRitualEventCard(Era.Era1, "SR2", false, 8, 3);
        Player boostedPlayer = new Player("Boosted", null, new Tribe(), 0, 0);
        Player printedIconsPlayer = new Player("Printed", null, new Tribe(), 0, 0);

        new ShamanCard(Era.Era1, "S1", 1).applyTo(boostedPlayer);
        new ShamanBonusIconsCard(Era.Era1, "B1", 0, 0, 4).applyTo(boostedPlayer);
        new ShamanCard(Era.Era1, "S2", 3).applyTo(printedIconsPlayer);

        event.resolveEvent(List.of(boostedPlayer, printedIconsPlayer));

        assertEquals(8, boostedPlayer.getPrestigePoints());
        assertEquals(-3, printedIconsPlayer.getPrestigePoints());
    }

    /**
     * Setup: the majority player owns a double-reward building and the minority player owns a no-penalty building.
     * Action: resolve a ritual with both building effects active.
     * Expected behavior: the winner reward is doubled and the loser penalty is prevented.
     * Edge case: independent building modifiers must both be applied in the same event resolution.
     */
    @Test
    void resolveEventShouldApplyRewardMultiplierAndPenaltyProtection() {
        ShamanicRitualEventCard event = new ShamanicRitualEventCard(Era.Era1, "SR3", false, 6, 4);
        Player majorityPlayer = new Player("Majority", null, new Tribe(), 0, 0);
        Player protectedMinority = new Player("Protected", null, new Tribe(), 0, 0);

        new ShamanCard(Era.Era1, "S1", 2).applyTo(majorityPlayer);
        new ShamanDoubleRewardCard(Era.Era1, "B1", 0, 0).applyTo(majorityPlayer);
        new ShamanNoPenaltyCard(Era.Era1, "B2", 0, 0).applyTo(protectedMinority);

        event.resolveEvent(List.of(majorityPlayer, protectedMinority));

        assertEquals(12, majorityPlayer.getPrestigePoints());
        assertEquals(0, protectedMinority.getPrestigePoints());
    }
}

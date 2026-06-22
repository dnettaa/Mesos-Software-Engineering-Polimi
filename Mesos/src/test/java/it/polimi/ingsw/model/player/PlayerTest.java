package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.card.HunterCard;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Player}, verifying identity fields, resource updates, prestige
 * point updates, and DTO conversion.
 *
 * @author Diana
 */
class PlayerTest {

    private Player player;
    private Tribe tribe;

    @BeforeEach
    void setUp() {
        tribe = new Tribe();
        player = new Player("Luke", TotemColor.RED, tribe, 0, 0);
    }

    /**
     * Verifies that constructor parameters initialize player identity and state.
     * Setup: a player is created with nickname, color, tribe, food, and prestige points.
     * Action: read all constructor-backed getters.
     * Expected behavior: every getter returns the provided value.
     * Edge case covered: player creation supports non-zero initial resources.
     */
    @Test
    void constructorShouldStoreIdentityTribeAndInitialResources() {
        Player initializedPlayer = new Player("Test", TotemColor.BLUE, tribe, 5, 10);

        assertEquals("Test", initializedPlayer.getNickname());
        assertEquals(TotemColor.BLUE, initializedPlayer.getTotemColor());
        assertSame(tribe, initializedPlayer.getTribe());
        assertEquals(5, initializedPlayer.getFood());
        assertEquals(10, initializedPlayer.getPrestigePoints());
    }

    /**
     * Verifies that positive food gains increase the player's food reserve.
     * Setup: a player starts with zero food.
     * Action: add five food.
     * Expected behavior: food becomes five.
     * Edge case covered: normal resource rewards are accumulated.
     */
    @Test
    void addFoodShouldIncreaseFoodWhenAmountIsPositive() {
        player.addFood(5);

        assertEquals(5, player.getFood());
    }

    /**
     * Verifies that non-positive food gains are ignored.
     * Setup: a player starts with zero food.
     * Action: add a negative food amount.
     * Expected behavior: food remains unchanged.
     * Edge case covered: invalid rewards cannot reduce food through {@link Player#addFood(int)}.
     */
    @Test
    void addFoodShouldIgnoreNegativeAmount() {
        player.addFood(-3);

        assertEquals(0, player.getFood());
    }

    /**
     * Verifies that spending more food than available leaves food unchanged.
     * Setup: a player starts with zero food.
     * Action: attempt to spend five food.
     * Expected behavior: food remains zero.
     * Edge case covered: overspending is ignored by the player resource model.
     */
    @Test
    void spendFoodShouldIgnoreAmountGreaterThanAvailableFood() {
        player.spendFood(5);

        assertEquals(0, player.getFood());
    }

    /**
     * Verifies that valid food spending subtracts from the current reserve.
     * Setup: a player has ten food.
     * Action: spend six food.
     * Expected behavior: four food remain.
     * Edge case covered: building purchases can consume only available resources.
     */
    @Test
    void spendFoodShouldDecreaseFoodWhenAmountIsAvailable() {
        player.addFood(10);

        player.spendFood(6);

        assertEquals(4, player.getFood());
    }

    /**
     * Verifies that negative food spending is ignored.
     * Setup: a player has ten food.
     * Action: spend a negative amount.
     * Expected behavior: food remains unchanged.
     * Edge case covered: invalid spending commands cannot increase resources.
     */
    @Test
    void spendFoodShouldIgnoreNegativeAmount() {
        player.addFood(10);

        player.spendFood(-6);

        assertEquals(10, player.getFood());
    }

    /**
     * Verifies that positive prestige point gains increase the player's score.
     * Setup: a player starts with zero prestige points.
     * Action: add ten prestige points.
     * Expected behavior: prestige points become ten.
     * Edge case covered: event and scoring rewards are accumulated.
     */
    @Test
    void addPPShouldIncreasePrestigeWhenAmountIsPositive() {
        player.addPP(10);

        assertEquals(10, player.getPrestigePoints());
    }

    /**
     * Verifies that negative prestige point gains are ignored.
     * Setup: a player starts with zero prestige points.
     * Action: add a negative prestige amount.
     * Expected behavior: prestige points remain unchanged.
     * Edge case covered: penalties must use {@link Player#losePP(int)}, not negative rewards.
     */
    @Test
    void addPPShouldIgnoreNegativeAmount() {
        player.addPP(-10);

        assertEquals(0, player.getPrestigePoints());
    }

    /**
     * Verifies that losing prestige can produce a negative score.
     * Setup: a player starts with zero prestige points.
     * Action: lose ten prestige points.
     * Expected behavior: prestige points become negative ten.
     * Edge case covered: Mesos allows negative final scores.
     */
    @Test
    void losePPShouldAllowNegativePrestige() {
        player.losePP(10);

        assertEquals(-10, player.getPrestigePoints());
    }

    /**
     * Verifies that prestige losses subtract from an existing positive score.
     * Setup: a player has 25 prestige points.
     * Action: lose ten prestige points.
     * Expected behavior: 15 prestige points remain.
     * Edge case covered: penalties apply consistently to positive scores.
     */
    @Test
    void losePPShouldDecreaseExistingPrestige() {
        player.addPP(25);

        player.losePP(10);

        assertEquals(15, player.getPrestigePoints());
    }

    /**
     * Verifies that the player DTO contains identity, resources, and acquired card identifiers.
     * Setup: a player has food, prestige points, and one acquired hunter card.
     * Action: build {@link it.polimi.ingsw.model.game.DTO.PlayerData}.
     * Expected behavior: the DTO exposes nickname, color, resources, and the hunter card ID.
     * Edge case covered: client snapshots contain IDs rather than direct card objects.
     */
    @Test
    void buildPlayerDataShouldExposePlayerSnapshotWithCardIds() {
        player.addFood(4);
        player.addPP(6);
        new HunterCard(Era.Era1, "CH_HUNTER", false).applyTo(player);

        var playerData = player.buildPlayerData();

        assertEquals("Luke", playerData.nickname());
        assertEquals(TotemColor.RED, playerData.totemColor());
        assertEquals(4, playerData.food());
        assertEquals(6, playerData.prestigePoints());
        assertEquals(1, playerData.tribeCardID().size());
        assertTrue(playerData.tribeCardID().contains("CH_HUNTER"));
        assertTrue(playerData.buildingID().isEmpty());
    }
}

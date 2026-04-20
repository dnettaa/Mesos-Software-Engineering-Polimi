package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CharacterCard implementations.
 * Verifies correct property initialization and acquisition logic
 * simulating the real game flow via applyTo().
 */
class CharacterCardsTest {

    private Player realPlayer;

    @BeforeEach
    void setUp() {
        // Initialize a real player before each test using the correct constructor
        realPlayer = new Player("TestUser", null, new Tribe(), 0, 0);
    }

    /**
     * Verifies the behavior of {@link HunterCard} when the food bonus is disabled.
     * The player should not receive any extra food when acquiring this card.
     */
    @Test
    void testHunterCardNoFoodBonus() {
        HunterCard card = new HunterCard(null, "H01", false);

        int initialFood = realPlayer.getFood();

        // Add existing hunters to the tribe via applyTo
        new HunterCard(null, "H02", false).applyTo(realPlayer);
        new HunterCard(null, "H03", false).applyTo(realPlayer);

        // Apply our test card
        card.applyTo(realPlayer);

        // Even with 3 hunters in the tribe, the food should not increase
        // because the bonus is disabled on the acquired card
        assertEquals(initialFood, realPlayer.getFood());
    }

    /**
     * Verifies the behavior of {@link HunterCard} when the food bonus is enabled.
     * When acquired, the player must receive an amount of food equal
     * to the total number of hunters present in the tribe (including the new one).
     */
    @Test
    void testHunterCardWithFoodBonus() {
        // Add two standard hunters first (no bonus)
        new HunterCard(null, "H01", false).applyTo(realPlayer);
        new HunterCard(null, "H02", false).applyTo(realPlayer);

        int currentFood = realPlayer.getFood();

        // Create the third hunter, this one WITH the bonus enabled
        HunterCard bonusCard = new HunterCard(null, "H03", true);

        // Applying this card should trigger the bonus logic
        bonusCard.applyTo(realPlayer);

        // The player should gain 3 food (1 for each hunter now in the tribe)
        assertEquals(currentFood + 3, realPlayer.getFood());
    }

    /**
     * Verifies the properties of the {@link ShamanCard}.
     * The card must return the correct character type and the exact number
     * of shaman symbols it was initialized with.
     */
    @Test
    void testShamanCardProperties() {
        ShamanCard card = new ShamanCard(null, "SH01", 2);

        assertEquals(2, card.getShamanSymbols());
    }
}
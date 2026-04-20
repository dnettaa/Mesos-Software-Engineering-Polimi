package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Event cards.
 * Updated to use card.applyTo(player) to populate the Tribe.
 */
class EventCardsTest {

    private Player realPlayer;

    @BeforeEach
    void setUp() {
        // Initialize a real player using the actual constructor
        realPlayer = new Player("TestUser", null, new Tribe(), 0, 0);
    }

    /**
     * Verifies the correct resolution of the {@link HuntEventCard}.
     */
    @Test
    void testHuntEventResolution() {
        HuntEventCard event = new HuntEventCard(null, "HE01", false, 2);

        // We use applyTo() to add characters to the tribe, testing the real game flow
        new HunterCard(null, "H01", false).applyTo(realPlayer);
        new HunterCard(null, "H02", false).applyTo(realPlayer);
        new HunterCard(null, "H03", false).applyTo(realPlayer);

        int initialFood = realPlayer.getFood();
        int initialPP = realPlayer.getPrestigePoints();

        event.resolveEvent(List.of(realPlayer));

        // 3 Hunters * 1 Food = 3 extra Food
        assertEquals(initialFood + 3, realPlayer.getFood());
        // 3 Hunters * 2 PP = 6 extra PP
        assertEquals(initialPP + 6, realPlayer.getPrestigePoints());
    }

    /**
     * Verifies the resolution of the {@link CavePaintingsEventCard} in case of success.
     */
    @Test
    void testCavePaintingsEventResolution_Success() {
        CavePaintingsEventCard event = new CavePaintingsEventCard(null, "CP01", true, 2, 2, 3);

        // Use applyTo() to add the artists to the player's tribe
        new ArtistCard(null, "A01").applyTo(realPlayer);
        new ArtistCard(null, "A02").applyTo(realPlayer);
        new ArtistCard(null, "A03").applyTo(realPlayer);

        int initialPP = realPlayer.getPrestigePoints();

        event.resolveEvent(List.of(realPlayer));

        // Reward: 3 Artists * 3 PP = 9 PP
        assertEquals(initialPP + 9, realPlayer.getPrestigePoints());
    }
}
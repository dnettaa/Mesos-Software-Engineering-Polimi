package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.game.Era;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Cave Paintings Event resolution and Artist requirements.
 */
class CavePaintingsEventCardTest {

    /**
     * Verifies the resolution when the player meets or exceeds the required number of Artists.
     * The player should receive the reward multiplied by the number of Artists without penalties.
     */
    @Test
    void testCavePaintingsSuccess() {
        // Required: 2 Artists. Penalty: 2 PP. Reward: 3 PP per Artist
        CavePaintingsEventCard event = new CavePaintingsEventCard(Era.Era1, "CP1", true, 2, 2, 3);
        Player player = new Player("P1", null, new Tribe(), 0, 0);

        // Add 3 artists to the tribe (Requirement met)
        new ArtistCard(Era.Era1, "A1").applyTo(player);
        new ArtistCard(Era.Era1, "A2").applyTo(player);
        new ArtistCard(Era.Era1, "A3").applyTo(player);

        event.resolveEvent(List.of(player));

        // Expected Reward: 3 Artists * 3 PP = 9 PP
        assertEquals(9, player.getPrestigePoints());
    }

    /**
     * Verifies the resolution when the player does not meet the required number of Artists.
     * The player should suffer the penalty and receive no rewards.
     */
    @Test
    void testCavePaintingsFailure() {
        // Required: 3 Artists. Penalty: 5 PP. Reward: 2 PP per Artist
        CavePaintingsEventCard event = new CavePaintingsEventCard(Era.Era1, "CP2", true, 3, 5, 2);

        // Give the player 10 initial PP to test the subtraction clearly
        Player player = new Player("P1", null, new Tribe(), 0, 10);

        // Add only 1 artist to the tribe (Requirement failed)
        new ArtistCard(Era.Era1, "A1").applyTo(player);

        event.resolveEvent(List.of(player));

        // Penalty applied: loses 5 PP. Expected: 10 - 5 = 5 PP
        assertEquals(5, player.getPrestigePoints());
    }
}
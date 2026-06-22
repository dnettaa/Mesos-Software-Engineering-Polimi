package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.player.Tribe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link CavePaintingsEventCard}, verifying artist requirements, rewards,
 * and penalties.
 *
 * @author Diana
 */
class CavePaintingsEventCardTest {

    /**
     * Verifies that a player meeting the artist requirement gains the painting reward.
     * Setup: a cave-paintings event requires two artists and the player has three.
     * Action: resolve the event.
     * Expected behavior: the player gains nine prestige points.
     * Edge case covered: reward uses actual artist count when the requirement is met.
     */
    @Test
    void resolveEventShouldRewardPlayerMeetingArtistRequirement() {
        CavePaintingsEventCard event = new CavePaintingsEventCard(Era.Era1, "CP1", true, 2, 2, 3);
        Player player = new Player("P1", TotemColor.RED, new Tribe(), 0, 0);
        new ArtistCard(Era.Era1, "A1").applyTo(player);
        new ArtistCard(Era.Era1, "A2").applyTo(player);
        new ArtistCard(Era.Era1, "A3").applyTo(player);

        event.resolveEvent(List.of(player));

        assertEquals(9, player.getPrestigePoints());
    }

    /**
     * Verifies that a player below the artist requirement receives the penalty.
     * Setup: a cave-paintings event requires three artists and the player has one plus ten prestige points.
     * Action: resolve the event.
     * Expected behavior: the player loses five prestige points and receives no reward.
     * Edge case covered: failure path subtracts prestige from an existing positive score.
     */
    @Test
    void resolveEventShouldPenalizePlayerBelowArtistRequirement() {
        CavePaintingsEventCard event = new CavePaintingsEventCard(Era.Era1, "CP2", true, 3, 5, 2);
        Player player = new Player("P1", TotemColor.RED, new Tribe(), 0, 10);
        new ArtistCard(Era.Era1, "A1").applyTo(player);

        event.resolveEvent(List.of(player));

        assertEquals(5, player.getPrestigePoints());
    }
}

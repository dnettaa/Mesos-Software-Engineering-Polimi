package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

/**
 * Represents the Cave Paintings event.
 * During this event, each player is evaluated based on the number of
 * Artist characters in their tribe.
 * Players with fewer Artists than the required threshold lose prestige points,
 * while players meeting or exceeding the requirement gain prestige points
 * for each Artist they own.
 * Building cards may grant additional food bonuses for each Artist.
 *
 * @author Andrea Markvukaj
 */

public class CavePaintingsEventCard extends EventCard {

    private final int requiredArtists;
    private final int rewardPerArtist;
    private final int prestigePenalty;

    public CavePaintingsEventCard(Era era, String id, boolean isFinal, int requiredArtists, int prestigePenalty, int rewardPerArtist) {

        super(era, id, isFinal);
        this.requiredArtists = requiredArtists;
        this.prestigePenalty = prestigePenalty;
        this.rewardPerArtist = rewardPerArtist;
    }

    /**
     * Resolves the Cave Paintings event for all players.
     * Each player either gains or loses prestige points depending on whether
     * they meet the required number of Artist characters.
     * Additional food bonuses provided by Building cards are also applied
     * based on the number of Artists.
     *
     * @param players the list of players affected by the event
     */
    @Override
    public void resolveEvent(List<Player> players) {

        for (Player p : players) {

            int artists = p.getTribe().countByType(CharacterType.ARTIST);

            int bonusFood = 0;

            for (BuildingCard b : p.getTribe().getBuildings()) {
                bonusFood += artists * b.getPaintingsBonusFood();
            }

            if (artists < requiredArtists) {
                p.losePP(prestigePenalty);
            } else {
                p.addPP(artists * rewardPerArtist);
            }

            if (bonusFood > 0) {
                p.addFood(bonusFood);
            }
        }
    }
}

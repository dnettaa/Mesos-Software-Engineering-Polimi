package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

/**
 * Represents the Hunt event.
 * During this event, each player gains food and prestige points
 * based on the number of Hunter characters in their tribe.
 * Additional bonuses may be granted by Building cards.
 *
 * @author Andrea Markvukaj
 */

public class HuntEventCard extends EventCard {

    private final int prestigeReward;

    public HuntEventCard(Era era, String id, boolean isFinal, int prestigeReward) {
        super(era, id, isFinal);
        this.prestigeReward = prestigeReward;
    }

    /**
     * Resolves the Hunt event for all players.
     * Each player gains 1 food and a fixed amount of prestige points
     * for each Hunter in their tribe.
     * Additional bonuses provided by Building cards are also applied.
     *
     * @param players the list of players affected by the event
     */
    @Override
    public void resolveEvent(List<Player> players) {

        for (Player p : players) {

            int hunters = p.getTribe().countByType(CharacterType.HUNTER);
            int food = hunters;
            int pp = hunters * prestigeReward;

            for (BuildingCard b : p.getTribe().getBuildings()) {
                food += hunters * b.getHunterBonusFood();
                pp += hunters * b.getHuntBonusPP();
            }

            p.addFood(food);
            p.addPP(pp);
        }
    }
}


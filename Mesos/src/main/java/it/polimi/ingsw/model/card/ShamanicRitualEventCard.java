package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

/**
 * Represents the Shamanic Ritual event.
 * During this event, players compare the total number of Shaman icons
 * in their tribe.
 * Players with the highest number of icons gain prestige points,
 * while players with the lowest number lose prestige points.
 * In case of ties, the effect applies to all tied players.
 * Building cards may modify the number of icons, double the reward
 * for the majority, or prevent the minority penalty.
 *
 * @author Andrea Markvukaj
 */
public class ShamanicRitualEventCard extends EventCard {

    private final int majorityReward;
    private final int minorityPenalty;

    public ShamanicRitualEventCard(Era era, String id, boolean isFinal, int majorityReward, int minorityPenalty) {
        super(era, id, isFinal);
        this.majorityReward = majorityReward;
        this.minorityPenalty = minorityPenalty;
    }

    /**
     * Resolves the Shamanic Ritual event for all players.
     * The total number of Shaman icons is calculated for each player,
     * including bonuses provided by Building cards.
     * Players with the highest value receive a reward, which may be doubled
     * by certain Building cards.
     * Players with the lowest value receive a penalty, unless prevented by a Building effect.
     *
     * @param players the list of players affected by the event
     */
    @Override
    public void resolveEvent(List<Player> players) {

        int[] icons = new int[players.size()];

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            int totalIcons = p.getTribe().countShamanIcons();

            for (BuildingCard b : p.getTribe().getBuildings()) {
                totalIcons = totalIcons + b.getBonusShamanIcons();
            }

            icons[i] = totalIcons;
        }

        int max = icons[0];
        int min = icons[0];

        for(int value : icons) {
            if (value > max)
                max = value;
            if (value < min)
                min = value;
        }

        //  REWARD PHASE
        for (int i = 0; i < players.size(); i++) {
            if (icons[i] == max) {

                Player p = players.get(i);
                int reward = majorityReward;

                for (BuildingCard b : p.getTribe().getBuildings()) {
                    if (b.doublesWinnerReward()) {
                        reward *= 2;
                    }
                }

                p.addPP(reward);
            }
        }

        //  PENALTY PHASE
        for (int i = 0; i < players.size(); i++) {
            if (icons[i] == min) {

                Player p = players.get(i);
                boolean avoidsPenalty = false;

                for (BuildingCard b : p.getTribe().getBuildings()) {
                    if (b.avoidsMinorityPenalty()) {
                        avoidsPenalty = true;
                        break;
                    }
                }

                if (!avoidsPenalty) {
                    p.losePP(minorityPenalty);
                }
            }
        }
    }
}

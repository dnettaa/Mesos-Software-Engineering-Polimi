package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

public class ShamanicRitualEventCard extends EventCard {

    private final int majorityReward;
    private final int minorityPenalty;

    public ShamanicRitualEventCard(Era era, String id, boolean isFinal,
                                   int majorityReward, int minorityPenalty) {
        super(era, id, isFinal);
        this.majorityReward = majorityReward;
        this.minorityPenalty = minorityPenalty;
    }

    @Override
    public void resolveEvent(List<Player> players) {
        //calcolo icone per ogni player
        int[] icons = new int[players.size()];

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            int totalIcons = p.getTribe().countShamanIcons();

            for (BuildingCard b : p.getTribe().getBuildings()) {
                totalIcons = totalIcons + b.getBonusShamanIcons();
            }

            icons[i] = totalIcons;
        }
        //trovo max e min
        int max = icons[0];
        int min = icons[0];

        for(int value : icons) {
            if (value > max)
                max = value;
            if (value < min)
                min = value;
        }
        //applico effetti
        for(int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int value = icons[i];
            //chi e' in maggioranza
            if(value == max) {
                int reward = majorityReward;

                for(BuildingCard b : p.getTribe().getBuildings()) {
                    if(b.doublesWinnerReward()) {
                        reward = reward * 2;
                    }
                }

                p.addPP(reward);
            }
            //chi e' in minoranza
            if (value == min) {

                boolean avoidsPenalty = false;

                for (BuildingCard b : p.getTribe().getBuildings()) {
                    if (b.avoidsMinorityPenalty()) {
                        avoidsPenalty = true;
                        break;
                    }
                }
                // se non ha il building si becca la penalita'
                if (!avoidsPenalty) {
                    p.losePP(minorityPenalty);
                }
            }
        }
    }
}

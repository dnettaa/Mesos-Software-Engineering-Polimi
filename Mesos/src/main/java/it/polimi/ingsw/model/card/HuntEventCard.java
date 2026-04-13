package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class HuntEventCard extends EventCard {

    private final int prestigeReward;

    public HuntEventCard(Era era, String id, boolean isFinal, int prestigeReward) {
        super(era, id, isFinal);
        this.prestigeReward = prestigeReward;
    }

    @Override
    public void resolveEvent(List<Player> players) {

        for (Player p : players) {
            // count by type degli hunters
            int hunters = p.getTribe().countByType(CharacterType.HUNTER);

            // cibo e pp base
            int food = hunters;
            int pp = hunters * prestigeReward;

            // bonus dai building
            for (BuildingCard b : p.getTribe().getBuildings()) {
                food += hunters * b.getHunterBonusFood();
                pp += hunters * b.getHuntBonusPP();
            }

            p.addFood(food);
            p.addPP(pp);
        }
    }
}


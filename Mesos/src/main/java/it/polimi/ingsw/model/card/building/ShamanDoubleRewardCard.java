package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that doubles the reward obtained when the player
 * has the highest number of Shaman icons during the Shamanic Ritual event.
 *
 * @author Andrea Markvukaj
 */
public class ShamanDoubleRewardCard extends BuildingCard {

    public ShamanDoubleRewardCard(Era era, String id, int cost, int prestigePoints) {

        super(era, id, cost, prestigePoints);
    }

    /**
     * Indicates that the reward for having the highest number of Shaman icons is doubled.
     *
     * @return true, this building doubles the reward
     */
    @Override
    public boolean doublesWinnerReward() {

        return true;
    }
}
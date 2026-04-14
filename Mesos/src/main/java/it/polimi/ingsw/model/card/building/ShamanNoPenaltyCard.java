package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that prevents the player from suffering
 * the minority penalty during the Shamanic Ritual event.
 *
 * @author Andrea Markvukaj
 */
public class ShamanNoPenaltyCard extends BuildingCard {

    public ShamanNoPenaltyCard(Era era, String id, int cost, int prestigePoints) {

        super(era, id, cost, prestigePoints);
    }

    /**
     * Indicates that the player is immune to the minority penalty.
     *
     * @return true, this building prevents the penalty
     */
    @Override
    public boolean avoidsMinorityPenalty() {

        return true;
    }
}
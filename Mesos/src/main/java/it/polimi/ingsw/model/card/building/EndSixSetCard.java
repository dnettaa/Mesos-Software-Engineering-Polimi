package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants 6 prestige points for each complete set
 * of 6 different Character types at the end of the game.
 *
 * @author Andrea Markvukaj
 */
public class EndSixSetCard extends BuildingCard {

    private static final int POINTS_PER_SET = 6;

    public EndSixSetCard(Era era, String id, int cost, int prestigePoints) {

        super(era, id, cost, prestigePoints);
    }

    /**
     * Calculates the prestige points bonus based on the number
     * of complete Character sets.
     *
     * @param player the player whose tribe is getting evaluated
     * @return the bonus prestige points
     */
    @Override
    public int calculateEndGameBonus(Player player) {

        int sets = player.getTribe().getFullSetsCount();
        return sets * POINTS_PER_SET;
    }
}
package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants a fixed amount of prestige points
 * at the end of the game.
 *
 * @author Andrea Markvukaj
 */
public class EndBonusCard extends BuildingCard {

    private final int prestigeBonus;

    public EndBonusCard(Era era, String id, int cost, int prestigePoints, int prestigeBonus) {

        super(era, id, cost, prestigePoints);
        this.prestigeBonus = prestigeBonus;
    }

    /**
     * Returns the fixed prestige points bonus provided by this building.
     *
     * @param player the player owning the building
     * @return the bonus prestige points
     */
    @Override
    public int calculateEndGameBonus(Player player) {

        return prestigeBonus;
    }
}
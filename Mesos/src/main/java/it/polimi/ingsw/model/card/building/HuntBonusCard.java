package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants additional food and prestige points
 * for each Hunter during the Hunt event.
 *
 * @author Andrea Markvukaj
 */
public class HuntBonusCard extends BuildingCard {

    private final int foodBonus;
    private final int prestigeBonus;

    public HuntBonusCard(Era era, String id, int cost, int prestigePoints, int foodBonus, int prestigeBonus) {

        super(era, id, cost, prestigePoints);
        this.foodBonus = foodBonus;
        this.prestigeBonus = prestigeBonus;
    }

    /**
     * Returns the food bonus provided by this building for each Hunter.
     *
     * @return the food bonus per Hunter
     */
    @Override
    public int getHunterBonusFood() {
        return foodBonus;
    }

    /**
     * Returns the prestige points bonus provided by this building for each Hunter.
     *
     * @return the prestige points bonus per Hunter
     */
    @Override
    public int getHuntBonusPP() {
        return prestigeBonus;
    }
}
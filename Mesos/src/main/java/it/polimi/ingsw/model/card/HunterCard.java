package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Tribe;
/**
 * Class representing Hunter Card.
 * Hunter Card may grant a food bonus when acquired.
 *
 * @author Andrea Markvukaj
 */
public class HunterCard extends CharacterCard{

    private final boolean hunterFoodBonus;

    public HunterCard(Era era, String id, boolean hunterFoodBonus) {
        super(era, id);
        this.hunterFoodBonus = hunterFoodBonus;
    }
    /**
     * Checks if this specific HunterCard has food bonus.
     * If the hunterFoodBonus flag is true, the player gains a bonus amount (1) of food.
     *
     * @param tribe the tribe acquiring the card
     * @return food amount gained
     */
    @Override
    public int getFoodOnAcquired(Tribe tribe) {
        if(hunterFoodBonus) {
            return 1;
        }
        return 0;
    }

    @Override
    public CharacterType getType() {
        return CharacterType.HUNTER;
    }
}

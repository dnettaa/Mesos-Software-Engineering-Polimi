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
     * If this card has the food bonus enabled, the player gains a number of food
     * equal to the total number of Hunter characters currently in the tribe
     * (including this card).
     *
     * @param tribe the tribe acquiring the card
     * @return food amount gained
     */
    @Override
    public int getFoodOnAcquired(Tribe tribe) {
        if(!hunterFoodBonus) {
            return 0;
        }

        return tribe.countByType(CharacterType.HUNTER);
    }

    @Override
    public CharacterType getType() {
        return CharacterType.HUNTER;
    }
}

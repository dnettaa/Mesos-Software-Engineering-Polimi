package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.player.Tribe;

/**
 * Abstract class representing a character card.
 * Character cards provide immediate or passive effects,
 * and belong to a specific CharacterType.
 *
 * @author Andrea Markvukaj
 */
public abstract class CharacterCard extends TribeCard {

    public CharacterCard(Era era, String id) {
        super(era, id);
    }

    /**
     * Returns the amount of food gained when this card is acquired.
     * Default implementation returns 0, only HunterCard
     * returns an actual amount of food.
     *
     * @param tribe the player's tribe
     * @return the amount of food gained
     */
    public int getFoodOnAcquired(Tribe tribe) {
        return 0;
    }

    /**
     * Returns the type of the character (SHAMAN, HUNTER, ...).
     * See enum class <<CharacterType>>.
     *
     * @return the character type
     */
    public abstract CharacterType getType();
}

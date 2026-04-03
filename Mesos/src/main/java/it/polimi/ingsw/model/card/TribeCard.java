package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;

/**
 * Abstract class representing a card that belongs to the tribe deck.
 * This includes CharacterCard and EventCard.
 *
 * @author Andrea Markvukaj
 */
public abstract class TribeCard extends Card {

    protected TribeCard(Era era, String id) {
        super(era, id);
    }

}

package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.board.CardRow;
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

    /**
     * Removes this tribe card from the given card row.
     * The card is removed directly from the internal tribe-card list
     * of the row.
     *
     * @param row the card row from which this card must be removed
     * @throws IllegalArgumentException if the card is not present in the row
     */
    @Override
    public void removeFrom(CardRow row) {
        if(!row.getTribeCardsInternal().remove(this)) {
            throw new IllegalArgumentException("Tribe card is not present in this row");
        }
    }

}

package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
/**
 * Class representing Gatherer Card.
 *
 * @author Andrea Markvukaj
 */
public class GathererCard extends CharacterCard {

    public GathererCard(Era era, String id) {
        super(era, id);
    }

    @Override
    public CharacterType getType() {
        return CharacterType.GATHERER;
    }
}

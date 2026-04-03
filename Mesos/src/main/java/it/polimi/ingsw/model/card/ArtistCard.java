package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;

/**
 * Class representing Artist Card.
 *
 * @author Andrea Markvukaj
 */
public class ArtistCard extends CharacterCard{

    public ArtistCard(Era era, String id)
    {
        super(era, id);
    }

    @Override
    public CharacterType getType() {
        return CharacterType.ARTIST;
    }
}

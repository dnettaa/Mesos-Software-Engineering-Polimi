package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.player.Tribe;

public abstract class CharacterCard extends TribeCard {

    public CharacterCard(Era era, String id) {
        super(era, id);
    }

    public int getFoodOnAcquired(Tribe tribe) {
        return 0;
    }
}

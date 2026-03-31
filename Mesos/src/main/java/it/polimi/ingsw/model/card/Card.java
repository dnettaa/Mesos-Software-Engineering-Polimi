package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;

/**
 * Abstract base class representing a generic card in the game.
 * Each card belongs to an era and has a unique id.
 * This class is extended by all specific card types in the game.
 *
 * @author Andrea Markvukaj
 */
public abstract class Card {

    protected Era era;
    protected String id;

    public Card(Era era, String id) {
        this.era = era;
        this.id = id;
    }

    public Era getEra() {
        return era;
    }

    public String getId() {
        return id;
    }
}

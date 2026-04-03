package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;

/**
 * Abstract base class representing a generic card in the game.
 * Each card belongs to an era and has a unique id.
 * This class is extended by all specific card types in the game.
 *
 * @author Andrea Markvukaj
 */
public abstract class Card {

    private final Era era;
    private final String id;

    protected Card(Era era, String id) {
        this.era = era;
        this.id = id;
    }

    public Era getEra() {
        return era;
    }

    public String getId() {
        return id;
    }

    public boolean isPickable() {
        return true;
    }

    public abstract void applyTo(Player player);

    public int getCostFor(Player player) {
        return 0;
    }

}   

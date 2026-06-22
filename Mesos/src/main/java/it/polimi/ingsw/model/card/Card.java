package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.board.CardRow;
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

    /**
     * Indicates whether this card can be picked by a player.
     * Subclasses can override this method to specify the card is a pickable card.
     *
     * @return true if the card is pickable, false otherwise
     */
    public boolean isPickable() {
        return true;
    }

    /**
     * Applies the effect of the card to the specified player.
     * The behavior depends on the card.
     *
     * @param player the player getting the effect
     */
    public abstract void applyTo(Player player);

    /**
     * Returns the cost required for the specified player to acquire this card.
     * Default implementation returns 0, only building card require spending food.
     *
     * @param player the player attempting to acquire the card
     * @return the cost of the card for that player
     */
    public int getCostFor(Player player) {
        return 0;
    }

    public int getBuilderDiscount() {
        return 0;
    }

    /**
     * Removes this card from the given card row.
     * The specific removal logic depends on the concrete type of the card
     * and is implemented by subclasses using polymorphism.
     * This avoids type checks (e.g., instanceof) in the CardRow class.
     *
     * @param row the card row from which this card must be removed
     * @throws IllegalArgumentException if the card is not present in the row
     */
    public abstract void removeFrom(CardRow row);

}   

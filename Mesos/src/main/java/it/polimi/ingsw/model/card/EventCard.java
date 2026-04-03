package it.polimi.ingsw.model.card;
import java.util.List;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Abstract class representing an event card.
 * Event cards trigger effects that are applied to all players.
 *
 * @author Andrea Markvukaj
 */
public abstract class EventCard extends TribeCard {

    private final boolean isFinal;

    protected EventCard(Era era, String id, boolean isFinal) {

        super(era, id);
        this.isFinal = isFinal;
    }

    /**
     * Indicates whether this event is a final event.
     *
     * @return true if the event is final, false otherwise.
     */
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    /**
     * Event cards have no cost.
     */
    @Override
    public int getCostFor(Player player) {
        return 0;
    }

    /**
     * Resolves the event's effect on all players.
     * Each subclass will implement its own resolving event logic by overriding this method.
     *
     * @param players the list of players
     */
    public abstract void resolveEvent(List<Player> players);

}
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

    private boolean isFinal;

    protected EventCard(Era era, String id) {

        super(era, id);
    }

    /**
     * Indicates whether this event is a final event.
     *
     * @return true if the event is final, false otherwise.
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Resolves the event's effect on all players.
     * Each subclass will implement its own resolving event logic by overriding this method.
     *
     * @param players the list of players
     */
    public abstract void resolveEvent(List<Player> players);
}
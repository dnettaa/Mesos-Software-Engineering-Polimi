package it.polimi.ingsw.model.card;

import java.util.List;
import it.polimi.ingsw.model.player.Player;

public abstract class EventCard extends TribeCard {

    protected boolean isFinal;

    public EventCard(Era era, String id) {

        super(era, id);
    }

    public boolean isFinal() {
        return isFinal;
    }
    // Each event will implement its own resolving event logic by overriding this method
    public abstract void resolveEvent(List<Player> players);

}
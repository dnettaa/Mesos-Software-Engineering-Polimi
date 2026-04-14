package it.polimi.ingsw.model.game.state;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.card.*;

import java.util.List;

public class EventResolutionPhase implements Phase {

    @Override
    public void resolveEvents(Game game) {
        game.validateState();

        List<EventCard> events = game.getBoard().getLowerRowEvents();
        if (game.getCurrentRound() == 10) {
            events.addAll(game.getBoard().getUpperRowEvents());
        }

        events.sort((a, b) -> {
            int cmp = Boolean.compare(a.isFinal(), b.isFinal());
            if (cmp != 0) return cmp;
            boolean aIsSustenance = a instanceof SustenanceEventCard;
            boolean bIsSustenance = b instanceof SustenanceEventCard;
            cmp = Boolean.compare(aIsSustenance, bIsSustenance);
            if (cmp != 0) return cmp;
            return a.getEra().compareTo(b.getEra());
        });

        for (EventCard e : events) {
            e.resolveEvent(game.getPlayers());
        }

        game.setCurrentPhase(new EndRoundPhase());
    }
}
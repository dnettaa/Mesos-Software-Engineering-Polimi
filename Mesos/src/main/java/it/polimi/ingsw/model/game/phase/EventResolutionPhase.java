package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.card.*;
import java.util.ArrayList;

import java.util.List;

/**
 * Phase in which event cards are resolved. Events are resolved
 * in Era order, with Sustenance events always resolved last
 * within each Era. On the last round, events from the upper
 * row are also resolved. Transitions to EndRoundPhase.
 *
 * @author Luca Grecchi
 */
public class EventResolutionPhase implements Phase {

    /**
     * Resolves all visible event cards. Collects events from the lower row,
     * and from the upper row if it's the last round. Events are separated
     * into normal and sustenance lists, sorted by Era, and resolved
     * in order with sustenance last for each Era.
     *
     * @param game the game instance
     */
    @Override
    public void resolveEvents(Game game) {
        game.validateState();

        List<EventCard> events = game.getBoard().getLowerRowEvents();
        if (game.getCurrentRound() == 10) {
            events.addAll(game.getBoard().getUpperRowEvents());
        }

        List<EventCard> normal = new ArrayList<>();
        List<EventCard> sustenance = new ArrayList<>();

        for (EventCard e : events) {
            e.addToList(normal, sustenance);
        }

        normal.sort((a, b) -> {
            int cmp = a.getEra().compareTo(b.getEra());
            if (cmp != 0) return cmp;
            return Boolean.compare(a.isFinal(), b.isFinal());
        });

        sustenance.sort((a, b) -> {
            int cmp = a.getEra().compareTo(b.getEra());
            if (cmp != 0) return cmp;
            return Boolean.compare(a.isFinal(), b.isFinal());
        });

        for (Era era : Era.values()) {
            for (EventCard e : normal) {
                if (e.getEra() == era) e.resolveEvent(game.getPlayers());
            }
            for (EventCard e : sustenance) {
                if (e.getEra() == era) e.resolveEvent(game.getPlayers());
            }
        }

        game.setCurrentPhase(new EndRoundPhase());
    }
}
package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.player.Player;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, Integer> initialPPByPlayer = new HashMap<>(Map.of());
        Map<String, Integer> initialFoodByPlayer = new HashMap<>(Map.of());
        for(Player p: game.getPlayers()){
            initialPPByPlayer.put(p.getNickname(), p.getPrestigePoints());
            initialFoodByPlayer.put(p.getNickname(), p.getFood());
        }

        for (Era era : Era.values()) {
            for (EventCard e : normal) {
                if (e.getEra() == era) resolveAndFire(game, e, initialPPByPlayer, initialFoodByPlayer);
            }
            for (EventCard e : sustenance) {
                if (e.getEra() == era) resolveAndFire(game, e, initialPPByPlayer, initialFoodByPlayer);
            }
        }



        game.setCurrentPhase(new EndRoundPhase());
        game.endRound();
    }

    private void resolveAndFire(Game game, EventCard e, Map<String, Integer> initialPP, Map<String, Integer> initialFood) {
        e.resolveEvent(game.getPlayers());

        Map<String, Integer> ppDelta = new HashMap<>();
        Map<String, Integer> foodDelta = new HashMap<>();
        for (Player p : game.getPlayers()) {
            ppDelta.put(p.getNickname(), p.getPrestigePoints() - initialPP.get(p.getNickname()));
            foodDelta.put(p.getNickname(), p.getFood() - initialFood.get(p.getNickname()));
            initialPP.put(p.getNickname(), p.getPrestigePoints());
            initialFood.put(p.getNickname(), p.getFood());
        }

        game.fireEventResolved(new EventResolvedDTO(e.getId(), e.getClass().getSimpleName(), ppDelta, foodDelta, game.getCurrentPhaseName()));
    }
}
package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Card;

import java.util.List;

public interface Phase {
    default void placeTotem(Game game, Player player, char slotID) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    default void takeCards(Game game, Player player, List<Card> upper, List<Card> lower) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    default void takeExtraCard(Game game, Player player, Card card) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    default void resolveEvents(Game game) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    default void endRound(Game game) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    default void endGame(Game game) {
        throw new IllegalStateException("Invalid action for current phase");
    }
}
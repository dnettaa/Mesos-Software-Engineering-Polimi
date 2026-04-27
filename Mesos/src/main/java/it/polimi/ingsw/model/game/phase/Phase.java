package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Card;

import java.util.List;

/**
 * Interface representing a game phase using the State pattern.
 * Each phase overrides only the actions valid during that phase.
 * Invalid actions throw IllegalStateException by default.
 *
 * @author Luca Grecchi
 */
public interface Phase {

    /**
     * Places a player's totem on an offer slot.
     * Valid only during TotemPlacementPhase.
     *
     * @param game the game instance
     * @param player the player placing the totem
     * @param slotID the chosen offer slot ID
     * @throws IllegalStateException if called during the wrong phase
     */
    default void placeTotem(Game game, Player player, char slotID) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    /**
     * Takes cards from the upper and lower rows.
     * Valid only during OfferResolutionPhase.
     *
     * @param game the game instance
     * @param player the player taking cards
     * @param upper cards chosen from the upper row
     * @param lower cards chosen from the lower row
     * @throws IllegalStateException if called during the wrong phase
     */
    default void takeCards(Game game, Player player, List<Card> upper, List<Card> lower) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    /**
     * Takes an extra card from the upper row.
     * Valid only during ExtraCardPhase.
     *
     * @param game the game instance
     * @param player the player taking the extra card
     * @param card the chosen card
     * @throws IllegalStateException if called during the wrong phase
     */
    default void takeExtraCard(Game game, Player player, Card card) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    /**
     * Resolves all event cards.
     * Valid only during EventResolutionPhase.
     *
     * @param game the game instance
     * @throws IllegalStateException if called during the wrong phase
     */
    default void resolveEvents(Game game) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    /**
     * Ends the current round and sets up the next one.
     * Valid only during EndRoundPhase.
     *
     * @param game the game instance
     * @throws IllegalStateException if called during the wrong phase
     */
    default void endRound(Game game) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    /**
     * Calculates final scoring and ends the game.
     * Valid only during EndGamePhase.
     *
     * @param game the game instance
     * @throws IllegalStateException if called during the wrong phase
     */
    default void endGame(Game game) {
        throw new IllegalStateException("Invalid action for current phase");
    }

    default String getCurrentPlayerNickname(Game game){
        return null;
    }
}
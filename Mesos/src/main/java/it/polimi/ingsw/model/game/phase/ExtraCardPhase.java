package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Card;

/**
 * Phase in which the player with the ExtraPick building
 * can take an additional card from the upper row.
 * Transitions to EventResolutionPhase after the card is taken.
 *
 * @author Luca Grecchi
 */
public class ExtraCardPhase implements Phase {

    /**
     * Allows the player with the ExtraPick building to take an extra card
     * from the upper row. Validates the player, the card selection,
     * and that the player has enough food if it's a building card.
     *
     * @param game the game instance
     * @param player the player taking the extra card
     * @param card the chosen card from the upper row
     * @throws IllegalArgumentException if wrong player, invalid card, or not enough food
     */
    @Override
    public void takeExtraCard(Game game, Player player, Card card) {
        game.validateState();

        if (!player.equals(game.getPlayers().get(game.getCurrentPlayerIndex()))) {
            throw new IllegalArgumentException("Wrong player!");
        }

        if (!card.isPickable() || !game.getBoard().getUpperRowCards().contains(card)) {
            throw new IllegalArgumentException("Wrong chosen card");
        }

        if (card.getCostFor(player) > player.getFood()) {
            throw new IllegalArgumentException("Not enough food");
        }

        card.applyTo(player);
        game.getBoard().removeCardFromUpper(card);

        game.setCurrentPhase(new EventResolutionPhase());
        game.resolveEvents();
    }

    @Override
    public String getCurrentPlayerNickname(Game game){
        return game.getPlayers().get(game.getCurrentPlayerIndex()).getNickname();
    }
}
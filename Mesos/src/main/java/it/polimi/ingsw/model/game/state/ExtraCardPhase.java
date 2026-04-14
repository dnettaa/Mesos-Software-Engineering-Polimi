package it.polimi.ingsw.model.game.state;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Card;

public class ExtraCardPhase implements Phase {

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
    }
}
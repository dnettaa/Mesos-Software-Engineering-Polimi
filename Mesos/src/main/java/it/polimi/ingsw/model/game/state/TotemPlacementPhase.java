package it.polimi.ingsw.model.game.state;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Card;

public class TotemPlacementPhase implements Phase {

    @Override
    public void placeTotem(Game game, Player player, char slotID) {

        game.validateState();
        game.validateActivePlayerTotemPlacement(player);

        game.getBoard().placeTotemOnOffer(player, slotID);
        game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);

        if(game.getCurrentPlayerIndex() == game.getPlayers().size()){
            game.setResolutionOrder(game.getBoard().getOfferResolutionOrder());
            game.setCurrentPlayerIndex(0);
            game.setCurrentPhase(new OfferResolutionPhase());
        }
    }
}

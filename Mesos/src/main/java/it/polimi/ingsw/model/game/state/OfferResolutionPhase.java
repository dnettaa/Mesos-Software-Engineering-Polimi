package it.polimi.ingsw.model.game.state;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.BuildingCard;

import java.util.List;

public class OfferResolutionPhase implements Phase {

    @Override
    public void takeCards(Game game, Player player, List<Card> chosenUpper, List<Card> chosenLower) {

        game.validateState();
        game.validateActivePlayerOfferResolution(player);
        game.validateChosenCards(player, chosenUpper, chosenLower);

        for (Card upperCard : chosenUpper) {
            game.getBoard().removeCardFromUpper(upperCard);
            upperCard.applyTo(player);
        }
        for (Card lowerCard : chosenLower) {
            game.getBoard().removeCardFromLower(lowerCard);
            lowerCard.applyTo(player);
        }

        game.getBoard().returnTotemToTurnOrder(player);
        game.applyTurnOrderBonus(player);

        game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);

        if (game.getCurrentPlayerIndex() == game.getPlayers().size()) {
            game.setCurrentPlayerIndex(0);

            for (Player p : game.getPlayers()) {
                for (BuildingCard b : p.getTribe().getBuildings()) {
                    if (b.requiresExtraCardPhase()) {
                        game.setCurrentPlayerIndex(game.getPlayers().indexOf(p));
                        game.setCurrentPhase(new ExtraCardPhase());
                        return;
                    }
                }
            }

            game.setCurrentPhase(new EventResolutionPhase());
        }
    }
}
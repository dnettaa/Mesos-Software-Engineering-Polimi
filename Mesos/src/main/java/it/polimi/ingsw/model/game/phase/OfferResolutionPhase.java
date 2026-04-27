package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.BuildingCard;

import java.util.List;

/**
 * Phase in which players resolve their offer actions by taking
 * cards from the upper and lower rows. Players act in order
 * from left to right on the offer track. When all players have
 * resolved, transitions to ExtraCardPhase if applicable,
 * otherwise to EventResolutionPhase.
 *
 * @author Luca Grecchi
 */
public class OfferResolutionPhase implements Phase {

    /**
     * Resolves the current player's offer action. Applies the food reward
     * if the slot provides one, then applies chosen cards to the player.
     * Returns the totem to the turn order track and applies the turn order bonus.
     * When all players have resolved, checks if any player has an ExtraPick
     * building to determine the next phase.
     *
     * @param game the game instance
     * @param player the player taking cards
     * @param chosenUpper cards chosen from the upper row
     * @param chosenLower cards chosen from the lower row
     */
    @Override
    public void takeCards(Game game, Player player, List<Card> chosenUpper, List<Card> chosenLower) {

        game.validateState();
        game.validateActivePlayerOfferResolution(player);
        game.validateChosenCards(player, chosenUpper, chosenLower);

        int foodReward = game.getResolutionOrder().get(game.getCurrentPlayerIndex()).getFoodReward();
        if (foodReward > 0) {
            player.addFood(foodReward);
        }

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
            game.resolveEvents();
        }
    }

    @Override
    public String getCurrentPlayerNickname(Game game){
        return game.getResolutionOrder().get(game.getCurrentPlayerIndex()).getOccupant().getNickname();
    }
}
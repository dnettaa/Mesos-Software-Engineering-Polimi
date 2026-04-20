package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;

/**
 * Phase in which players place their totems on offer slots
 * following the turn order. When all players have placed,
 * transitions to OfferResolutionPhase.
 *
 * @author Luca Grecchi
 */
public class TotemPlacementPhase implements Phase {

    /**
     * Places a player's totem on the specified offer slot.
     * Validates the game state and the active player.
     * When all players have placed, prepares the resolution order
     * and transitions to OfferResolutionPhase.
     *
     * @param game the game instance
     * @param player the player placing the totem
     * @param slotID the ID of the chosen offer slot
     */
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

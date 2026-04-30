package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Card;

/**
 * Phase in which the player with the ExtraPick building
 * can take an additional card from the upper row.
 * Transitions to {@link EventResolutionPhase} after the card is taken.
 *
 * @author Luca Grecchi
 */ class ExtraCardPhase implements Phase {

    /**
     * Allows the player with the ExtraPick building to take an extra card
     * from the upper row. Validates the player's turn, the card's availability,
     * and that the player has enough food to pay for it.
     *
     * @param game the game instance
     * @param player the player taking the extra card
     * @param card the chosen card from the upper row
     * @throws GameException with {@link ErrorCode#NOT_YOUR_TURN} if it is not the provided player's turn
     * @throws GameException with {@link ErrorCode#CARD_NOT_IN_ROW} if the chosen card is not pickable or not in the upper row
     * @throws GameException with {@link ErrorCode#INSUFFICIENT_FOOD} if the player does not have enough food to pay for the card
     */
    @Override
    public void takeExtraCard(Game game, Player player, Card card) {
        game.validateState();

        if (!player.equals(game.getPlayers().get(game.getCurrentPlayerIndex()))) {
            throw new GameException(ErrorCode.NOT_YOUR_TURN, "Wrong player!");
        }

        if (!card.isPickable() || !game.getBoard().getUpperRowCards().contains(card)) {
            throw new GameException(ErrorCode.CARD_NOT_IN_ROW, "Card not available in upper row");
        }

        if (card.getCostFor(player) > player.getFood()) {
            throw new GameException(ErrorCode.INSUFFICIENT_FOOD, "Not enough food");
        }

        int initialFood = player.getFood();
        card.applyTo(player);
        int foodDelta = player.getFood() - initialFood;

        game.getBoard().removeCardFromUpper(card);

        ExtraCardTakenDTO dto = new ExtraCardTakenDTO(
                                    player.getNickname(),
                                    card.getId(),
                                    true,
                                    card instanceof BuildingCard,
                                    foodDelta
        );

        game.fireExtraCardTaken(dto);

        game.setCurrentPhase(new EventResolutionPhase());
        game.resolveEvents();
    }

    /**
     * Returns the nickname of the player who is currently taking their extra card.
     *
     * @param game the game instance
     * @return the nickname of the active player
     */
    @Override
    public String getCurrentPlayerNickname(Game game){
        return game.getPlayers().get(game.getCurrentPlayerIndex()).getNickname();
    }
}
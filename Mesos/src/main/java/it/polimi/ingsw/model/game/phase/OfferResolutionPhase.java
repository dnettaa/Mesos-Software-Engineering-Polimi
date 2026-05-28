package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.building.BuildingCard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * After the logic is executed, calculates deltas and fires a notification.
     *
     * @param game the game instance
     * @param player the player taking cards
     * @param chosenUpper cards chosen from the upper row
     * @param chosenLower cards chosen from the lower row
     */
    @Override
    public void takeCards(Game game, Player player, List<Card> chosenUpper, List<Card> chosenLower, List<Card> orderedCards) {

        game.validateState();
        game.validateActivePlayerOfferResolution(player);
        game.validateChosenCards(player, chosenUpper, chosenLower);

        int initialFood = player.getFood();
        int initialPP = player.getPrestigePoints();
        char freedSlotID = game.getResolutionOrder().get(game.getCurrentPlayerIndex()).getSlotID();

        int foodReward = game.getResolutionOrder().get(game.getCurrentPlayerIndex()).getFoodReward();
        if (foodReward > 0) {
            player.addFood(foodReward);
        }

        for (Card upperCard : chosenUpper) game.getBoard().removeCardFromUpper(upperCard);
        for (Card lowerCard : chosenLower) game.getBoard().removeCardFromLower(lowerCard);
        for (Card card : orderedCards) card.applyTo(player);

        if (game.getCurrentRound() < 10) {
            game.getBoard().returnTotemToTurnOrder(player);
            game.applyTurnOrderBonus(player);
        }

        game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);

        boolean triggerEventCascade = false;

        if (game.getCurrentPlayerIndex() == game.getPlayers().size()) {
            game.setCurrentPlayerIndex(0);
            boolean nextPhaseSet = false;

            for (Player p : game.getPlayers()) {
                for (BuildingCard b : p.getTribe().getBuildings()) {
                    if (b.requiresExtraCardPhase()) {
                        game.setCurrentPlayerIndex(game.getPlayers().indexOf(p));
                        game.setCurrentPhase(new ExtraCardPhase());
                        nextPhaseSet = true;
                        break;
                    }
                }
                if (nextPhaseSet) break;
            }

            if (!nextPhaseSet) {
                game.setCurrentPhase(new EventResolutionPhase());
                triggerEventCascade = true;
            }
        }

            int foodDelta = player.getFood() - initialFood;
            int ppDelta = player.getPrestigePoints() - initialPP;
            int turnOrderPosition = game.getBoard().getTurnOrderTrack().getPlayersInOrder().indexOf(player);

            List<String> upperIDs = chosenUpper.stream().map(Card::getId).toList();
            List<String> lowerIDs = chosenLower.stream().map(Card::getId).toList();

            List<Card> allChosen = new ArrayList<>(chosenUpper);
            allChosen.addAll(chosenLower);

            List<String> addedBuildings = allChosen.stream()
                            .filter(c -> c instanceof BuildingCard)
                            .map(Card::getId)
                            .toList();

            List<String> addedTribeCards = allChosen.stream()
                            .filter(c -> !(c instanceof BuildingCard))
                            .map(Card::getId)
                            .toList();

            String nextPlayer = triggerEventCascade ? null : game.getCurrentPlayerNickname();

            CardsTakenDTO dto = new CardsTakenDTO(
                    player.getNickname(),
                    upperIDs,
                    lowerIDs,
                    addedTribeCards,
                    addedBuildings,
                    foodDelta,
                    ppDelta,
                    freedSlotID,
                    turnOrderPosition,
                    nextPlayer,
                    game.getCurrentPhaseName()
            );
            game.fireCardsTaken(dto);

            if(triggerEventCascade){
                game.resolveEvents();
            }
    }

    /**
     * Returns the nickname of the player who is currently taking cards.
     *
     * @param game the game instance
     * @return the nickname of the active player
     */
    @Override
    public String getCurrentPlayerNickname(Game game){
        return game.getResolutionOrder().get(game.getCurrentPlayerIndex()).getOccupant().getNickname();
    }
}
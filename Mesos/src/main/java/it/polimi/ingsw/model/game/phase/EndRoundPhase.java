package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase that ends the current round. Sets up the board for the
 * next round if it's not the last one, otherwise transitions
 * to EndGamePhase.
 *
 * @author Luca Grecchi
 */
public class EndRoundPhase implements Phase {


    /**
     * Ends the current round. If not the last round, sets up the board
     * for the next round, updates the placement order, and transitions
     * to TotemPlacementPhase. If it's the last round, transitions
     * to EndGamePhase.
     *
     * @param game the game instance
     */
    @Override
    public void endRound(Game game) {
        game.validateState();

        if (game.getCurrentRound() < 10) {

            int newRound = game.getCurrentRound() + 1;
            List<String> newTurnOrder = game.getBoard().getTurnOrderTrack()
                    .getPlayersInOrder()
                    .stream()
                    .map(Player::getNickname)
                    .toList();
            String firstPlayer = newTurnOrder.isEmpty() ? null : newTurnOrder.getFirst();

            RoundEndedDTO dto = game.getBoard().setupNewRound(newRound, newTurnOrder, firstPlayer);

            game.setPlacementOrder(new ArrayList<>(game.getBoard().getPlacementOrder()));
            game.setCurrentPlayerIndex(0);
            game.setCurrentRound(newRound);
            game.setCurrentPhase(new TotemPlacementPhase());

            game.fireRoundEnded(dto);
        } else {
            Phase next = new EndGamePhase();
            game.setCurrentPhase(next);
            next.endGame(game);
        }
    }
}
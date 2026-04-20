package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;

import java.util.ArrayList;

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
            game.getBoard().setupNewRound();
            game.setPlacementOrder(new ArrayList<>(game.getBoard().getPlacementOrder()));
            game.setCurrentPlayerIndex(0);
            game.setCurrentRound(game.getCurrentRound() + 1);
            game.setCurrentPhase(new TotemPlacementPhase());
        } else {
            game.setCurrentPhase(new EndGamePhase());
        }
    }
}
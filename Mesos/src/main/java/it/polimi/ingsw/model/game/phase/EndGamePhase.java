package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.FinalScoringCalculator;

/**
 * Final phase that calculates end-game scoring for all players
 * and sets the game state to Finished.
 *
 * @author Luca Grecchi
 */
public class EndGamePhase implements Phase {

    /**
     * Calculates the final scoring through FinalScoringCalculator
     * and transitions the game state to Finished.
     *
     * @param game the game instance
     */
    @Override
    public void endGame(Game game) {
        game.validateState();

        FinalScoringCalculator.calculate(game.getPlayers());
        game.setState(GameState.Finished);
    }
}
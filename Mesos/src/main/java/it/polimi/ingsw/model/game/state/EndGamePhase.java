package it.polimi.ingsw.model.game.state;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.FinalScoringCalculator;

public class EndGamePhase implements Phase {

    @Override
    public void endGame(Game game) {
        game.validateState();

        FinalScoringCalculator.calculate(game.getPlayers());
        game.setState(GameState.Finished);
    }
}
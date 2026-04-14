package it.polimi.ingsw.model.game.state;

import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.player.Player;

import java.util.ArrayList;

public class EndRoundPhase implements Phase {

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
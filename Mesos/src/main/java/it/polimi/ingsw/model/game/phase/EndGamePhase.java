package it.polimi.ingsw.model.game.phase;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.FinalScoringCalculator;
import it.polimi.ingsw.model.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, Integer> initialPPByPlayer = new HashMap<>();
        for(Player p: game.getPlayers()){
            initialPPByPlayer.put(p.getNickname(), p.getPrestigePoints());
        }

        FinalScoringCalculator.calculate(game.getPlayers());

        Map<String, Integer> finalPPByPlayer = new HashMap<>();
        Map<String, Integer> endGameBonusByPlayer = new HashMap<>();
        for(Player p: game.getPlayers()){
            endGameBonusByPlayer.put(p.getNickname(), p.getPrestigePoints() - initialPPByPlayer.get(p.getNickname()));
            finalPPByPlayer.put(p.getNickname(), p.getPrestigePoints());
        }

        List<String> ranking = game.getPlayers().stream()
                .sorted((a, b) -> b.getPrestigePoints() - a.getPrestigePoints())
                .map(Player::getNickname)
                .toList();


        GameEndedDTO dto = new GameEndedDTO(finalPPByPlayer, endGameBonusByPlayer, ranking);
        game.fireGameEnded(dto);
        game.setState(GameState.Finished);
    }
}
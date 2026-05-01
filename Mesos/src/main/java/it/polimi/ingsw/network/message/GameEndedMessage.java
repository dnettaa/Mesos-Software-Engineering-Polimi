package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Message sent by the server to notify clients that the game has ended.
 * <p>
 * This message contains the final ranking and final score of each player.
 * It is sent once, after the final scoring has been completed by the model.
 * @author Diana
 */

public class GameEndedMessage extends ServerMessage{
    private final Map<String, Integer> finalPPByPlayer;
    private final Map<String, Integer> endGameBonusByPlayer;
    private final List<String> ranking;

    /**
     * Creates a new game-ended message.
     *
     * @param finalPPByPlayer the final prestige points for each player nickname
     * @param endGameBonusByPlayer the end-game bonus points for each player nickname
     * @param ranking the final ranking as an ordered list of player nicknames
     */
    public GameEndedMessage(Map<String, Integer> finalPPByPlayer, Map<String, Integer> endGameBonusByPlayer, List<String> ranking){
        this.finalPPByPlayer = new HashMap<>(finalPPByPlayer);
        this.endGameBonusByPlayer = new HashMap<>(endGameBonusByPlayer);
        this.ranking = new ArrayList<>(ranking);
    }


    /**
     * Returns the final score associated with each player.
     *
     * @return a copy of the final scores map
     */
    public Map<String, Integer> getFinalPPByPlayer(){
        return new HashMap<>(finalPPByPlayer);
    }

    public Map<String, Integer> getEndGameBonusByPlayer() {
        return new HashMap<>(endGameBonusByPlayer);
    }

    /**
     * Returns the final ranking.
     *
     * @return a copy of the ranking list
     */
    public List<String> getRanking() {
        return new ArrayList<>(ranking);
    }


    /**
     * Applies this final update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display the final result
     */
    @Override
    public void apply(View view) {
        view.getClientModel().setCurrentPhase("EndGame");
        view.getClientModel().setRanking(ranking);
        view.getClientModel().setFinalPP(finalPPByPlayer);
        view.getClientModel().setEndGameBonus(endGameBonusByPlayer);
        view.render();
    }
}

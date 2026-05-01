package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;
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
    private final Map<String, Integer> finalScores;
    private final Map<Integer, String> ranking;

    /**
     * Creates a new game-ended message.
     *
     * @param finalScores the final score associated with each player nickname
     * @param ranking the final ranking, where the key is the position and the value is the player nickname
     */
    public GameEndedMessage(Map<String, Integer> finalScores, Map<Integer, String> ranking){
        this.finalScores = new HashMap<>(finalScores);
        this.ranking = new HashMap<>(ranking);
    }

    /**
     * Returns the final score associated with each player.
     *
     * @return a copy of the final scores map
     */
    public Map<String, Integer> getFinalScores(){
        return new HashMap<>(finalScores);
    }

    /**
     * Returns the final ranking.
     *
     * @return a copy of the ranking map
     */
    public Map<Integer, String> getRanking(){
        return new HashMap<>(ranking);
    }

    /**
     * Applies this final update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display the final result
     */
    @Override
    public void apply(View view){
        view.getClientModel().setFinalScores(finalScores);
        view.getClientModel().setRanking(ranking);
        view.getClientModel().setCurrentPhase("EndGame");

        view.render();
    }
}

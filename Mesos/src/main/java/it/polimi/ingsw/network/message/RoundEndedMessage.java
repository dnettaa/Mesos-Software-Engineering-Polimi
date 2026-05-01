package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the server to notify clients that a round has ended.
 * <p>
 * This is a delta message: it contains only the information needed to update
 * the turn order, round number, and the next active player.
 * @author Diana
 */

public class RoundEndedMessage extends ServerMessage{
    private final int newRound;
    private final List<String> newTurnOrder;
    private final String nextPlayerNickname;

    /**
     * Creates a new round-ended message.
     *
     * @param newRound the number of the new round
     * @param newTurnOrder the new turn order for the upcoming round
     * @param nextPlayerNickname the nickname of the next active player
     */
    public RoundEndedMessage(int newRound, List<String> newTurnOrder, String nextPlayerNickname) {
        this.newRound = newRound;
        this.newTurnOrder = new ArrayList<>(newTurnOrder);
        this.nextPlayerNickname = nextPlayerNickname;
    }

    /**
     * Returns the new round number.
     *
     * @return the round number
     */
    public int getNewRound(){
        return newRound;
    }

    /**
     * Returns the new turn order.
     *
     * @return a copy of the turn order list
     */
    public List<String> getNewTurnOrder(){
        return new ArrayList<>(newTurnOrder);
    }

    /**
     * Returns the nickname of the next active player.
     *
     * @return the next player nickname
     */
    public String getNextPlayerNickname(){
        return nextPlayerNickname;
    }

    /**
     * Applies this delta update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display this update
     */
    @Override
    public void apply(View view){
        view.getClientModel().setCurrentRound(newRound);
        view.getClientModel().setTurnOrder(newTurnOrder);
        view.getClientModel().setCurrentPlayer(nextPlayerNickname);

        view.render();
    }
}

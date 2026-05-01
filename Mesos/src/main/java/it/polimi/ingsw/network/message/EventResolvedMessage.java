package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;
import java.util.HashMap;
import java.util.Map;


/**
 * Message sent by the server to notify clients that an event card has been resolved.
 * <p>
 * This is a delta message: it contains only the resource variations produced by
 * the resolved event, without sending the whole game state again.
 * @author Diana
 */

public class EventResolvedMessage extends ServerMessage{
    private final String eventCardID;
    private final Map<String, Integer> foodDeltas;
    private final Map<String, Integer> prestigePointsDeltas;
    private final String nextPhaseName;
    private final String nextPlayerNickname;

    /**
     * Creates a new event-resolved message.
     *
     * @param eventCardID the id of the resolved event card
     * @param foodDeltas the food variation for each player nickname
     * @param prestigePointsDeltas the prestige points variation for each player nickname
     * @param nextPhaseName the name of the next game phase
     * @param nextPlayerNickname the nickname of the next active player
     */
    public EventResolvedMessage(String eventCardID, Map<String, Integer> foodDeltas, Map<String, Integer> prestigePointsDeltas, String nextPhaseName, String nextPlayerNickname) {
        this.eventCardID = eventCardID;
        this.foodDeltas = new HashMap<>(foodDeltas);
        this.prestigePointsDeltas = new HashMap<>(prestigePointsDeltas);
        this.nextPhaseName = nextPhaseName;
        this.nextPlayerNickname = nextPlayerNickname;
    }

    /**
     * Returns the id of the resolved event card.
     *
     * @return the event card id
     */
    public String getEventCardID(){
        return eventCardID;
    }

    /**
     * Returns the food variation for each player.
     *
     * @return a copy of the food deltas map
     */
    public Map<String, Integer> getFoodDeltas(){
        return new HashMap<>(foodDeltas);
    }

    /**
     * Returns the prestige points variation for each player.
     *
     * @return a copy of the prestige points deltas map
     */
    public Map<String, Integer> getPrestigePointsDeltas(){
        return new HashMap<>(prestigePointsDeltas);
    }

    /**
     * Returns the name of the next game phase.
     *
     * @return the next phase name
     */
    public String getNextPhaseName(){
        return nextPhaseName;
    }

    /**
     * Returns the nickname of the next active player.
     *
     * @return the next active player nickname
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
        for(Map.Entry<String, Integer> entry : foodDeltas.entrySet()){
            view.getClientModel().adjustFood(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<String, Integer> entry : prestigePointsDeltas.entrySet()){
            view.getClientModel().adjustPP(entry.getKey(), entry.getValue());
        }

        view.getClientModel().setCurrentPhase(nextPhaseName);
        view.getClientModel().setCurrentPlayer(nextPlayerNickname);

        view.render();
    }
}

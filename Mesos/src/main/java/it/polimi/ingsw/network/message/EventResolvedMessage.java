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
    private final String eventType;
    private final Map<String, Integer> ppDeltaByPlayer;
    private final Map<String, Integer> foodDeltaByPlayer;
    private final String nextPhaseName;

    /**
     * Creates a new event-resolved message.
     *
     * @param eventCardID the id of the resolved event card
     * @param eventType the type of the resolved event
     * @param ppDeltaByPlayer the prestige point variation for each player nickname
     * @param foodDeltaByPlayer the food variation for each player nickname
     */
    public EventResolvedMessage(String eventCardID, String eventType, Map<String, Integer> ppDeltaByPlayer, Map<String, Integer> foodDeltaByPlayer, String nextPhaseName) {
        this.eventCardID = eventCardID;
        this.eventType = eventType;
        this.ppDeltaByPlayer = new HashMap<>(ppDeltaByPlayer);
        this.foodDeltaByPlayer = new HashMap<>(foodDeltaByPlayer);
        this.nextPhaseName = nextPhaseName;
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
     * Returns the type of the resolved event card.
     *
     * @return the event card type
     */
    public String getEventType(){
        return eventType;
    }

    /**
     * Returns the food variation for each player.
     *
     * @return a copy of the food deltas map
     */
    public Map<String, Integer> getFoodDeltaByPlayer() {
        return new HashMap<>(foodDeltaByPlayer);
    }

    /**
     * Returns the prestige points variation for each player.
     *
     * @return a copy of the prestige points deltas map
     */
    public Map<String, Integer> getPpDeltaByPlayer() {
        return new HashMap<>(ppDeltaByPlayer);
    }

    /**
     * Applies this delta update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display this update
     */
    @Override
    public void apply(View view){
        for (Map.Entry<String, Integer> entry : ppDeltaByPlayer.entrySet()){
            view.getClientModel().adjustPP(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : foodDeltaByPlayer.entrySet()){
            view.getClientModel().adjustFood(entry.getKey(), entry.getValue());
        }

        view.getClientModel().setCurrentPhase(nextPhaseName);

        view.render();
    }
}
package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.controller.GameController;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by a client to take cards from the upper and/or lower row.
 * <p>
 * The message contains only card identifiers, not model card objects.
 * The model is responsible for resolving these ids into actual cards.
 * @author Diana
 */

public class TakeCardsMessage extends ClientMessage{
    private final List<String> upperIDs;
    private final List<String> lowerIDs;
    private final List<String> orderedIDs;

    /**
     * Creates a new take-cards message.
     *
     * @param nickname   the nickname of the player who wants to take the cards
     * @param upperIDs   the ids of the selected cards from the upper row
     * @param lowerIDs   the ids of the selected cards from the lower row
     * @param orderedIDs all selected card ids in the order the player picked them
     */
    public TakeCardsMessage(String nickname, List<String> upperIDs, List<String> lowerIDs, List<String> orderedIDs){
        super(nickname);
        this.upperIDs = new ArrayList<>(upperIDs);
        this.lowerIDs = new ArrayList<>(lowerIDs);
        this.orderedIDs = new ArrayList<>(orderedIDs);
    }

    /**
     * Returns the ids of the selected lower-row cards.
     *
     * @return a copy of the selected lower-row card ids
     */
    public List<String> getLowerIDs(){
        return new ArrayList<>(lowerIDs);
    }

    /**
     * Returns the ids of the selected upper-row cards.
     *
     * @return a copy of the selected upper-row card ids
     */
    public List<String> getUpperIDs(){
        return new ArrayList<>(upperIDs);
    }

    /**
     * Returns all selected card ids in pick order.
     *
     * @return a copy of the ordered card ids
     */
    public List<String> getOrderedIDs(){
        return new ArrayList<>(orderedIDs);
    }

    /**
     * Executes the take-cards action on the controller.
     *
     * @param controller the game controller handling the request
     * @param sender the virtual view associated with the client who sent the message
     */
    @Override
    public void execute (GameController controller, VirtualView sender){
        controller.takeCards(getNickname(), getUpperIDs(), getLowerIDs(), getOrderedIDs());
    }
}
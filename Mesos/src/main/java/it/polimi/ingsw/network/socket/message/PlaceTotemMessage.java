package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.controller.GameController;

/**
 * Message sent by a client to place their totem on an offer slot.
 *
 * @author Diana
 */

public class PlaceTotemMessage extends ClientMessage{
    private final char slotID;

    /**
     * Creates a new place-totem message.
     *
     * @param slotID the identifier of the offer slot where the player wants to place the totem
     */
    public PlaceTotemMessage(String nickname, char slotID) {
        super(nickname);
        this.slotID = slotID;
    }

    /**
     * Returns the selected offer slot ID.
     *
     * @return the slot identifier
     */

    public char getSlotID(){
        return slotID;
    }
    /**
     * Executes the place-totem action on the controller.
     *
     * @param controller the game controller handling the request
     * @param sender the virtual view associated with the client who sent the message
     */
    @Override
    public void execute(GameController controller, VirtualView sender){
        controller.placeTotem(getNickname(), slotID);
    }
}
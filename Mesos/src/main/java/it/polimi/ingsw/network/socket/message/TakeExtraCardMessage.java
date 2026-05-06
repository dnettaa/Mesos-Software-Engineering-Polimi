package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.controller.GameController;

/**
 * Message sent by a client to take an extra card.
 * <p>
 * This message is used when a game effect allows the player to take
 * one additional card outside the standard offer resolution.
 * @author Diana
 */

public class TakeExtraCardMessage extends ClientMessage{
    private final String cardID;

    /**
     * Creates a new take-extra-card message.
     *
     * @param nickname the nickname of the player who wants to take the extra card
     * @param cardID the id of the selected extra card
     */
    public TakeExtraCardMessage(String nickname, String cardID) {
        super(nickname);
        this.cardID = cardID;
    }

    /**
     * Returns the id of the selected extra card.
     *
     * @return the selected card id
     */
    public String getCardID(){
        return cardID;
    }

    /**
     * Executes the take-extra-card action on the controller.
     *
     * @param controller the game controller handling the request
     * @param sender the virtual view associated with the client who sent the message
     */
    @Override
    public void execute(GameController controller, VirtualView sender){
        controller.takeExtraCard(getNickname(), cardID);
    }
}
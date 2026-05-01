package it.polimi.ingsw.network.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.controller.GameController;

/**
 * Base class for all messages sent from a client to the server.
 * <p>
 * Each concrete client message knows how to apply itself to the server-side
 * controller. This avoids the use of switch statements or instanceof checks
 * when handling client requests.
 * @author Diana
 */

public abstract class ClientMessage extends Message{
    private final String nickname;

    /**
     * Creates a new client message.
     *
     * @param nickname the nickname of the player who sent the message
     */
    protected ClientMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the nickname of the player who sent this message.
     *
     * @return the sender nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Executes this message on the server-side controller.
     *
     * @param controller the game controller handling the request
     * @param sender the virtual view associated with the client who sent the message
     */
    public abstract void execute(GameController controller, VirtualView sender);
}
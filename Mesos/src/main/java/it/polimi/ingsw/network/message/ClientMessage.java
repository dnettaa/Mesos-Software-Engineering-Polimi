package it.polimi.ingsw.network.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.controller.GameController;

/**
 * Base class for all messages sent from a client to the server.
 * <p>
 * Each concrete client message knows how to apply itself to the server-side
 * controller. This avoids the use of switch statements or instanceof checks
 * when handling client requests.
 */

public abstract class ClientMessage extends Message{

    /**
     * Applies this message to the server-side controller.
     *
     * @param controller the game controller handling the request
     * @param view the virtual view associated with the client who sent the message
     */
    public abstract void apply(GameController controller, VirtualView view);
}

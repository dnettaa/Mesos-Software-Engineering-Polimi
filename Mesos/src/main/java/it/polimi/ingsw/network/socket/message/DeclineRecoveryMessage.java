package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.VirtualView;

/**
 * Message sent by a client that refuses to recover
 * a previously saved game after a server crash.
 *
 * @author Andrea Markvukaj
 */
public class DeclineRecoveryMessage extends ClientMessage {

    public DeclineRecoveryMessage() {
        super(null);
    }

    /**
     * Executes the recovery-decline request on the controller.
     *
     * @param controller the game controller
     * @param sender the client view declining recovery
     */
    @Override
    public void execute(GameController controller, VirtualView sender) {
        controller.declineRecovery(sender);
    }
}

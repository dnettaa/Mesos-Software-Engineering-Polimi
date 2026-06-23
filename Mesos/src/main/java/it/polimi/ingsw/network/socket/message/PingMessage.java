package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.VirtualView;

/**
 * Heartbeat message sent periodically by the client to the server.
 * <p>
 * It carries no game data: its only purpose is to keep the connection alive
 * and to let the server's read timeout detect a dead client. Receiving it is
 * a no-op on the controller.
 *
 * @author Luca Grecchi
 */
public class PingMessage extends ClientMessage {

    public PingMessage() {
        super(null);
    }

    /**
     * No-op: a ping carries no action. The mere fact that it was received is
     * enough to prove the client is still alive and to reset the read timeout.
     *
     * @param controller the game controller (unused)
     * @param sender the client view that sent the ping (unused)
     */
    @Override
    public void execute(GameController controller, VirtualView sender) {
        // Intentionally empty: heartbeat only.
    }
}

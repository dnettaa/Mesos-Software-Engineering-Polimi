package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.view.View;

/**
 * Heartbeat message sent periodically by the server to a client.
 * <p>
 * It carries no game data: its only purpose is to keep the connection alive
 * and to let the client's read timeout detect a dead server. Receiving it is
 * a no-op on the view.
 *
 * @author Luca Grecchi
 */
public class HeartbeatMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    /**
     * No-op: a heartbeat carries no state. The mere fact that it was received is
     * enough to prove the server is still alive and to reset the read timeout.
     *
     * @param view the client view (unused)
     */
    @Override
    public void apply(View view) {
        // Intentionally empty: heartbeat only.
    }
}

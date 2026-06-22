package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.view.View;

/**
 * Message sent by the server when saved-game recovery is canceled.
 *
 * @author Andrea Markvukaj
 */
public class RecoveryCancelledMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final String reason;

    public RecoveryCancelledMessage(String reason) {
        this.reason = reason;
    }

    /**
     * Applies this message to the given view.
     */
    @Override
    public void apply(View view) {
        view.showRecoveryCancelled(reason);
    }
}

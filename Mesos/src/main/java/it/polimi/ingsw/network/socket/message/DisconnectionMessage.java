package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify a client that the game has been interrupted
 * because of a disconnection.
 * @author Diana
 */

public class DisconnectionMessage extends ServerMessage{
    private final String reason;


    /**
     * Creates a new disconnection message.
     *
     * @param reason the reason of the disconnection
     */
    public DisconnectionMessage(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the disconnection reason.
     *
     * @return the reason of the disconnection
     */
    public String getReason(){
        return reason;
    }

    /**
     * Applies this message to the given view.
     *
     * @param view the view that must be notified about the disconnection
     */
    @Override
    public void apply(View view){
        view.notifyDisconnection(reason);
    }
}
package it.polimi.ingsw.message;

import it.polimi.ingsw.view.View;

/**
 * Base class for all messages sent from the server to a client.
 * <p>
 * Each concrete server message knows how to apply itself to the local View.
 * This allows the network layer to dispatch messages without using switch
 * statements or instanceof checks.
 */

public abstract class ServerMessage extends Message{

    /**
     * Applies this message to the given client-side view.
     *
     * @param view the view that must react to this message
     */
    public abstract void apply(View view);
}
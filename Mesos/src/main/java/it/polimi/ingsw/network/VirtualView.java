package it.polimi.ingsw.network;

import it.polimi.ingsw.network.message.ServerMessage;

/**
 * Server-side abstraction of a connected client.
 * <p>
 * The controller uses this interface to notify a client without knowing
 * whether the client is connected through Socket or RMI.
 * @author Diana
 */

public interface VirtualView {

    /**
     * Returns the nickname associated with this virtual view.
     *
     * @return the nickname of the connected player
     */
    String getNickname();

    /**
     * Associates this virtual view with a player nickname.
     *
     * @param nickname the nickname of the connected player
     */
    void setNickname(String nickname);

    /**
     * Sends a server message to the connected client.
     *
     * @param message the message to be delivered to the client
     */
    void send(ServerMessage message);

    /**
     * Closes the connection with the client.
     */
    void disconnect();

    /**
     * Checks whether the client is still connected.
     *
     * @return {@code true} if the client is connected, {@code false} otherwise
     */
    boolean isConnected();
}

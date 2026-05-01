//contratto lato client

package it.polimi.ingsw.network;

import it.polimi.ingsw.network.message.ClientMessage;
import it.polimi.ingsw.model.player.TotemColor;

/**
 * Client-side abstraction of the remote game server.
 * <p>
 * The View uses this interface to communicate with the server without knowing
 * whether the underlying network technology is Socket or RMI.
 * @author Diana
 */

public interface VirtualServer {

    /**
     * Creates a new lobby and joins it as the first player.
     *
     * @param nickname the nickname chosen by the player
     * @param color the totem color chosen by the player
     * @param expectedPlayers the number of players required to start the game
     */
    void createLobby(String nickname, TotemColor color, int expectedPlayers);

    /**
     * Joins an existing lobby.
     *
     * @param nickname the nickname chosen by the player
     * @param color the totem color chosen by the player
     */
    void joinLobby(String nickname, TotemColor color);

    /**
     * Sends a generic client message to the server.
     *
     * @param message the message representing a client-side action
     */
    void sendMessage(ClientMessage message);

    /**
     * Closes the connection with the server.
     */
    void disconnect();
}

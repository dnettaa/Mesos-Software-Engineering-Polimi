package it.polimi.ingsw.network;

import it.polimi.ingsw.model.player.TotemColor;
import java.util.List;

/**
 * Client-side abstraction of the remote game server.
 * <p>
 * The view uses this interface to request domain actions without knowing
 * whether the underlying network protocol is Socket or RMI.
 *
 * @author Diana
 */

public interface VirtualServer{

    /**
     * Requests the creation of a new lobby.
     *
     * @param nickname        nickname of the player creating the lobby
     * @param color           chosen totem color
     * @param expectedPlayers number of players required to start the game
     */
    void createLobby(String nickname, TotemColor color, int expectedPlayers);

    /**
     * Requests to join an existing lobby.
     *
     * @param nickname nickname of the joining player
     * @param color    chosen totem color
     */
    void joinLobby(String nickname, TotemColor color);

    /**
     * Requests to place the player's totem on an offer slot.
     *
     * @param nickname nickname of the player performing the action
     * @param slotID   identifier of the chosen offer slot
     */
    void placeTotem(String nickname, char slotID);

    /**
     * Requests to take cards from the upper and lower rows.
     *
     * @param nickname nickname of the player performing the action
     * @param upperIDs identifiers of the selected upper-row cards
     * @param lowerIDs identifiers of the selected lower-row cards
     */
    void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs);

    /**
     * Requests to take an extra card.
     *
     * @param nickname nickname of the player performing the action
     * @param cardID   identifier of the selected card
     */
    void takeExtraCard(String nickname, String cardID);

    /**
     * Requests a voluntary disconnection from the server.
     */
    void disconnect();

    /**
     * Checks whether the client-side network adapter currently considers
     * the server connection available.
     *
     * @return {@code true} if the adapter can send requests to the server
     */
    boolean isConnected();

    /**
     * Requests to reconnect to an existing game session.
     *
     * @param nickname nickname of the reconnecting player
     * @param color    chosen totem color
     */
    void reconnect(String nickname, TotemColor color);
}

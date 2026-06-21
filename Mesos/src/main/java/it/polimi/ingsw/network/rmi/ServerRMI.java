package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.model.player.TotemColor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface exposed by the server to RMI clients.
 * <p>
 * This interface contains domain-specific methods. RMI clients call these
 * methods directly instead of sending generic network messages.
 *
 * @author Diana
 */
public interface ServerRMI extends Remote{

    /**
     * Remotely requests the creation of a new lobby.
     *
     * @param nickname        nickname of the player creating the lobby
     * @param color           chosen totem color
     * @param expectedPlayers number of players required to start the game
     * @param client          remote callback object of the client
     * @throws RemoteException if the remote invocation fails
     */
    void createLobby(String nickname, TotemColor color, int expectedPlayers, ClientRMI client)
            throws RemoteException;

    /**
     * Remotely requests to join an existing lobby.
     *
     * @param nickname nickname of the joining player
     * @param color    chosen totem color
     * @param client   remote callback object of the client
     * @throws RemoteException if the remote invocation fails
     */
    void joinLobby(String nickname, TotemColor color, ClientRMI client)
            throws RemoteException;

    /**
     * Remotely requests to place a totem on an offer slot.
     *
     * @param nickname nickname of the player performing the action
     * @param slotID   identifier of the chosen offer slot
     * @throws RemoteException if the remote invocation fails
     */
    void placeTotem(String nickname, char slotID)
            throws RemoteException;

    /**
     * Remotely requests to take cards from the upper and lower rows.
     *
     * @param nickname   nickname of the player performing the action
     * @param upperIDs   identifiers of the selected upper-row cards
     * @param lowerIDs   identifiers of the selected lower-row cards
     * @param orderedIDs all selected card ids in the order the player picked them
     * @throws RemoteException if the remote invocation fails
     */
    void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs, List<String> orderedIDs)
            throws RemoteException;

    /**
     * Remotely requests to take an extra card.
     *
     * @param nickname nickname of the player performing the action
     * @param cardID   identifier of the selected card
     * @throws RemoteException if the remote invocation fails
     */
    void takeExtraCard(String nickname, String cardID)
            throws RemoteException;

    /**
     * Remotely requests a disconnection for the given player.
     *
     * @param nickname nickname of the disconnecting player
     * @throws RemoteException if the remote invocation fails
     */
    void disconnect(String nickname)
            throws RemoteException;

    /**
     * Checks whether the remote server is reachable.
     *
     * @throws RemoteException if the remote invocation fails
     */
    void ping()
            throws RemoteException;

    /**
     * Remotely requests to reconnect to a
     * previously saved match after a server crash.
     *
     * @param nickname original player nickname
     * @param color original player totem color
     * @param client remote callback object
     * @throws RemoteException if remote invocation fails
     */
    void reconnect(String nickname, TotemColor color, ClientRMI client)
            throws RemoteException;

    /**
     * Remotely declines recovery of a previously saved match.
     *
     * @param client remote callback object
     * @throws RemoteException if remote invocation fails
     */
    void declineRecovery(ClientRMI client)
            throws RemoteException;
}
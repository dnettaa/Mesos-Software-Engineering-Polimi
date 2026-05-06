package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Remote callback interface exposed by an RMI client to the server.
 * <p>
 * The server uses this interface to notify the client through direct remote
 * method calls, passing DTOs when game state changes must be applied.
 *
 * @author Diana
 */

public interface ClientRMI extends Remote{

    /**
     * Remotely notifies the client that the join operation succeeded.
     *
     * @param nickname assigned nickname
     * @param color    assigned totem color
     * @throws RemoteException if the remote invocation fails
     */
    void onJoinSuccess(String nickname, TotemColor color)
            throws RemoteException;

    /**
     * Remotely notifies the client that the lobby state changed.
     *
     * @param players        nicknames of the players currently in the lobby
     * @param colorsByPlayer selected color for each player
     * @param expected       number of players required to start the game
     * @throws RemoteException if the remote invocation fails
     */
    void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected)
            throws RemoteException;

    /**
     * Remotely notifies the client about an error.
     *
     * @param code        error code
     * @param description human-readable error description
     * @throws RemoteException if the remote invocation fails
     */
    void onError(String code, String description)
            throws RemoteException;

    /**
     * Remotely notifies the client that the game connection has been closed.
     *
     * @param reason reason of the disconnection
     * @throws RemoteException if the remote invocation fails
     */
    void onDisconnection(String reason)
            throws RemoteException;

    /**
     * Remotely notifies the client that the game has started.
     *
     * @param snapshot initial game state snapshot
     * @throws RemoteException if the remote invocation fails
     */
    void onGameStarted(GameStateSnapshot snapshot)
            throws RemoteException;

    /**
     * Remotely notifies the client that a totem has been placed.
     *
     * @param dto data describing the totem placement
     * @throws RemoteException if the remote invocation fails
     */
    void onTotemPlaced(TotemPlacedDTO dto)
            throws RemoteException;

    /**
     * Remotely notifies the client that cards have been taken.
     *
     * @param dto data describing the taken cards and resulting state changes
     * @throws RemoteException if the remote invocation fails
     */
    void onCardsTaken(CardsTakenDTO dto)
            throws RemoteException;

    /**
     * Remotely notifies the client that an extra card has been taken.
     *
     * @param dto data describing the extra card action
     * @throws RemoteException if the remote invocation fails
     */
    void onExtraCardTaken(ExtraCardTakenDTO dto)
            throws RemoteException;

    /**
     * Remotely notifies the client that one or more events have been resolved.
     *
     * @param dto data describing the resolved events
     * @throws RemoteException if the remote invocation fails
     */
    void onEventResolved(EventResolvedDTO dto)
            throws RemoteException;

    /**
     * Remotely notifies the client that the current round has ended.
     *
     * @param dto data describing the new round state
     * @throws RemoteException if the remote invocation fails
     */
    void onRoundEnded(RoundEndedDTO dto)
            throws RemoteException;

    /**
     * Remotely notifies the client that the game has ended.
     *
     * @param dto data describing the final game result
     * @throws RemoteException if the remote invocation fails
     */
    void onGameEnded(GameEndedDTO dto)
            throws RemoteException;

    /**
     * Checks whether the remote client is reachable.
     *
     * @throws RemoteException if the remote invocation fails
     */
    void ping()
            throws RemoteException;
}
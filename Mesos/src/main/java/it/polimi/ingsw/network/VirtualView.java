package it.polimi.ingsw.network;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import java.util.List;
import java.util.Map;

/**
 * Server-side abstraction of a connected client view.
 * <p>
 * The controller uses this interface to notify a client without knowing
 * whether the client is connected through Socket or RMI.
 *
 * @author Diana
 */

public interface VirtualView{

    /**
     * Returns the nickname associated with this client connection.
     *
     * @return the player's nickname
     */
    String getNickname();

    /**
     * Sets the nickname associated with this client connection.
     *
     * @param nickname the player's nickname
     */
    void setNickname(String nickname);

    /**
     * Checks whether this client connection is still active.
     *
     * @return {@code true} if the client is connected, {@code false} otherwise
     */
    boolean isConnected();

    /**
     * Closes this client connection.
     */
    void disconnect();

    /**
     * Notifies the client that the join operation succeeded.
     *
     * @param nickname assigned nickname
     * @param color    assigned totem color
     */
    void onJoinSuccess(String nickname, TotemColor color);

    /**
     * Notifies the client that the lobby state changed.
     *
     * @param players        nicknames of the players currently in the lobby
     * @param colorsByPlayer selected color for each player
     * @param expected       number of players required to start the game
     */
    void onLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected);

    /**
     * Notifies the client about an error.
     *
     * @param code        error code
     * @param description human-readable error description
     */
    void onError(String code, String description);

    /**
     * Notifies the client that the game connection has been closed.
     *
     * @param reason reason of the disconnection
     */
    void onDisconnection(String reason);

    /**
     * Notifies the client that recovery was canceled without closing the connection.
     *
     * @param reason reason shown to the user
     */
    void onRecoveryCancelled(String reason);

    /**
     * Notifies the client that the game has started.
     *
     * @param snapshot initial game state snapshot
     */
    void onGameStarted(GameStateSnapshot snapshot);

    /**
     * Notifies the client that a totem has been placed.
     *
     * @param dto data describing the totem placement
     */
    void onTotemPlaced(TotemPlacedDTO dto);

    /**
     * Notifies the client that cards have been taken.
     *
     * @param dto data describing the taken cards and resulting state changes
     */
    void onCardsTaken(CardsTakenDTO dto);

    /**
     * Notifies the client that an extra card has been taken.
     *
     * @param dto data describing the extra card action
     */
    void onExtraCardTaken(ExtraCardTakenDTO dto);

    /**
     * Notifies the client that one or more events have been resolved.
     *
     * @param dto data describing the resolved events
     */
    void onEventResolved(EventResolvedDTO dto);

    /**
     * Notifies the client that the current round has ended.
     *
     * @param dto data describing the new round state
     */
    void onRoundEnded(RoundEndedDTO dto);

    /**
     * Notifies the client that the game has ended.
     *
     * @param dto data describing the final game result
     */
    void onGameEnded(GameEndedDTO dto);
}

package it.polimi.ingsw.view;

import java.util.List;
import java.util.Map;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;

/**
 * The View interface representing the client-side UI.
 * It defines the contract for displaying updates and interacting with the game state.
 *
 * @author Luca Grecchi
 */
public interface View {

    /**
     * Notifies the user of a successful lobby join.
     *
     * @param nickname the confirmed nickname assigned by the server
     * @param color    the assigned totem color
     */
    void showJoinSuccess(String nickname, TotemColor color);

    /**
     * Updates the user with the current status of the lobby.
     *
     * @param players        ordered list of connected player nicknames
     * @param colorsByPlayer map from nickname to assigned totem color
     * @param expected       total number of players required to start
     */
    void showLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected);

    /**
     * Notifies the user of a disconnection from the server.
     *
     * @param reason human-readable disconnection reason
     */
    void notifyDisconnection(String reason);

    /**
     * Notifies the user that recovery was canceled and the initial menu can be shown again.
     */
    void showRecoveryCancelled(String reason);

    /**
     * Shuts down the client application after printing the given message.
     */
    void shutdown(String reason);

    /**
     * Binds the view to the network layer.
     *
     * @param vs the {@link VirtualServer} to use for outgoing messages
     */
    void setVirtualServer(VirtualServer vs);

    /**
     * Returns the local replica of the game state.
     *
     * @return the current {@link ClientModel}
     */
    ClientModel getClientModel();

    /**
     * Sets the client model for this view.
     *
     * @param model the new {@link ClientModel} received from the server
     */
    void setClientModel(ClientModel model);

    /**
     * Reads the current state from the client model and redraws the interface.
     */
    void render();

    /**
     * Handles errors occurring during the pre-game phases (lobby and login).
     *
     * @param description human-readable error description
     */
    void showLoginError(String description);

    /**
     * Handles errors occurring during gameplay (invalid moves, wrong turn, etc.).
     *
     * @param description human-readable error description
     */
    void showGameError(String description);

    /**
     * Notifies the view that an event card has been resolved.
     *
     * @param eventCardID the ID of the resolved event card
     * @param eventType   the type of the event
     * @param ppDelta     prestige point changes per player (positive or negative)
     * @param foodDelta   food changes per player (positive or negative)
     */
    void showEventResolved(String eventCardID, String eventType,
                           Map<String, Integer> ppDelta, Map<String, Integer> foodDelta);

    /**
     * Displays the leaderboard received from the server.
     */
    void showLeaderboard(List<MatchResult> ranking, int position);

    /**
     * Asks the user whether to recover a previously saved game.
     *
     * @return true if the user accepts recovery, false otherwise
     */
    boolean askRecoveryChoice();

    /**
     * Updates the user with the current saved-game recovery status.
     *
     * @param reconnectedPlayers nicknames of the players who already reconnected
     * @param missingPlayers nicknames of the players still missing from recovery
     */
    void showRecoveryUpdate(List<String> reconnectedPlayers, List<String> missingPlayers);

    /**
     * Returns the client to the initial welcome/connection screen.
     * Called when the server disconnects while the client is waiting in the lobby.
     *
     * @param reason human-readable reason
     */
    void goToWelcomeScreen(String reason);
}
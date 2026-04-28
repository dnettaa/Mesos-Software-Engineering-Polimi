package it.polimi.ingsw.view;

import java.util.List;
import java.util.Map;
import it.polimi.ingsw.model.player.TotemColor;

/**
 * The View interface representing the client-side UI.
 * It defines the contract for displaying updates and interacting with the game state.
 */
public interface View {

    /**
     * Notifies the user of a successful lobby join.
     */
    void showJoinSuccess(String nickname, TotemColor color);

    /**
     * Updates the user with the current status of the lobby.
     */
    void showLobbyUpdate(List<String> players, Map<String, TotemColor> colorsByPlayer, int expected);

    /**
     * Renders the current state of the game board.
     */
    void showGameState(/* GameStateMessage */ Object state);

    /**
     * Displays an error message to the user.
     */
    void showError(String code, String description);

    /**
     * Notifies the user of a disconnection from the server.
     */
    void notifyDisconnection(String reason);

    /**
     * Binds the view to the network layer (VirtualServer).
     */
    void setVirtualServer(/* VirtualServer */ Object vs);
}
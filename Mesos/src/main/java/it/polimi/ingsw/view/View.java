package it.polimi.ingsw.view;

import java.util.List;
import java.util.Map;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;

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
     * Notifies the user of a disconnection from the server.
     */
    void notifyDisconnection(String reason);

    /**
     * Notifies the user that recovery was canceled and the initial menu can be shown again.
     */
    void showRecoveryCancelled(String reason);

    /**
     * Binds the view to the network layer (VirtualServer).
     */
    void setVirtualServer(VirtualServer vs);

    /**
     * Restituisce la replica locale dello stato del gioco.
     */
    ClientModel getClientModel();

    /**
     * Imposta il ClientModel per questa vista.
     */
    void setClientModel(ClientModel model);

    /**
     * Legge lo stato dal ClientModel e ridisegna l'interfaccia.
     */
    void render();

    /**
     * Gestisce gli errori durante la fase pre-partita (Lobby e Login).
     */
    void showLoginError(String description);

    /**
     * Gestisce gli errori durante il gioco (mosse non valide, turno sbagliato).
     */
    void showGameError(String description);

    void showEventResolved(String eventCardID, String eventType,
                           Map<String, Integer> ppDelta, Map<String, Integer> foodDelta);

    /**
     * Displays the leaderboard received from the server.
     */
    void showLeaderboard(List<MatchResult> ranking, int position);

    boolean askRecoveryChoice();

    void showRecoveryUpdate(List<String> reconnectedPlayers, List<String> missingPlayers);
}
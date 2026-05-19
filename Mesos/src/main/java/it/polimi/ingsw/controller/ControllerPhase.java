package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;

import java.util.List;

/**
 * Represents a phase of the controller.
 * Each phase defines how incoming client actions are handled.
 * Implementations include different behaviors depending on the
 * current state of the game (e.g., lobby phase, in-game phase).
 *
 * @author Andrea Markvukaj
 */
public interface ControllerPhase {

    /**
     * Creates a new lobby.
     *
     * @param nickname the nickname of the player creating the lobby
     * @param color the chosen totem color
     * @param view the virtual view associated with the player
     */
    void createLobby(String nickname, TotemColor color, VirtualView view);

    /**
     * Allows a player to join an existing lobby.
     *
     * @param nickname the nickname of the player
     * @param color the chosen totem color
     * @param view the virtual view associated with the player
     */
    void joinLobby(String nickname, TotemColor color, VirtualView view);

    /**
     * Handles the placement of a totem on the board.
     *
     * @param nickname the nickname of the player
     * @param slotID the identifier of the slot where the totem is placed
     */
    void placeTotem(String nickname, char slotID);

    /**
     * Handles the selection of cards by a player.
     *
     * @param nickname the nickname of the player
     * @param upperIDs identifiers of selected cards from the upper row
     * @param lowerIDs identifiers of selected cards from the lower row
     */
    void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs);

    /**
     * Handles the selection of an extra card.
     *
     * @param nickname the nickname of the player
     * @param cardID the identifier of the selected card
     */
    void takeExtraCard(String nickname, String cardID);

    /**
     * Handles player disconnection.
     *
     * @param nickname the nickname of the disconnected player
     */
    void onDisconnect(String nickname);

    /**
     * Handles a player's reconnection request after
     * a server crash recovery.
     *
     * @param controller the main game controller
     * @param nickname the player's original nickname
     * @param color the player's original totem color
     * @param view the reconnecting virtual view
     */
    default void reconnect(GameController controller, String nickname, TotemColor color, VirtualView view) {
        throw new IllegalStateException("Reconnect not allowed in current phase");
    }

    default void acceptRecovery(GameController controller, String nickname, TotemColor color, VirtualView view) {
        throw new IllegalStateException("Recovery accept not allowed in current phase");
    }

    default void declineRecovery(GameController controller, VirtualView view) {
        throw new IllegalStateException("Recovery decline not allowed in current phase");
    }
}

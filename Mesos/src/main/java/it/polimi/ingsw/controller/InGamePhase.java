package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.persistence.PersistenceManager;

import java.util.List;

/**
 * Controller phase representing the active gameplay state.
 * In this phase, all valid game actions (such as placing a totem,
 * selecting cards, or taking an extra card) are accepted and delegated
 * to the {@link GameActions} model.
 * The model notifies its listeners (via {@link it.polimi.ingsw.model.game.GameListener}),
 * and the {@link GameController} forwards those updates to the clients.
 * Any invalid action or rule violation is handled through
 * {@link GameException}, which is caught and translated into
 * an error message sent to the corresponding client.
 * Lobby-related actions (e.g., creating or joining a lobby) are rejected,
 * as the game has already started.
 *
 * @author Andrea Markvukaj
 */
public class InGamePhase implements ControllerPhase {

    private final GameController controller;
    private final GameActions game;

    public InGamePhase(GameController controller, GameActions game) {
        this.controller = controller;
        this.game = game;
    }

    // METODI DI GIOCO

    /**
     * Handles the placement of a totem by a player.
     * Delegates the action to the model. If the action is valid,
     * the model will notify all listeners, and the update will be
     * propagated to clients by the controller.
     * In case of rule violations, an error is sent to the requesting player.
     *
     * @param nickname the nickname of the player
     * @param slotID the identifier of the chosen slot
     */
    @Override
    public void placeTotem(String nickname, char slotID) {
        try {
            game.placeTotem(nickname, slotID);
        } catch (GameException e) {
            controller.sendError(nickname, e.getCode().name(), e.getMessage());
        }
    }

    /**
     * Handles the selection of cards by a player.
     * Delegates the action to the model. If the action is valid,
     * the model will notify all listeners, and the update will be
     * propagated to clients by the controller.
     * In case of rule violations, an error is sent to the player.
     *
     * @param nickname the nickname of the player
     * @param upperIDs identifiers of selected cards from the upper row
     * @param lowerIDs identifiers of selected cards from the lower row
     */
    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs) {
        try {
            game.takeCards(nickname, upperIDs, lowerIDs);
        } catch (GameException e) {
            controller.sendError(nickname, e.getCode().name(), e.getMessage());
        }
    }

    /**
     * Handles the selection of an extra card by a player.
     * Delegates the action to the model. If the action is valid,
     * the model will notify all listeners, and the update will be
     * propagated to clients by the controller.
     * In case of rule violations, an error is sent to the player.
     *
     * @param nickname the nickname of the player
     * @param cardID the identifier of the selected card
     */
    @Override
    public void takeExtraCard(String nickname, String cardID) {
        try {
            game.takeExtraCard(nickname, cardID);
        } catch (GameException e) {
            controller.sendError(nickname, e.getCode().name(), e.getMessage());
        }
    }

    /**
     * Handles the disconnection of a player during the game.
     * Currently, the disconnection of any player results in the termination
     * of the entire game session and all connected clients are disconnected.
     *
     * @param nickname the nickname of the disconnected player
     */
    @Override
    public void onDisconnect(String nickname) {
        PersistenceManager.deleteSave();
        controller.closeAll();
    }

    // METODI NON APPARTENENTI A GAME PHASE

    /**
     * Rejects lobby creation requests, as the game is already in progress.
     *
     * @param nickname the nickname of the player
     * @param color the chosen totem color
     * @param view the virtual view of the player
     */
    @Override
    public void createLobby(String nickname, TotemColor color, VirtualView view) {
        controller.sendError(nickname, ErrorCode.GAME_ALREADY_STARTED.name(),
                "Game already started");
    }

    /**
     * Rejects requests to join a lobby, as the game has already started.
     *
     * @param nickname the nickname of the player
     * @param color the chosen totem color
     * @param view the virtual view of the player
     */
    @Override
    public void joinLobby(String nickname, TotemColor color, VirtualView view) {
        controller.sendError(nickname, ErrorCode.GAME_ALREADY_STARTED.name(),
                "Game already started");
    }
}

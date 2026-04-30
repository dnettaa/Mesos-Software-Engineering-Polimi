package it.polimi.ingsw.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.GameSetupService;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.LobbyUpdateMessage;
import it.polimi.ingsw.network.message.JoinSuccessMessage;

/**
 * Controller phase representing the lobby state of the game.
 * In this phase, players can create or join a lobby before the game starts.
 * The class is responsible for:
 *     Managing player registrations and color assignments
 *     Validating lobby constraints (e.g., unique nickname and color)
 *     Broadcasting lobby updates to all connected clients
 *     Creating the game when the expected number of players is reached
 * All game-related actions (e.g., placing a totem, taking cards) are rejected
 * during this phase.
 *
 * @author Andrea Markvukaj
 */
public class LobbyPhase implements ControllerPhase {

    private final GameController gameController;
    private final int expectedPlayers;
    private final Map<String, TotemColor> playerSelections;

    public LobbyPhase(GameController gameController, int expectedPlayers) {

        this.gameController = gameController;
        this.expectedPlayers = expectedPlayers;
        this.playerSelections = new LinkedHashMap<>();
    }

    /**
     * Handles the creation of a new lobby.
     * Only allowed if the lobby is empty. Registers the player,
     * sends a confirmation message, and broadcasts the updated lobby state.
     *
     * @param nickname the nickname of the player creating the lobby
     * @param color the chosen totem color
     * @param view the virtual view associated with the player
     */
    @Override
    public void createLobby(String nickname, TotemColor color, VirtualView view) {

        if (!playerSelections.isEmpty()) {
            gameController.sendError(nickname, ErrorCode.GAME_ALREADY_STARTED.name(),
                    "Lobby already created");
            return;
        }

        gameController.registerView(nickname, view);
        playerSelections.put(nickname, color);
        gameController.sendTo(nickname, new JoinSuccessMessage(nickname, color));

        gameController.broadcast(
                new LobbyUpdateMessage(List.copyOf(playerSelections.keySet()), playerSelections, this.expectedPlayers)
        );
    }

    /**
     * Handles a player's request to join an existing lobby.
     * Validates lobby constraints (existence, capacity, uniqueness of nickname and color).
     * If successful, registers the player, sends confirmation, and broadcasts the updated lobby.
     * When the expected number of players is reached, initializes the game and transitions
     * to {@link InGamePhase}.
     *
     * @param nickname the nickname of the player
     * @param color the chosen totem color
     * @param view the virtual view associated with the player
     */
    @Override
    public void joinLobby(String nickname, TotemColor color, VirtualView view) {

        if (playerSelections.isEmpty()) {
            gameController.sendError(nickname, ErrorCode.LOBBY_NOT_CREATED.name(),
                    "Lobby has not been created yet");
            return;
        }

        if (playerSelections.size() >= expectedPlayers) {
            gameController.sendError(nickname, ErrorCode.LOBBY_FULL.name(),
                    "Lobby is full");
            return;
        }

        if (playerSelections.containsKey(nickname)) {
            gameController.sendError(nickname, ErrorCode.NICKNAME_TAKEN.name(),
                    "Nickname already taken");
            return;
        }

        if (playerSelections.containsValue(color)) {
            gameController.sendError(nickname, ErrorCode.COLOR_TAKEN.name(),
                    "Color already taken");
            return;
        }

        gameController.registerView(nickname, view);
        playerSelections.put(nickname, color);
        gameController.sendTo(nickname, new JoinSuccessMessage(nickname, color));

        gameController.broadcast(
                new LobbyUpdateMessage(List.copyOf(playerSelections.keySet()), playerSelections, expectedPlayers)
        );

        // Creazione del Game e transizione alla fase successiva se la lobby è completa.
        if (playerSelections.size() == expectedPlayers) {

            GameSetupService gameSetupService = new GameSetupService();
            GameActions game = gameSetupService.createNewGame(playerSelections, 0);

            gameController.setGame(game);
            gameController.transitionTo(new InGamePhase(gameController, game));
            game.startGame();
        }
    }

    /**
     * Rejects totem placement requests during the lobby phase.
     *
     * @param nickname the nickname of the player
     * @param slotID the slot identifier
     */
    @Override
    public void placeTotem(String nickname, char slotID) {
        gameController.sendError(nickname, ErrorCode.INVALID_PHASE.name(),
                "Cannot place totem during lobby phase");
    }

    /**
     * Rejects card selection requests during the lobby phase.
     *
     * @param nickname the nickname of the player
     * @param upperIDs selected upper row cards
     * @param lowerIDs selected lower row cards
     */
    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs) {
        gameController.sendError(nickname, ErrorCode.INVALID_PHASE.name(),
                "Cannot take cards during lobby phase");
    }

    /**
     * Rejects extra card selection requests during the lobby phase.
     *
     * @param nickname the nickname of the player
     * @param cardID the identifier of the selected card
     */
    @Override
    public void takeExtraCard(String nickname, String cardID) {
        gameController.sendError(nickname, ErrorCode.INVALID_PHASE.name(),
                "Cannot take extra card during lobby phase");
    }

    /**
     * Handles player disconnection during the lobby phase.
     * Removes the player from the lobby, unregisters the associated view,
     * and broadcasts the updated lobby state to the remaining players.
     *
     * @param nickname the nickname of the disconnected player
     */
    @Override
    public void onDisconnect(String nickname) {

        if (playerSelections.remove(nickname) != null) {

            gameController.unregisterView(nickname);

            gameController.broadcast(
                    new LobbyUpdateMessage(List.copyOf(playerSelections.keySet()), playerSelections, expectedPlayers)
            );
        }
    }
}
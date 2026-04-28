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

public class LobbyPhase implements ControllerPhase {

    private final GameController gameController;
    private final int expectedPlayers;
    private final Map<String, TotemColor> playerSelections;

    public LobbyPhase(GameController gameController, int expectedPlayers) {

        this.gameController = gameController;
        this.expectedPlayers = expectedPlayers;
        this.playerSelections = new LinkedHashMap<>();
    }

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
        }
    }

    @Override
    public void placeTotem(String nickname, char slotID) {
        gameController.sendError(nickname, ErrorCode.INVALID_PHASE.name(),
                "Cannot place totem during lobby phase");
    }

    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs) {
        gameController.sendError(nickname, ErrorCode.INVALID_PHASE.name(),
                "Cannot take cards during lobby phase");
    }

    @Override
    public void takeExtraCard(String nickname, String cardID) {
        gameController.sendError(nickname, ErrorCode.INVALID_PHASE.name(),
                "Cannot take extra card during lobby phase");
    }

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
package it.polimi.ingsw.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.polimi.ingsw.model.Exception.GameException;
import it.polimi.ingsw.model.Exception.ErrorCode;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.game.GameSetupService;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.LobbyUpdateMessage;

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

            throw new GameException(ErrorCode.GAME_ALREADY_STARTED, "Lobby already created");
        }

        if (playerSelections.containsKey(nickname)) {

            throw new GameException(ErrorCode.NICKNAME_TAKEN, "Nickname already taken");
        }

        if (playerSelections.containsValue(color)) {

            throw new GameException(ErrorCode.COLOR_TAKEN, "Color already taken");
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

            throw new GameException(ErrorCode.LOBBY_NOT_CREATED, "Lobby has not been created yet");
        }

        if (playerSelections.size() >= expectedPlayers) {

            throw new GameException(ErrorCode.LOBBY_FULL, "Lobby is full");
        }

        if (playerSelections.containsKey(nickname)) {

            throw new GameException(ErrorCode.NICKNAME_TAKEN, "Nickname already taken");
        }

        if (playerSelections.containsValue(color)) {

            throw new GameException(ErrorCode.COLOR_TAKEN, "Color already taken");
        }

        gameController.registerView(nickname, view);
        playerSelections.put(nickname, color);

        gameController.broadcast(
                new LobbyUpdateMessage(List.copyOf(playerSelections.keySet()), playerSelections, expectedPlayers)
        );

        // Creazione del Game e transizione alla fase successiva
        if (playerSelections.size() == expectedPlayers) {

            GameSetupService gameSetupService = new GameSetupService();
            GameActions game = gameSetupService.createNewGame(playerSelections, 0);

            gameController.setGame(game);
            gameController.transitionTo(new InGamePhase(gameController));
        }
    }

    @Override
    public void placeTotem(String nickname, char slotID) {
        throw new GameException(ErrorCode.INVALID_PHASE, "Cannot place totem during lobby phase");
    }

    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs) {
        throw new GameException(ErrorCode.INVALID_PHASE, "Cannot take cards during lobby phase");
    }

    @Override
    public void takeExtraCard(String nickname, String cardID) {
        throw new GameException(ErrorCode.INVALID_PHASE, "Cannot take extra card during lobby phase");
    }

    @Override
    public void onDisconnect(String nickname) {

        if (playerSelections.remove(nickname) != null) {

            gameController.unregisterView(nickname);

            gameController.broadcast(
                    new LobbyUpdateMessage( List.copyOf(playerSelections.keySet()), playerSelections, expectedPlayers)
            );
        }
    }
}
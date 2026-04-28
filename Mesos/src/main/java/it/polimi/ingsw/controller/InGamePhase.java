package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;

import java.util.List;

public class InGamePhase implements ControllerPhase {

    private final GameController controller;
    private final GameActions game;

    public InGamePhase(GameController controller, GameActions game) {
        this.controller = controller;
        this.game = game;
    }

    // METODI DI GIOCO

    @Override
    public void placeTotem(String nickname, char slotID) {
        try {
            game.placeTotem(nickname, slotID);
            controller.broadcastGameState();
        } catch (GameException e) {
            controller.sendError(nickname, e.getCode().name(), e.getMessage());
        }
    }

    @Override
    public void takeCards(String nickname, List<String> upperIDs, List<String> lowerIDs) {
        try {
            game.takeCards(nickname, upperIDs, lowerIDs);
            controller.broadcastGameState();
        } catch (GameException e) {
            controller.sendError(nickname, e.getCode().name(), e.getMessage());
        }
    }

    @Override
    public void takeExtraCard(String nickname, String cardID) {
        try {
            game.takeExtraCard(nickname, cardID);
            controller.broadcastGameState();
        } catch (GameException e) {
            controller.sendError(nickname, e.getCode().name(), e.getMessage());
        }
    }

    @Override
    public void onDisconnect(String nickname) {
        controller.closeAll();
    }

    // METODI NON APPARTENENTI A GAME PHASE

    @Override
    public void createLobby(String nickname, TotemColor color, VirtualView view) {
        controller.sendError(nickname, ErrorCode.GAME_ALREADY_STARTED.name(),
                "Game already started");
    }

    @Override
    public void joinLobby(String nickname, TotemColor color, VirtualView view) {
        controller.sendError(nickname, ErrorCode.GAME_ALREADY_STARTED.name(),
                "Game already started");
    }

}

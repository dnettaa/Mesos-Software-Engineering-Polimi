package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.message.GameStateMessage;
import it.polimi.ingsw.network.message.ServerMessage;
import it.polimi.ingsw.network.message.ErrorMessage;

import java.util.HashMap;
import java.util.Map;

public class GameController {

    private GameActions game;
    private ControllerPhase currentPhase;
    private final Map<String, VirtualView> views;

    public GameController() {
        this.views = new HashMap<>();
    }

    // metodi da delegare alla LobbyPhase
    public synchronized void createLobby(String nickname, TotemColor color, int expectedPlayers, VirtualView view) {
        if (currentPhase == null) {
            currentPhase = new LobbyPhase(this, expectedPlayers);
        }
        currentPhase.createLobby(nickname, color, view);
    }

    public synchronized void joinLobby(String nickname, TotemColor color, VirtualView view) {
        currentPhase.joinLobby(nickname, color, view);
    }

    public synchronized void onDisconnect(String nickname) {
        if (currentPhase != null) {
            currentPhase.onDisconnect(nickname);
        }
    }

    // metodi da delegare alla InGamePhase
    public synchronized void placeTotem(String nickname, char slotID) {
        currentPhase.placeTotem(nickname, slotID);
    }

    public synchronized void takeCards(String nickname, java.util.List<String> upperIDs, java.util.List<String> lowerIDs) {
        currentPhase.takeCards(nickname, upperIDs, lowerIDs);
    }

    public synchronized void takeExtraCard(String nickname, String cardID) {
        currentPhase.takeExtraCard(nickname, cardID);
    }

    // GESTIONE DELLE VIEW

    public void registerView(String nickname, VirtualView view) {
        views.put(nickname, view);
    }

    public void unregisterView(String nickname) {
        views.remove(nickname);
    }

    // GESTIONE DELLA COMUNICAZIONE

    public void broadcast(ServerMessage msg) {
        for (VirtualView view : views.values()) {
            if (view.isConnected()) {
                view.send(msg);
            }
        }
    }

    public void broadcastGameState() {
        if (game != null) {
            broadcast(game.buildGameStateMessage());
        }
    }

    public void sendError(String nickname, String code, String desc) {
        VirtualView view = views.get(nickname);
        if (view != null && view.isConnected()) {
            view.send(new ErrorMessage(code, desc));
        }
    }

    public void sendTo(String nickname, ServerMessage msg) {
        VirtualView view = views.get(nickname);
        if (view != null && view.isConnected()) {
            view.send(msg);
        }
    }

    // GESTIONE DEL MODEL

    public void setGame(GameActions game) {
        this.game = game;
    }

    public GameActions getGame() {
        return game;
    }

    public void transitionTo(ControllerPhase phase) {
        this.currentPhase = phase;
    }

    public ControllerPhase getCurrentPhase() {
        return currentPhase;
    }

    // TERMINAZIONE DELLE VIEW

    public void closeAll() {
        for (VirtualView view : views.values()) {
            view.disconnect();
        }
        views.clear();
    }
}

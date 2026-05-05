package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to initialize the client with the full game state.
 * This is a thin wrapper around {@link GameStateSnapshot}.
 * It is used only at game start to build the client-side model.
 *
 * @author Andrea Markvukaj
 */
public class GameStartedMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final GameStateSnapshot snapshot;

    public GameStartedMessage(GameStateSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public GameStateSnapshot getDto() {
        return snapshot;
    }

    @Override
    public void apply(View view) {
        view.getClientModel().applyGameStarted(snapshot);
        view.render();
    }
}
package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.view.View;

import java.util.List;

/**
 * Message sent by the server to update clients about the saved-game recovery status.
 *
 * @author Andrea Markvukaj
 */
public class RecoveryUpdateMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final List<String> reconnectedPlayers;
    private final List<String> missingPlayers;

    /**
     * Creates a new recovery update message.
     *
     * @param reconnectedPlayers the nicknames of the players who already reconnected
     * @param missingPlayers the nicknames of the players still missing from recovery
     */
    public RecoveryUpdateMessage(List<String> reconnectedPlayers, List<String> missingPlayers) {

        this.reconnectedPlayers = List.copyOf(reconnectedPlayers);
        this.missingPlayers = List.copyOf(missingPlayers);
    }

    /**
     * Applies this message to the given view.
     */
    @Override
    public void apply(View view) {
        view.showRecoveryUpdate(reconnectedPlayers, missingPlayers);
    }
}
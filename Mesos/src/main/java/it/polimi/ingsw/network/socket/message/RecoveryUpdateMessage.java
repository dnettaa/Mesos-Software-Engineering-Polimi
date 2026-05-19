package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.view.View;

import java.util.List;

public class RecoveryUpdateMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final List<String> reconnectedPlayers;
    private final List<String> missingPlayers;

    public RecoveryUpdateMessage(List<String> reconnectedPlayers, List<String> missingPlayers) {

        this.reconnectedPlayers = List.copyOf(reconnectedPlayers);
        this.missingPlayers = List.copyOf(missingPlayers);
    }

    @Override
    public void apply(View view) {
        view.showRecoveryUpdate(reconnectedPlayers, missingPlayers);
    }
}
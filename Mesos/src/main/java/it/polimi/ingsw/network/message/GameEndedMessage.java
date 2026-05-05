package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify that the game has ended.
 * Thin wrapper around {@link GameEndedDTO}.
 *
 * @author Andrea Markvukaj
 */
public class GameEndedMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final GameEndedDTO dto;

    public GameEndedMessage(GameEndedDTO dto) {
        this.dto = dto;
    }

    public GameEndedDTO getDto() {
        return dto;
    }

    @Override
    public void apply(View view) {
        view.getClientModel().applyGameEnded(dto);
        view.render();
    }
}
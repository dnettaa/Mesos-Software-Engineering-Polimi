package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.model.game.DTO.ExtraCardTakenDTO;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify that a player has taken an extra card.
 * Thin wrapper around {@link ExtraCardTakenDTO}.
 *
 * @author Andrea Markvukaj
 */
public class ExtraCardTakenMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final ExtraCardTakenDTO dto;

    public ExtraCardTakenMessage(ExtraCardTakenDTO dto) {
        this.dto = dto;
    }

    public ExtraCardTakenDTO getDto() {
        return dto;
    }

    @Override
    public void apply(View view) {
        view.getClientModel().applyExtraCardTaken(dto);
        view.render();
    }
}
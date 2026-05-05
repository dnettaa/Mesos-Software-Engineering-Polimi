package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.game.DTO.TotemPlacedDTO;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify that a totem has been placed.
 * Thin wrapper around {@link TotemPlacedDTO}.
 *
 * @author Andrea Markvukaj
 */
public class TotemPlacedMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final TotemPlacedDTO dto;

    public TotemPlacedMessage(TotemPlacedDTO dto) {
        this.dto = dto;
    }

    public TotemPlacedDTO getDto() {
        return dto;
    }

    @Override
    public void apply(View view) {
        view.getClientModel().applyTotemPlaced(dto);
        view.render();
    }
}
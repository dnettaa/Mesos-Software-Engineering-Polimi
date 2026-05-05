package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.model.game.DTO.EventResolvedDTO;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify that an event has been resolved.
 * Thin wrapper around {@link EventResolvedDTO}.
 *
 * @author Andrea Markvukaj
 */
public class EventResolvedMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final EventResolvedDTO dto;

    public EventResolvedMessage(EventResolvedDTO dto) {
        this.dto = dto;
    }

    public EventResolvedDTO getDto() {
        return dto;
    }

    @Override
    public void apply(View view) {
        view.getClientModel().applyEventResolved(dto);
        view.render();
    }
}
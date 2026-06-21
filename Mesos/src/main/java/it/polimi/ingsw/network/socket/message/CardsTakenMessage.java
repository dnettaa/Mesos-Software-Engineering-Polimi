package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.model.game.DTO.CardsTakenDTO;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify that a player has taken cards.
 * Thin wrapper around {@link CardsTakenDTO}.
 *
 * @author Andrea Markvukaj
 */
public class CardsTakenMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final CardsTakenDTO dto;

    public CardsTakenMessage(CardsTakenDTO dto) {
        this.dto = dto;
    }

    public CardsTakenDTO getDto() {
        return dto;
    }

    /**
     * Applies this message to the given view.
     */
    @Override
    public void apply(View view) {
        view.getClientModel().applyCardsTaken(dto);
        view.render();
    }
}
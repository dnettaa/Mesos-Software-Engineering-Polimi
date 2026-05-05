package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.game.DTO.RoundEndedDTO;
import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify that a round has ended.
 * Thin wrapper around {@link RoundEndedDTO}.
 *
 * @author Andrea Markvukaj
 */
public class RoundEndedMessage extends ServerMessage {

    private static final long serialVersionUID = 1L;

    private final RoundEndedDTO dto;

    public RoundEndedMessage(RoundEndedDTO dto) {
        this.dto = dto;
    }

    public RoundEndedDTO getDto() {
        return dto;
    }

    @Override
    public void apply(View view) {
        view.getClientModel().applyRoundEnded(dto);
        view.render();
    }
}
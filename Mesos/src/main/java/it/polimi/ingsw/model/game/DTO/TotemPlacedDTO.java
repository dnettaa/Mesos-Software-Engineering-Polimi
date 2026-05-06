package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

public record TotemPlacedDTO(
        String placerNickname,
        char slotID,
        String nextPlayerNickname,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}
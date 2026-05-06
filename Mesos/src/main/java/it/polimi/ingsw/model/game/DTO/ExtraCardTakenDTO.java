package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

public record ExtraCardTakenDTO(
        String nickname,
        String cardID,
        boolean fromUpperRow,
        boolean isBuilding,
        int foodDelta,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

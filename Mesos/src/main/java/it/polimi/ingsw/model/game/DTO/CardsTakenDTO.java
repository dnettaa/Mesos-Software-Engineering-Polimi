package it.polimi.ingsw.model.game.DTO;

import java.util.List;
import java.io.Serializable;

public record CardsTakenDTO(
        String nickname,
        List<String> takenUpperIDs,
        List<String> takenLowerIDs,
        List<String> addedTribeCardIDs,
        List<String> addedBuildingIDs,
        int foodDelta,
        int ppDelta,
        char freedSlotID,
        int turnOrderPosition,
        String nextPlayerNickname,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

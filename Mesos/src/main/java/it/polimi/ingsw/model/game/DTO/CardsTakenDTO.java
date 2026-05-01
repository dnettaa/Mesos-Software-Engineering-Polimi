package it.polimi.ingsw.model.game.DTO;

import java.util.List;

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
) {}

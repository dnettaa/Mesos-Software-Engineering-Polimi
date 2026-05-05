package it.polimi.ingsw.model.game.DTO;

public record ExtraCardTakenDTO(
        String nickname,
        String cardID,
        boolean fromUpperRow,
        boolean isBuilding,
        int foodDelta,
        String nextPhaseName
) {}

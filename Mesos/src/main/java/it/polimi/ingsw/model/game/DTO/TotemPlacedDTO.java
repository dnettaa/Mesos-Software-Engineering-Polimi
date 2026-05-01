package it.polimi.ingsw.model.game.DTO;

public record TotemPlacedDTO(
        String placerNickname,
        char slotID,
        String nextPlayerNickname,
        String currentPhaseName
) {}
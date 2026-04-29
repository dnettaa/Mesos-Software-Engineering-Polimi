package it.polimi.ingsw.model.game.DTO;

import it.polimi.ingsw.model.game.Era;

import java.util.List;

public record RoundEndedDTO(
        List<String> discardedLowerTribeIDs,
        List<String> discardedLowerEventIDs,
        List<String> movedUpperToLowerTribeIDs,
        List<String> discardedLowerBuildingIDs,
        List<String> movedUpperToLowerBuildingIDs,
        List<String> newUpperRowIDs,
        List<String> revealedBuildingIDs,
        Era newEra,
        int newRound,
        List<String> newTurnOrder,
        String firstPlayerNickname,
        int tribeDeckRemaining
) {}

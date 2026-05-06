package it.polimi.ingsw.model.game.DTO;

import it.polimi.ingsw.model.game.Era;

import java.util.List;
import java.io.Serializable;

public record GameStateSnapshot(
        int currentRound,
        Era currentEra,
        String currentPhaseName,
        String currentPlayerNickname,
        List<String> placementOrder,
        List<Character> resolutionOrder,
        List<String> turnOrder,
        int tribeDeckRemaining,
        List<String> upperRowCardIDs,
        List<String> lowerRowCardIDs,
        List<OfferSlotData> offerSlots,
        List<PlayerData> players
) implements Serializable{
    private static final long serialVersionUID = 1L;
}


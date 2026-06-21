package it.polimi.ingsw.model.game.DTO;

import it.polimi.ingsw.model.game.Era;

import java.util.List;
import java.io.Serializable;

/**
 * Complete serializable snapshot of the public game state sent to clients.
 *
 * @param currentRound the current round number
 * @param currentEra the current game era
 * @param currentPhaseName the name of the current phase
 * @param currentPlayerNickname the nickname of the player currently expected to act
 * @param placementOrder player nicknames in totem placement order
 * @param resolutionOrder offer slot identifiers in resolution order
 * @param turnOrder player nicknames in turn order
 * @param tribeDeckRemaining the number of cards left in the tribe deck
 * @param upperRowCardIDs identifiers of cards currently in the upper row
 * @param lowerRowCardIDs identifiers of cards currently in the lower row
 * @param offerSlots current public data for each offer slot
 * @param players current public data for each player
 *
 * @author Luca Grecchi
 */
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


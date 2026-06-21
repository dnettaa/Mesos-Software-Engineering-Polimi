package it.polimi.ingsw.model.game.DTO;

import it.polimi.ingsw.model.game.Era;

import java.util.List;
import java.io.Serializable;

/**
 * DTO broadcast after the board has been refreshed for a new round.
 *
 * @param discardedLowerTribeIDs identifiers of tribe cards discarded from the lower row
 * @param discardedLowerEventIDs identifiers of event cards discarded from the lower row
 * @param movedUpperToLowerTribeIDs identifiers of tribe cards moved from upper to lower row
 * @param discardedLowerBuildingIDs identifiers of building cards discarded from the lower row
 * @param movedUpperToLowerBuildingIDs identifiers of building cards moved from upper to lower row
 * @param newUpperRowIDs identifiers of cards newly placed in the upper row
 * @param newLowerRowIDs identifiers of cards newly placed in the lower row
 * @param revealedBuildingIDs identifiers of newly revealed building cards
 * @param newEra the era active after the round transition
 * @param newRound the round number reached after the transition
 * @param newTurnOrder player nicknames in the new turn order
 * @param firstPlayerNickname the nickname of the first player of the new round
 * @param tribeDeckRemaining the number of cards left in the tribe deck
 *
 * @author Luca Grecchi
 */
public record RoundEndedDTO(
        List<String> discardedLowerTribeIDs,
        List<String> discardedLowerEventIDs,
        List<String> movedUpperToLowerTribeIDs,
        List<String> discardedLowerBuildingIDs,
        List<String> movedUpperToLowerBuildingIDs,
        List<String> newUpperRowIDs,
        List<String> newLowerRowIDs,
        List<String> revealedBuildingIDs,
        Era newEra,
        int newRound,
        List<String> newTurnOrder,
        String firstPlayerNickname,
        int tribeDeckRemaining
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

package it.polimi.ingsw.model.game.DTO;

import java.util.List;
import java.io.Serializable;

/**
 * DTO broadcast after a player resolves an offer slot and takes cards.
 *
 * @param nickname the nickname of the player who took the cards
 * @param takenUpperIDs identifiers of the cards taken from the upper row
 * @param takenLowerIDs identifiers of the cards taken from the lower row
 * @param addedTribeCardIDs identifiers of the tribe cards added to the player's tribe
 * @param addedBuildingIDs identifiers of the building cards added to the player's buildings
 * @param foodDelta the food variation applied to the player
 * @param ppDelta the prestige point variation applied to the player
 * @param freedSlotID the identifier of the offer slot freed by the action
 * @param nextPlayerNickname the nickname of the next player
 * @param nextPhaseName the name of the phase reached after the action
 *
 * @author Luca Grecchi
 */
public record CardsTakenDTO(
        String nickname,
        List<String> takenUpperIDs,
        List<String> takenLowerIDs,
        List<String> addedTribeCardIDs,
        List<String> addedBuildingIDs,
        int foodDelta,
        int ppDelta,
        char freedSlotID,
        String nextPlayerNickname,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

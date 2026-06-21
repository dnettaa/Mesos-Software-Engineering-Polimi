package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

/**
 * DTO broadcast when a player takes an extra card granted by a special effect.
 *
 * @param nickname the nickname of the player who took the extra card
 * @param cardID the identifier of the taken card
 * @param fromUpperRow true if the card was taken from the upper row
 * @param isBuilding true if the taken card is a building card
 * @param foodDelta the food variation applied to the player
 * @param nextPhaseName the name of the phase reached after the action
 *
 * @author Luca Grecchi
 */
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

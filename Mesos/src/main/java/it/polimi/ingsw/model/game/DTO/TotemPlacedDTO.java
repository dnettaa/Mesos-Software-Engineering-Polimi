package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

/**
 * DTO broadcast after a player places a totem on an offer slot.
 *
 * @param placerNickname the nickname of the player who placed the totem
 * @param slotID the identifier of the occupied offer slot
 * @param nextPlayerNickname the nickname of the next player, if any
 * @param nextPhaseName the name of the phase reached after the placement
 *
 * @author Luca Grecchi
 */
public record TotemPlacedDTO(
        String placerNickname,
        char slotID,
        String nextPlayerNickname,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}
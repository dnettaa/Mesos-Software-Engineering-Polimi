package it.polimi.ingsw.model.game.DTO;

import it.polimi.ingsw.model.player.TotemColor;

import java.io.Serializable;
import java.util.List;

/**
 * Public serializable snapshot of a player's state.
 *
 * @param nickname the player's nickname
 * @param totemColor the player's totem color
 * @param food the player's current food amount
 * @param prestigePoints the player's current prestige points
 * @param tribeCardID identifiers of the player's tribe cards
 * @param buildingID identifiers of the player's building cards
 *
 * @author Luca Grecchi
 */
public record PlayerData(
        String nickname,
        TotemColor totemColor,
        int food,
        int prestigePoints,
        List<String> tribeCardID,
        List<String> buildingID
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public PlayerData {
        tribeCardID = List.copyOf(tribeCardID);
        buildingID = List.copyOf(buildingID);
    }
}
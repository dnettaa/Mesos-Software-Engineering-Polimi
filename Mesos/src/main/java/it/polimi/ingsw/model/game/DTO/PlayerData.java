package it.polimi.ingsw.model.game.DTO;

import it.polimi.ingsw.model.player.TotemColor;

import java.io.Serializable;
import java.util.List;

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
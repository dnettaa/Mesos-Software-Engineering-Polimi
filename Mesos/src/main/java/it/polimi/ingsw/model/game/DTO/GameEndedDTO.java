package it.polimi.ingsw.model.game.DTO;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

public record GameEndedDTO(
        Map<String, Integer> finalPPByPlayer,
        Map<String, Integer> endGameBonusByPlayer,
        List<String> ranking
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

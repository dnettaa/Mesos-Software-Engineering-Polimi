package it.polimi.ingsw.model.game.DTO;

import java.util.List;
import java.util.Map;

public record GameEndedDTO(
        Map<String, Integer> finalPPByPlayer,
        Map<String, Integer> endGameBonusByPlayer,
        List<String> ranking
) {
}

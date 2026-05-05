package it.polimi.ingsw.model.game.DTO;

import java.util.Map;

public record EventResolvedDTO(
        String eventCardID,
        String eventType,
        Map<String, Integer> ppDeltaByPlayer,
        Map<String, Integer> foodDeltaByPlayer,
        String nextPhaseName
) {}

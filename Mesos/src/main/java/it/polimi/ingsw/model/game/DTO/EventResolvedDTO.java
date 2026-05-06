package it.polimi.ingsw.model.game.DTO;

import java.util.Map;
import java.io.Serializable;

public record EventResolvedDTO(
        String eventCardID,
        String eventType,
        Map<String, Integer> ppDeltaByPlayer,
        Map<String, Integer> foodDeltaByPlayer,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

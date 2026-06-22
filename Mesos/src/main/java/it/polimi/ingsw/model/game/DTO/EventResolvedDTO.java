package it.polimi.ingsw.model.game.DTO;

import java.util.Map;
import java.io.Serializable;

/**
 * DTO broadcast after an event card has been resolved.
 *
 * @param eventCardID the identifier of the resolved event card
 * @param eventType the concrete event card type
 * @param ppDeltaByPlayer prestige point variations indexed by player nickname
 * @param foodDeltaByPlayer food variations indexed by player nickname
 * @param nextPhaseName the name of the phase reached after event resolution
 *
 * @author Luca Grecchi
 */
public record EventResolvedDTO(
        String eventCardID,
        String eventType,
        Map<String, Integer> ppDeltaByPlayer,
        Map<String, Integer> foodDeltaByPlayer,
        String nextPhaseName
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

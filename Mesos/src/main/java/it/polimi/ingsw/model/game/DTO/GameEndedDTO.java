package it.polimi.ingsw.model.game.DTO;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * DTO broadcast when the game ends and final scoring has been computed.
 *
 * @param finalPPByPlayer final prestige points indexed by player nickname
 * @param endGameBonusByPlayer end-game bonus points indexed by player nickname
 * @param ranking player nicknames ordered from first to last place
 *
 * @author Luca Grecchi
 */
public record GameEndedDTO(
        Map<String, Integer> finalPPByPlayer,
        Map<String, Integer> endGameBonusByPlayer,
        List<String> ranking
) implements Serializable{
    private static final long serialVersionUID = 1L;
}

package it.polimi.ingsw.leaderboard;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents the result of a completed match for a single player.
 *
 * @param nickname the player's nickname
 * @param finalScore the final score achieved
 * @param playerCount number of players in the match
 * @param timestamp when the match was played
 *
 * @author Andrea Markvukaj
 */
public record MatchResult(
        String nickname,
        int finalScore,
        int playerCount,
        LocalDateTime timestamp
) implements Serializable {}
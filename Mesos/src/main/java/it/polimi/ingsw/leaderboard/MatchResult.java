package it.polimi.ingsw.leaderboard;

import java.time.LocalDateTime;

public record MatchResult(
        String nickname,
        int finalScore,
        int playerCount,
        LocalDateTime timestamp
) {}
package it.polimi.ingsw.leaderboard;

import java.util.List;

public interface MatchResultRepository {

    void save(MatchResult result);

    List<MatchResult> findByPlayerCount(int playerCount);
}

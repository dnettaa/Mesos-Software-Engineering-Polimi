package it.polimi.ingsw.leaderboard;

import java.util.List;

/**
 * Repository interface for storing and retrieving {@link MatchResult} objects.
 * Provides an abstraction over the persistence layer, allowing different
 * implementations ( in-memory or SQL-based).
 *
 * @author Andrea Markvukaj
 */
public interface MatchResultRepository {

    /**
     * Saves a match result.
     *
     * @param result the match result to store
     */
    void save(MatchResult result);

    /**
     * Retrieves all match results for a given number of players.
     *
     * @param playerCount the number of players in the match
     * @return a list of matching results
     */
    List<MatchResult> findByPlayerCount(int playerCount);
}

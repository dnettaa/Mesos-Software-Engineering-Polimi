package it.polimi.ingsw.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link MatchResultRepository}.
 * Stores match results in a local list without using a database.
 * Mainly used for testing or as a fallback when a database is not available.
 *
 * @author Andrea Markvukaj
 */
public class InMemoryMatchResultRepository implements MatchResultRepository {

    private final List<MatchResult> results = new ArrayList<>();

    /**
     * Stores a match result in memory.
     *
     * @param result the match result to store
     */
    @Override
    public void save(MatchResult result) {
        results.add(result);
    }

    /**
     * Returns all match results with the specified number of players.
     *
     * @param playerCount the number of players in the match
     * @return a list of matching results
     */
    @Override
    public List<MatchResult> findByPlayerCount(int playerCount) {
        //per ogni risultato prende il playerCount e tiene solo quelli uguali.
        return results.stream()
                .filter(r -> r.playerCount() == playerCount)
                .collect(Collectors.toList());
    }
}

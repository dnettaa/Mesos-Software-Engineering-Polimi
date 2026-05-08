package it.polimi.ingsw.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryMatchResultRepository implements MatchResultRepository {

    private final List<MatchResult> results = new ArrayList<>();

    @Override
    public void save(MatchResult result) {
        results.add(result);
    }

    @Override
    public List<MatchResult> findByPlayerCount(int playerCount) {
        //per ogni risultato prende il playerCount e tiene solo quelli uguali.
        return results.stream()
                .filter(r -> r.playerCount() == playerCount)
                .collect(Collectors.toList());
    }
}

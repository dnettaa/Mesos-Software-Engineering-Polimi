package it.polimi.ingsw.leaderboard;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests an end-to-end leaderboard flow with multiple recorded games using an
 * in-memory SQL repository fake. This replaces the old manual database/TUI smoke test
 * with deterministic JUnit assertions.
 *
 * @author Diana
 */
class LeaderboardTest {

    /**
     * Verifies that several completed games can be recorded and then queried as
     * a sorted leaderboard for the selected player count.
     * Setup: three two-player game results are recorded through {@link RankingService}.
     * Action: request the two-player ranking and Roberto's position.
     * Expected behavior: the ranking keeps each player's best score and Roberto is placed after higher-scoring players.
     * Edge case covered: repeated recordings by the same player are collapsed to the player's best result.
     */
    @Test
    void leaderboardShouldRecordMultipleGamesAndReturnSortedRanking() {
        RankingService service = new RankingService(new InMemoryMatchResultRepository());

        service.recordGame(new GameEndedDTO(
                Map.of("Roberto", 43, "Paolo", -19),
                Map.of("Roberto", 13, "Paolo", 12),
                List.of("Roberto", "Paolo")
        ), 2);
        service.recordGame(new GameEndedDTO(
                Map.of("Roberto", 30, "Paolo", 50),
                Map.of("Roberto", 5, "Paolo", 10),
                List.of("Paolo", "Roberto")
        ), 2);
        service.recordGame(new GameEndedDTO(
                Map.of("Riccardo", 100, "Giuseppe", -12),
                Map.of("Riccardo", 5, "Giuseppe", 10),
                List.of("Riccardo", "Giuseppe")
        ), 2);

        List<MatchResult> ranking = service.getRanking(2);
        int robertoPosition = service.getPlayerPosition("Roberto", 2);

        assertEquals(List.of("Riccardo", "Paolo", "Roberto", "Giuseppe"),
                ranking.stream().map(MatchResult::nickname).toList());
        assertEquals(List.of(100, 50, 43, -12),
                ranking.stream().map(MatchResult::finalScore).toList());
        assertEquals(3, robertoPosition);
    }

    /**
     * In-memory SQL repository fake used to test leaderboard behavior without requiring
     * PostgreSQL or TUI rendering.
     */
    private static class InMemoryMatchResultRepository extends SqlMatchResultRepository {

        private final List<MatchResult> results = new ArrayList<>();

        private InMemoryMatchResultRepository() {
            super("", "", "");
        }

        @Override
        public void save(MatchResult result) {
            results.add(result);
        }

        @Override
        public List<MatchResult> findByPlayerCount(int playerCount) {
            return results.stream()
                    .filter(result -> result.playerCount() == playerCount)
                    .collect(Collectors.toMap(
                            MatchResult::nickname,
                            result -> result,
                            (first, second) -> betterResult(first, second)
                    ))
                    .values()
                    .stream()
                    .toList();
        }

        private MatchResult betterResult(MatchResult first, MatchResult second) {
            return Comparator.comparingInt(MatchResult::finalScore)
                    .thenComparing(MatchResult::timestamp)
                    .compare(first, second) >= 0 ? first : second;
        }
    }
}

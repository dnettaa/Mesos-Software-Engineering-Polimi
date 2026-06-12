package it.polimi.ingsw.leaderboard;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the leaderboard business service using an in-memory repository.
 * The suite verifies result recording, ranking order, and player-position lookup
 * without depending on a PostgreSQL database.
 *
 * @author Diana
 */
class RankingServiceTest {

    /**
     * Verifies that recording a completed game stores one match result per player.
     * Setup: a completed two-player game DTO and an empty in-memory repository.
     * Action: record the game through {@link RankingService#recordGame(GameEndedDTO, int)}.
     * Expected behavior: the repository contains one result for each player with the provided player count.
     * Edge case covered: every player in the final score map must be persisted independently.
     */
    @Test
    void recordGameShouldSaveOneResultForEachPlayer() {
        InMemoryMatchResultRepository repository = new InMemoryMatchResultRepository();
        RankingService service = new RankingService(repository);
        GameEndedDTO gameEndedDTO = new GameEndedDTO(
                Map.of("Diana", 42, "Luca", 35),
                Map.of("Diana", 5, "Luca", 3),
                List.of("Diana", "Luca")
        );

        service.recordGame(gameEndedDTO, 2);

        assertEquals(2, repository.savedResults.size());
        assertEquals(2, repository.savedResults.get(0).playerCount());
        assertEquals(2, repository.savedResults.get(1).playerCount());
        assertEquals(42, findScore(repository.savedResults, "Diana"));
        assertEquals(35, findScore(repository.savedResults, "Luca"));
    }

    /**
     * Verifies that rankings are sorted by score in descending order.
     * Setup: an in-memory repository containing unsorted results for the same player count.
     * Action: request the ranking for two-player games.
     * Expected behavior: the highest score appears first, followed by lower scores.
     * Edge case covered: the service must not depend on repository insertion order.
     */
    @Test
    void getRankingShouldSortResultsByFinalScoreDescending() {
        InMemoryMatchResultRepository repository = new InMemoryMatchResultRepository();
        repository.savedResults.add(new MatchResult("Luca", 25, 2, LocalDateTime.of(2026, 1, 1, 10, 0)));
        repository.savedResults.add(new MatchResult("Diana", 50, 2, LocalDateTime.of(2026, 1, 1, 9, 0)));
        repository.savedResults.add(new MatchResult("Vadym", 40, 2, LocalDateTime.of(2026, 1, 1, 11, 0)));
        RankingService service = new RankingService(repository);

        List<MatchResult> ranking = service.getRanking(2);

        assertEquals(List.of("Diana", "Vadym", "Luca"), ranking.stream().map(MatchResult::nickname).toList());
        assertEquals(List.of(50, 40, 25), ranking.stream().map(MatchResult::finalScore).toList());
    }

    /**
     * Verifies that ranking ties are resolved by the most recent timestamp.
     * Setup: two results with the same final score and different timestamps.
     * Action: request the ranking for two-player games.
     * Expected behavior: the most recent result appears before the older one.
     * Edge case covered: deterministic ordering is preserved when scores are equal.
     */
    @Test
    void getRankingShouldSortEqualScoresByMostRecentTimestamp() {
        InMemoryMatchResultRepository repository = new InMemoryMatchResultRepository();
        repository.savedResults.add(new MatchResult("Diana", 50, 2, LocalDateTime.of(2026, 1, 1, 9, 0)));
        repository.savedResults.add(new MatchResult("Luca", 50, 2, LocalDateTime.of(2026, 1, 1, 10, 0)));
        RankingService service = new RankingService(repository);

        List<MatchResult> ranking = service.getRanking(2);

        assertEquals(List.of("Luca", "Diana"), ranking.stream().map(MatchResult::nickname).toList());
    }

    /**
     * Verifies that ranking retrieval filters results by player count through the repository.
     * Setup: the in-memory repository contains two-player and three-player results.
     * Action: request the ranking for three-player games.
     * Expected behavior: only results with matching player count are returned.
     * Edge case covered: leaderboards for different match sizes must remain separated.
     */
    @Test
    void getRankingShouldReturnOnlyResultsForRequestedPlayerCount() {
        InMemoryMatchResultRepository repository = new InMemoryMatchResultRepository();
        repository.savedResults.add(new MatchResult("Diana", 50, 2, LocalDateTime.of(2026, 1, 1, 9, 0)));
        repository.savedResults.add(new MatchResult("Luca", 60, 3, LocalDateTime.of(2026, 1, 1, 10, 0)));
        RankingService service = new RankingService(repository);

        List<MatchResult> ranking = service.getRanking(3);

        assertEquals(1, ranking.size());
        assertEquals("Luca", ranking.getFirst().nickname());
        assertEquals(3, ranking.getFirst().playerCount());
    }

    /**
     * Verifies that player positions are one-based in the sorted ranking.
     * Setup: an in-memory repository containing three ranked players.
     * Action: request the position of the second-ranked player.
     * Expected behavior: the returned position is 2.
     * Edge case covered: position lookup must use sorted ranking order, not repository insertion order.
     */
    @Test
    void getPlayerPositionShouldReturnOneBasedPositionInSortedRanking() {
        InMemoryMatchResultRepository repository = new InMemoryMatchResultRepository();
        repository.savedResults.add(new MatchResult("Luca", 25, 2, LocalDateTime.of(2026, 1, 1, 10, 0)));
        repository.savedResults.add(new MatchResult("Diana", 50, 2, LocalDateTime.of(2026, 1, 1, 9, 0)));
        repository.savedResults.add(new MatchResult("Vadym", 40, 2, LocalDateTime.of(2026, 1, 1, 11, 0)));
        RankingService service = new RankingService(repository);

        int position = service.getPlayerPosition("Vadym", 2);

        assertEquals(2, position);
    }

    /**
     * Verifies that an absent player has no leaderboard position.
     * Setup: an in-memory repository containing rankings for other players.
     * Action: request the position of a nickname absent from the ranking.
     * Expected behavior: the service returns -1.
     * Edge case covered: callers can distinguish missing players from valid one-based positions.
     */
    @Test
    void getPlayerPositionShouldReturnMinusOneWhenPlayerIsNotRanked() {
        InMemoryMatchResultRepository repository = new InMemoryMatchResultRepository();
        repository.savedResults.add(new MatchResult("Diana", 50, 2, LocalDateTime.of(2026, 1, 1, 9, 0)));
        RankingService service = new RankingService(repository);

        int position = service.getPlayerPosition("Unknown", 2);

        assertEquals(-1, position);
    }

    private int findScore(List<MatchResult> results, String nickname) {
        return results.stream()
                .filter(result -> result.nickname().equals(nickname))
                .findFirst()
                .orElseThrow()
                .finalScore();
    }

    /**
     * In-memory repository used to test service behavior without database access.
     */
    private static class InMemoryMatchResultRepository implements MatchResultRepository {

        private final List<MatchResult> savedResults = new ArrayList<>();

        @Override
        public void save(MatchResult result) {
            savedResults.add(result);
        }

        @Override
        public List<MatchResult> findByPlayerCount(int playerCount) {
            return savedResults.stream()
                    .filter(result -> result.playerCount() == playerCount)
                    .toList();
        }
    }
}

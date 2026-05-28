package it.polimi.ingsw.leaderboard;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Service class responsible for managing leaderboard logic.
 * This class acts as the business layer between the game model and the persistence layer
 * ({@link MatchResultRepository}).
 * It provides methods to:
 *     Record the results of a completed game.
 *     Retrieve rankings filtered by number of players.
 *     Compute the position of a specific player in the ranking.
 * The ranking is based on the final score of each player, computed as the sum of
 * final prestige points and end-game bonuses.
 *
 * @author Andrea Markvukaj
 */
public class RankingService {

    private final MatchResultRepository repository;

    public RankingService(MatchResultRepository repository) {
        this.repository = repository;
    }

    /**
     * Records the results of a completed game.
     * For each player in the provided {@link GameEndedDTO}, a {@link MatchResult}
     * is created and saved using the repository.
     *
     * @param dto the data transfer object containing final game results
     * @param playerCount the number of players in the match
     */
    public void recordGame(GameEndedDTO dto, int playerCount) {

        LocalDateTime now = LocalDateTime.now();

        for(String nickname : dto.finalPPByPlayer().keySet()) {

            int finalScore = dto.finalPPByPlayer().get(nickname);

            MatchResult result = new MatchResult(nickname, finalScore, playerCount, now);

            repository.save(result);
        }
    }

    /**
     * Returns the ranking of players for matches with a given number of players.
     * The repository already collapses repeated games by nickname, keeping each
     * player's best stored score. Results are sorted in descending order based on
     * final score, with the most recent timestamp used as tie-breaker.
     *
     * @param playerCount the number of players in the match
     * @return a list of {@link MatchResult} sorted by score (highest first)
     */
    public List<MatchResult> getRanking(int playerCount) {

        return repository.findByPlayerCount(playerCount)
                .stream()
                .sorted(Comparator.comparingInt(MatchResult::finalScore)
                        .reversed()
                        .thenComparing(MatchResult::timestamp, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the position of a specific player within the ranking.
     * The ranking is computed for the given number of players and the position
     * is 1-based (the top player has position 1).
     *
     * @param nickname the nickname of the player
     * @param playerCount the number of players in the match
     * @return the player's position in the ranking, or -1 if not found
     */
    public int getPlayerPosition(String nickname, int playerCount) {
        List<MatchResult> ranking = getRanking(playerCount);

        for(int i = 0; i < ranking.size(); i++) {

            if(ranking.get(i).nickname().equals(nickname)) {
                return i + 1;
            }
        }

        return -1; //non trovato
    }
}

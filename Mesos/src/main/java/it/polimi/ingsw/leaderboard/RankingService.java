package it.polimi.ingsw.leaderboard;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingService {

    private final MatchResultRepository repository;

    public RankingService(MatchResultRepository repository) {
        this.repository = repository;
    }

    public void recordGame(GameEndedDTO dto, int playerCount) {

        LocalDateTime now = LocalDateTime.now();

        for(String nickname : dto.finalPPByPlayer().keySet()) {

            int finalPP = dto.finalPPByPlayer().get(nickname);
            int bonus = dto.endGameBonusByPlayer().getOrDefault(nickname, 0);

            int finalScore = finalPP + bonus;

            MatchResult result = new MatchResult(nickname, finalScore, playerCount, now);

            repository.save(result);
        }
    }

    public List<MatchResult> getRanking(int playerCount) {

        return repository.findByPlayerCount(playerCount)
                .stream()
                .sorted(Comparator.comparingInt(MatchResult::getFinalScore).reversed())
                .collect(Collectors.toList());
    }

    public int getPlayerPosition(String nickname, int playerCount) {
        List<MatchResult> ranking = getRanking(playerCount);

        for(int i = 0; i < ranking.size(); i++) {

            if(ranking.get(i).getNickname().equals(nickname)) {
                return i + 1;
            }
        }

        return -1; //non trovato
    }
}

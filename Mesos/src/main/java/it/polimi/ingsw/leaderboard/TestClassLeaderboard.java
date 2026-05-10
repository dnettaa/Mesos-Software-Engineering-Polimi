package it.polimi.ingsw.leaderboard;

import it.polimi.ingsw.model.game.DTO.GameEndedDTO;

import java.util.List;
import java.util.Map;

public class TestClassLeaderboard {

    public static void main(String[] args) {

        RankingService service = new RankingService(new InMemoryMatchResultRepository());

        // ── PARTITA 1 ─────────────────────────────────────────
        Map<String, Integer> finalPP1 = Map.of(
                "Roberto", 43,
                "Paolo", -19
        );

        Map<String, Integer> bonus1 = Map.of(
                "Roberto", 13,
                "Paolo", 12
        );

        GameEndedDTO dto1 = new GameEndedDTO(
                finalPP1,
                bonus1,
                List.of("Roberto", "Paolo")
        );

        service.recordGame(dto1, 2);

        // ── PARTITA 2 (simulata, stessi player ma score diversi) ───────────────
        Map<String, Integer> finalPP2 = Map.of(
                "Roberto", 30,
                "Paolo", 50
        );

        Map<String, Integer> bonus2 = Map.of(
                "Roberto", 5,
                "Paolo", 10
        );

        GameEndedDTO dto2 = new GameEndedDTO(
                finalPP2,
                bonus2,
                List.of("Paolo", "Roberto")
        );

        service.recordGame(dto2, 2);

        // ── PARTITA 3 (simulata, stessi player ma score diversi) ───────────────
        Map<String, Integer> finalPP3 = Map.of(
                "Riccardo", 100,
                "Giuseppe", -12
        );

        Map<String, Integer> bonus3 = Map.of(
                "Riccardo", 5,
                "Giuseppe", 10
        );

        GameEndedDTO dto3 = new GameEndedDTO(
                finalPP3,
                bonus3,
                List.of("Riccardo", "Giuseppe")
        );

        service.recordGame(dto3, 2);

        // ── STAMP LEADERBOARD ───────────────────────────────
        List<MatchResult> ranking = service.getRanking(2);

        System.out.println("\n=== LEADERBOARD (2 players) ===");

        for (int i = 0; i < ranking.size(); i++) {
            MatchResult r = ranking.get(i);

            System.out.printf(
                    "%d) %s - score: %d - date: %s%n",
                    i + 1,
                    r.nickname(),
                    r.finalScore(),
                    r.timestamp()
            );
        }
    }
}
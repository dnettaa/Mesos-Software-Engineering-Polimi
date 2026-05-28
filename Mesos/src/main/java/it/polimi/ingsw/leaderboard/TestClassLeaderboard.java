package it.polimi.ingsw.leaderboard;

import it.polimi.ingsw.config.ConfigLoader;
import it.polimi.ingsw.config.DBConfiguration;
import it.polimi.ingsw.model.game.DTO.GameEndedDTO;
import it.polimi.ingsw.view.TUI;

import java.util.List;
import java.util.Map;

public class TestClassLeaderboard {

    public static void main(String[] args) {

        // DATABASE CONFIGURATION
        DBConfiguration config = ConfigLoader.load();

        RankingService service;

        if (config != null) {
            SqlMatchResultRepository repository = new SqlMatchResultRepository(config.dbUrl, config.dbUser, config.dbPassword);
            if (!repository.testConnection()) {
                System.out.println("Database non disponibile");
                return;
            }
            service = new RankingService(repository);
            System.out.println("Using SQL DB");
        } else {
            System.out.println("Database non disponibile");
            return;
        }

        // PARTITA 1
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

        // PARTITA 2
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

        // PARTITA 3
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

        // SHOW WITH THE SAME RENDERING USED BY THE REAL TUI
        List<MatchResult> ranking = service.getRanking(2);
        int position = service.getPlayerPosition("Roberto", 2);

        TUI tui = new TUI();
        tui.showLeaderboard(ranking, position);
    }
}
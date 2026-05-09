package it.polimi.ingsw.leaderboard;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;


public class SqlMatchResultRepository implements MatchResultRepository {

    private final String url;
    private final String user;
    private final String password;

    public SqlMatchResultRepository(String url, String user, String password) {

        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException  {

        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void save(MatchResult result) {

        String query = "INSERT INTO match_results (nickname, final_score, player_count, played_at) VALUES (?, ?, ?, ?)";

        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, result.nickname());
            stmt.setInt(2, result.finalScore());
            stmt.setInt(3, result.playerCount());
            stmt.setTimestamp(4, Timestamp.valueOf(result.timestamp()));

            stmt.executeUpdate();

        } catch(SQLException e) {
            System.err.println("Errore durante save su database: " + e.getMessage());
        }

    }

    @Override
    public List<MatchResult> findByPlayerCount(int playerCount) {

        List<MatchResult> results = new ArrayList<>();

        String query = "SELECT nickname, final_score, player_count, played_at FROM match_results WHERE player_count = ?";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playerCount);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                MatchResult result = new MatchResult(
                        rs.getString("nickname"),
                        rs.getInt("final_score"),
                        rs.getInt("player_count"),
                        rs.getTimestamp("played_at").toLocalDateTime()
                );

                results.add(result);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la lettura database: " + e.getMessage());
        }

        return results;
    }
}

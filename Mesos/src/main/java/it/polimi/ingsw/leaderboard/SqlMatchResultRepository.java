package it.polimi.ingsw.leaderboard;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository responsible for storing and retrieving match results.
 * Persists match results in a PostgreSQL database using JDBC.
 * Handles saving and retrieving results through SQL queries.
 *
 * @author Andrea Markvukaj
 */
public class SqlMatchResultRepository {

    private final String url;
    private final String user;
    private final String password;

    public SqlMatchResultRepository(String url, String user, String password) {

        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Opens a new connection to the database using the configured credentials.
     *
     * @return an active {@link Connection} to the database
     * @throws SQLException if the connection cannot be established
     */
    private Connection getConnection() throws SQLException  {

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks if the database connection is available.
     *
     * @return true if the connection is valid, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2); // timeout 2 seconds
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Saves a match result into the database.
     *
     * @param result the match result to store
     */
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

    /**
     * Retrieves the best match result for each player with a given number of players.
     * All matches remain stored in the database, but leaderboard reads keep only
     * one row per nickname: highest score first, most recent timestamp as tie-breaker.
     *
     * @param playerCount the number of players in the match
     * @return a list of matching results
     */
    public List<MatchResult> findByPlayerCount(int playerCount) {

        List<MatchResult> results = new ArrayList<>();

        String query = """
                SELECT nickname,
                       final_score, 
                       player_count, 
                       played_at
                FROM (
                    SELECT nickname,
                           final_score,
                           player_count,
                           played_at,
                           ROW_NUMBER() OVER (
                               PARTITION BY nickname
                               ORDER BY final_score DESC, played_at DESC) AS RN
                    FROM match_results
                    WHERE player_count = ?
                ) best_results
                WHERE RN = 1
                """;

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

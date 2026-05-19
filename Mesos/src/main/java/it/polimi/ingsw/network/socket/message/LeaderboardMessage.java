package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.view.View;

import java.util.List;

/**
 * Server-to-client message that delivers the leaderboard data.
 * Contains the full ranking and the position of the requesting player.
 *
 * @author Andrea Markvukaj
 */
public class LeaderboardMessage extends ServerMessage {

    private final List<MatchResult> ranking;
    private final int position;

    public LeaderboardMessage(List<MatchResult> ranking, int position) {
        this.ranking = ranking;
        this.position = position;
    }

    /**
     * Applies the leaderboard data to the client view.
     *
     * @param view the client view
     */
    @Override
    public void apply(View view) {
        view.showLeaderboard(ranking, position);
    }
}
package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Message sent by the server to update all clients about the current lobby state.
 */

public class LobbyUpdateMessage extends ServerMessage{
    private final List<String> players;
    private final Map<String, TotemColor> colorsByPlayer;
    private final int exceptedPlayers;

    /**
     * Creates a new lobby update message.
     *
     * @param players the nicknames of the players currently in the lobby
     * @param colorsByPlayer the selected totem color for each player
     * @param expectedPlayers the number of players required to start the game
     */
    public LobbyUpdateMessage(List<String> players, Map<String, TotemColor> colorsByPlayer, int exceptedPlayers) {
        this.players = new ArrayList<>(players);
        this.colorsByPlayer = new HashMap<>(colorsByPlayer);
        this.exceptedPlayers = exceptedPlayers;
    }

    /**
     * Returns the nicknames of the players currently in the lobby.
     *
     * @return a copy of the lobby player list
     */
    public List<String> getPlayers(){
        return new ArrayList<>(players);
    }

    /**
     * Returns the selected totem color for each player.
     *
     * @return a copy of the player-color map
     */
    public Map<String, TotemColor> getColorsByPlayer(){
        return new HashMap<>(colorsByPlayer);
    }

    /**
     * Returns the number of players required to start the game.
     *
     * @return the expected number of players
     */
    public int getExpectedPlayers(){
        return exceptedPlayers;
    }

    /**
     * Applies this message to the given view.
     *
     * @param view the view that must display the lobby update
     */
    @Override
    public void apply(View view){
        view.showLobbyUpdate(getPlayers(), getColorsByPlayer(), exceptedPlayers);
    }
}
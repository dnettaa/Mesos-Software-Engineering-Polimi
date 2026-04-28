package it.polimi.ingsw.network.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.controller.GameController;

/**
 * Message sent by a client to create a new lobby.
 * <p>
 * The player who sends this message becomes the first player in the lobby
 * and defines the number of players required to start the game.
 */

public class CreateLobbyMessage extends ClientMessage{
    private final TotemColor color;
    private final int expectedPlayers;

    /**
     * Creates a new create-lobby message.
     *
     * @param nickname the nickname chosen by the player
     * @param color the totem color chosen by the player
     * @param expectedPlayers the number of players required to start the game
     */
    public CreateLobbyMessage(String nickname, TotemColor color, int expectedPlayers) {
        super(nickname);
        this.color = color;
        this.expectedPlayers = expectedPlayers;
    }

    /**
     * Returns the totem color chosen by the player.
     *
     * @return the selected totem color
     */
    public TotemColor getColor(){
        return color;
    }

    /**
     * Returns the number of players required to start the game.
     *
     * @return the expected number of players
     */
    public int getExpectedPlayers(){
        return expectedPlayers;
    }

    /**
     * Executes the create-lobby request on the controller.
     *
     * @param controller the game controller handling the request
     * @param sender the virtual view associated with the client who sent the message
     */
    @Override
    public void execute(GameController controller, VirtualView sender){
        controller.createLobby(getNickname(), color, expectedPlayers, sender);
    }
}
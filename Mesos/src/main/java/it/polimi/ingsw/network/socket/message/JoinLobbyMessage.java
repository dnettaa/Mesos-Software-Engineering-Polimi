package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.controller.GameController;

/**
 * Message sent by a client to join an existing lobby.
 *
 * @author Diana
 */

public class JoinLobbyMessage extends ClientMessage{
    private final TotemColor color;


    /**
     * Creates a new join-lobby message.
     *
     * @param nickname the nickname chosen by the player
     * @param color the totem color chosen by the player
     */
    public JoinLobbyMessage(String nickname, TotemColor color) {
        super(nickname);
        this.color = color;
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
     * Executes the join-lobby request on the controller.
     *
     * @param controller the game controller handling the request
     * @param sender the virtual view associated with the client who sent the message
     */
    @Override
    public void execute(GameController controller, VirtualView sender){
        controller.joinLobby(getNickname(), color, sender);
    }
}
package it.polimi.ingsw.message;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.viw.View;

/**
 * Message sent by the server to confirm that a player successfully joined the lobby.
 */

public class JoinSeccessMessage extends ServerMessage{
    private final String nickname;
    private final TotemColor color;

    /**
     * Creates a new join success message.
     *
     * @param nickname the nickname accepted by the server
     * @param color the totem color assigned to the player
     */
    public JoinSeccessMessage(String nickname, TotemColor color) {
        this.nickname = nickname;
        this.color = color;
    }

    /**
     * Returns the accepted nickname.
     *
     * @return the player nickname
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Returns the assigned totem color.
     *
     * @return the player totem color
     */
    public TotemColor getColor(){
        return color;
    }

    /**
     * Applies this message to the given view.
     *
     * @param view the view that must show the join confirmation
     */
    @Override
    public void apply(View view){
        view.showJoinSuccess(nickname, color);
    }
}

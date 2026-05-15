package it.polimi.ingsw.network.socket.message;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;

/**
 * Message sent by a client attempting to reconnect
 * to a previously saved game after a server crash.
 */
public class ReconnectMessage extends ClientMessage {

    private final TotemColor color;

    /**
     * Creates a reconnect request.
     *
     * @param nickname the original player nickname
     * @param color the original player totem color
     */
    public ReconnectMessage(
            String nickname,
            TotemColor color
    ) {

        super(nickname);

        this.color = color;
    }

    /**
     * Returns the player's totem color.
     *
     * @return the saved totem color
     */
    public TotemColor getColor() {
        return color;
    }

    /**
     * Executes the reconnect request on the controller.
     *
     * @param controller the game controller
     * @param sender the reconnecting client view
     */
    @Override
    public void execute(
            GameController controller,
            VirtualView sender
    ) {

        controller.reconnectPlayer(
                getNickname(),
                color,
                sender
        );
    }
}
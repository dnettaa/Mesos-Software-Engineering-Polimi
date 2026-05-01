package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify clients that a player has placed
 * their totem on an offer slot.
 * <p>
 * This is a delta message: it does not contain the full game state, but only
 * the information needed by the client-side model to update the affected
 * offer slot and the current active player.
 * @author Diana
 */

public class TotemPlacedMessage extends ServerMessage{
    private final String playerNickname;
    private final char slotID;
    private final String nextPlayerNickname;


    /**
     * Creates a new totem-placed message.
     *
     * @param playerNickname the nickname of the player who placed the totem
     * @param slotID the identifier of the offer slot where the totem was placed
     * @param nextPlayerNickname the nickname of the next player who must act
     */
    public TotemPlacedMessage(String playerNickname, char slotID, String nextPlayerNickname) {
        this.playerNickname = playerNickname;
        this.slotID = slotID;
        this.nextPlayerNickname = nextPlayerNickname;
    }

    /**
     * Returns the nickname of the player who placed the totem.
     *
     * @return the player nickname
     */
    public String getPlayerNickname(){
        return playerNickname;
    }

    /**
     * Returns the identifier of the offer slot where the totem was placed.
     *
     * @return the offer slot identifier
     */
    public char getSlotID(){
        return slotID;
    }

    /**
     * Returns the nickname of the next player who must act.
     *
     * @return the next active player nickname
     */
    public String getNextPlayerNickname(){
        return nextPlayerNickname;
    }

    /**
     * Applies this delta update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display this update
     */
    @Override
    public void apply(View view){
        view.getClientModel().placeTotemOnSlot(playerNickname, slotID);
        view.getClientModel().setCurrentPlayer(nextPlayerNickname);
        view.render();
    }
}
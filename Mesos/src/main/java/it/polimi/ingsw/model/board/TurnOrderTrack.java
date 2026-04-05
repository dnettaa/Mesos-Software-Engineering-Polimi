package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.player.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the turn order track of the game.
 * It stores the current player order for the next round and
 * the food bonuses associated with each position.
 *
 * @author Diana
 */
public class TurnOrderTrack {
    private final List<Player> order;
    private final int[] foodBonus;
    private final int numPlayers;

    /**
     * Creates a new turn order track.
     *
     * @param players the players initially placed on the track
     * @param foodBonus the food bonus associated with each position
     * @param numPlayers the number of players in the game
     */
    public TurnOrderTrack(List<Player> players, int[] foodBonus, int numPlayers) {
        this.order = new ArrayList<>(players);
        this.foodBonus = foodBonus.clone(); //per non condividere direttamente l'array esterno
        this.numPlayers = numPlayers;
    }

    /**
     * Returns the food bonus associated with a given position.
     *
     * @param position the position index in the track
     * @return the food bonus for that position
     * @throws IndexOutOfBoundsException if the position is invalid
     */
    public int getPositionFoodBonus(int position){
        return foodBonus[position];
    }

    /**
     * Returns the position of the given player in the turn order track.
     *
     * @param player the player to search for
     * @return the index of the player in the track
     * @throws IllegalArgumentException if the player is not present in the track
     */
    public int getPositionOf(Player player){
        int index = order.indexOf(player);

        if(index == -1){
            throw new IllegalArgumentException("Player is not present in th eturn order track");
        }

        return index;
    }

    /**
     * Checks whether the given player is in the last position of the track.
     *
     * @param player the player to check
     * @return true if the player is in the last position, false otherwise
     */
    public boolean isLastPosition(Player player){
        return getPositionOf(player) == numPlayers - 1;
    }

    /**
     * Returns the players currently stored in turn order.
     *
     * @return a copy of the player order list
     */
    public List<Player> getPlayersInOrder(){
        return new ArrayList<>(order);
    }

    /**
     * Places the given player in the first free position of the turn order track.
     *
     * @param player the player to place
     * @throws IllegalStateException if the track is already full
     */
    public void placeFirstSlot(Player player){
        if(order.size() >= numPlayers){
            throw new IllegalStateException("Turn order track is already full");
        }

        order.add(player);
    }

    /**
     * Clears the turn order track.
     */
    public void clear(){
        order.clear();
    }

}
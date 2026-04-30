package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents a single slot in the offer track.
 * Each slot defines how many cards can be selected from the upper and lower rows,
 * the food reward associated with the slot, and the player currently occupying it.
 *
 * @author Diana
 */

public class OfferSlot {
    private final char slotID;
    private final int upSel;
    private final int downSel;
    private final int foodReward;
    private Player occupant;

    /**
     * Creates a new offer slot with its fixed action values and food reward.
     * The slot is initially unoccupied.
     *
     * @param slotID the identifier of the slot
     * @param upSel the number of cards that can be selected from the upper row
     * @param downSel the number of cards that can be selected from the lower row
     * @param foodReward the food reward associated with this slot
     */
    public OfferSlot(char slotID, int upSel, int downSel, int foodReward) {
        this.slotID = slotID;
        this.upSel = upSel;
        this.downSel = downSel;
        this.foodReward = foodReward;
        this.occupant = null;
    }

    /**
     * Returns the identifier of this offer slot.
     *
     * @return the slot identifier
     */
    public char getSlotID(){
        return slotID;
    }

    /**
     * Returns the number of cards that can be selected from the upper row.
     *
     * @return the upper row selection amount
     */
    public int getUpSel(){
        return upSel;
    }

    public int getDownSel(){
        return downSel;
    }

    /**
     * Returns the food reward associated with this slot.
     *
     * @return the food reward
     */
    public int getFoodReward() {
        return foodReward;
    }

    /**
     * Checks whether this slot is currently occupied by a player.
     *
     * @return true if the slot is occupied, false otherwise
     */
    public boolean isOccupied(){
        return occupant != null;
    }

    /**
     * Returns the player currently occupying this slot.
     *
     * @return the occupying player, or null if the slot is free
     */
    public Player getOccupant(){
        return occupant;
    }

    /**
     * Places a player on this slot.
     *
     * @param player the player to place on the slot
     * @throws GameException with {@link ErrorCode#SLOT_OCCUPIED} if the slot is already occupied
     */
    public void place(Player player){
        if(isOccupied()){
            throw new GameException(ErrorCode.SLOT_OCCUPIED, "Offer slot is already occupied");
        }

        occupant = player;
    }

    /**
     * Removes and returns the player currently occupying this slot.
     * After this operation, the slot becomes free.
     */
    public void remove(){
        Player removedPlayer = occupant;

        occupant = null;

    }

    /**
     * Builds and returns a data transfer object representing the current state of this offer slot.
     *
     * @return an {@link OfferSlotData} object containing the slot's configuration and its current occupant
     */
    public OfferSlotData buildOfferSlotData(){
        String occupantNickname = isOccupied() ? occupant.getNickname() : null;
        return new OfferSlotData(slotID, upSel, downSel, foodReward, occupantNickname);
    }
}

package it.polimi.ingsw.model.board;

import it.polimi.ingsw.network.message.OfferSlotData;
import it.polimi.ingsw.model.exception.ErrorCode;
import it.polimi.ingsw.model.exception.GameException;
import it.polimi.ingsw.model.player.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the offer track of the game.
 * It contains all the available offer slots and provides methods
 * to place players, inspect slot availability, and retrieve actions
 * and resolution order.
 *
 * @author Diana
 */

public class OfferTrack {
    private final List<OfferSlot> slots;   //dopo il costruttore il riferimento alla lista non cambia (magari il contenuto si) quindi final

    /**
     * Creates a new offer track with the given list of offer slots.
     *
     * @param slots the list of slots that compose the offer track
     */
    public OfferTrack(List<OfferSlot> slots) {
        this.slots = new ArrayList<>(slots);
    }

    /**
     * Returns the slot with the given identifier.
     *
     * @param slotID the identifier of the desired slot
     * @return the matching offer slot
     * @throws GameException with {@link ErrorCode#INVALID_SELECTION} if no slot with the given ID exists
     */
    public OfferSlot getSlot(char slotID) {
        for(OfferSlot slot : slots){
            if(slot.getSlotID() == slotID){
                return slot;
            }
        }

        throw new GameException(ErrorCode.INVALID_SELECTION, "No offer slot found with ID: " + slotID);
    }


    /**
     * Places a player on the specified offer slot.
     *
     * @param player the player to place
     * @param slotID the identifier of the target slot
     * @throws GameException with {@link ErrorCode#INVALID_SELECTION} if no slot with the given ID exists
     * @throws IllegalStateException if the target slot is already occupied
     */
    public void placePlayer(Player player, char slotID){
        OfferSlot slot = getSlot(slotID);

        slot.place(player);
    }

    /**
     * Returns the occupied slots in left-to-right resolution order.
     *
     * @return a list containing only the occupied slots, in track order
     */
    public List<OfferSlot> getResolutionOrder(){
        List<OfferSlot> resolutionOrder = new ArrayList<>();

        for(OfferSlot slot : slots){
            if(slot.isOccupied()){
                resolutionOrder.add(slot);
            }
        }

        return resolutionOrder;
    }

    /**
     * Returns the action associated with the slot occupied by the given player.
     * The returned array contains:
     * index 0 -> number of upper row selections
     * index 1 -> number of lower row selections
     *
     * @param player the player whose action must be retrieved
     * @return an array containing upper and lower row selection counts
     * @throws IllegalArgumentException if the player is not occupying any slot
     */
    public int[] getActionFor(Player player){
        for(OfferSlot slot : slots){
            if(player.equals(slot.getOccupant())){
                return new int[]{slot.getUpSel(), slot.getDownSel()};
            }
        }

        throw new IllegalArgumentException("Player is not occupying any offer slot");
    }

    /**
     * Removes all players from the offer track, making every slot free again.
     */
    public void reset(){
        for(OfferSlot slot : slots){
            slot.remove();
        }
    }

    /**
     * Builds and returns a list of data transfer objects representing the current state of all offer slots.
     *
     * @return a list of {@link OfferSlotData} representing the entire offer track
     */
    public List<OfferSlotData> buildOfferSlotsData(){
        List<OfferSlotData> result = new ArrayList<>();
        for(OfferSlot o: slots){
            result.add(o.buildOfferSlotData());
        }
        return result;
    }

}
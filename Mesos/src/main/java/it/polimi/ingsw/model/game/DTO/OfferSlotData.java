package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

/**
 * Serializable DTO containing the public state of an offer slot.
 * <p>
 * This class is used inside GameStateMessage to transfer offer track information
 * without exposing the model OfferSlot object.
 */

public class OfferSlotData implements Serializable{
    private static final long serialVersionUID = 1L;
    private final char slotID;
    private final int upSel;
    private final int downSel;
    private final int foodReward;
    private final String occupantNickname;

    /**
     * Creates a new offer slot data transfer object.
     *
     * @param slotID the slot identifier
     * @param upSel the number of cards selectable from the upper row
     * @param downSel the number of cards selectable from the lower row
     * @param foodReward the food reward provided by the slot
     * @param occupantNickname the nickname of the occupying player, or null if the slot is empty
     */
    public OfferSlotData(char slotID, int upSel, int downSel, int foodReward, String occupantNickname) {
        this.slotID = slotID;
        this.upSel = upSel;
        this.downSel = downSel;
        this.foodReward = foodReward;
        this.occupantNickname = occupantNickname;
    }

    public char getSlotID(){
        return slotID;
    }

    public int getUpSel(){
        return upSel;
    }

    public int getDownSel(){
        return downSel;
    }

    public int getFoodReward(){
        return foodReward;
    }

    public String getOccupantNickname(){
        return occupantNickname;
    }
}
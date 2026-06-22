package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

/**
 * Public serializable state of an offer slot.
 *
 * @param slotID the offer slot identifier
 * @param upSel the number of cards selectable from the upper row
 * @param downSel the number of cards selectable from the lower row
 * @param foodReward the food reward granted by the slot
 * @param occupantNickname the nickname of the occupying player, or null if the slot is empty
 *
 * @author Luca Grecchi
 */
public record OfferSlotData(
        char slotID,
        int upSel,
        int downSel,
        int foodReward,
        String occupantNickname
) implements Serializable {
        private static final long serialVersionUID = 1L;
}
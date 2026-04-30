package it.polimi.ingsw.model.game.DTO;

import java.io.Serializable;

public record OfferSlotData(
        char slotID,
        int upSel,
        int downSel,
        int foodReward,
        String occupantNickname
) implements Serializable {
        private static final long serialVersionUID = 1L;
}
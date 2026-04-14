package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants a food bonus when the player
 * is placed in the turn order track.
 *
 * @author Andrea Markvukaj
 */
public class TurnOrderBonusCard extends BuildingCard {

    private final int foodBonus;

    public TurnOrderBonusCard(Era era, String id, int cost, int prestigePoints, int foodBonus) {
        super(era, id, cost, prestigePoints);
        this.foodBonus = foodBonus;
    }
    /**
     * Grants foodBonus if the turn order slot is a food granting slot.
     * Control logic is implemented in Game class.
     *
     * @param slotIndex the turn's order slot index
     * @param player the player owning the building
     */
    @Override
    public void onTurnOrderPlaced(int slotIndex, Player player) {

        player.addFood(foodBonus);
    }
}
package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that provides a food discount during the Sustenance event
 * based on the number of Characters of a specific type in the player's tribe.
 * For each Character of the target type, the player receives a discount
 * of 1 food unit during this event.
 *
 * @author Andrea Markvukaj
 */
public class SustenanceDiscountCard extends BuildingCard{

    private final CharacterType targetCharacterType;

    public SustenanceDiscountCard(Era era, String id, int cost, int prestigePoints, CharacterType targetCharacterType) {

        super(era, id, cost, prestigePoints);
        this.targetCharacterType = targetCharacterType;
    }

    /**
     * Returns the sustenance discount provided by this building.
     * The discount is equal to the number of Characters of the specified
     * target type in the player's tribe.
     *
     * @param player the owner of the building
     * @return the food discount applied during the Sustenance event
     */
    @Override
    public int getSustenanceDiscount(Player player) {

        return player.getTribe().countByType(targetCharacterType);
    }
}

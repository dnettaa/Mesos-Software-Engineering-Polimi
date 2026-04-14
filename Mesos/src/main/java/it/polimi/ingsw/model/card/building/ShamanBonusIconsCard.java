package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants additional Shaman icons to the player.
 * The bonus is applied during the Shamanic Ritual event when
 * calculating the total number of Shaman icons.
 *
 * @author Andrea Markvukaj
 */
public class ShamanBonusIconsCard extends BuildingCard {

    private final int bonusIcons;

    public ShamanBonusIconsCard(Era era, String id, int cost, int prestigePoints, int bonusIcons) {

        super(era, id, cost, prestigePoints);
        this.bonusIcons = bonusIcons;
    }

    /**
     * Returns the additional number of Shaman icons provided by this building.
     *
     * @return the bonus Shaman icons
     */
    @Override
    public int getBonusShamanIcons() {

        return bonusIcons;
    }
}

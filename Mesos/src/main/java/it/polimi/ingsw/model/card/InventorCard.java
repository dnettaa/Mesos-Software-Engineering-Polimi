package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;

/**
 * Class representing Inventor Card.
 * Inventor Card provides an invention icon,
 * which are used in some game mechanics such as building effect.
 *
 * @author Andrea Markvukaj
 */
public class InventorCard extends CharacterCard {

    private final InventionType inventionType;

    public InventorCard(Era era, String id, InventionType inventionType) {
        super(era, id);
        this.inventionType = inventionType;
    }

    /**
     * Returns the invention type associated with this card.
     * See enum class <<InventionType>>.
     *
     * @return the invention type
     */
    public InventionType getInventionType() {
        return inventionType;
    }

    /**
     * Applies this INVENTOR card to the given player.
     * Behavior:
     * Adding the character to the player's tribe.
     * Granting food based on {@link #getFoodOnAcquired(Tribe)}
     * Triggering building effects that react to character acquisition.
     *
     * @param player the player acquiring the card
     */
    @Override
    public void applyTo(Player player) {
        Tribe tribe = player.getTribe();

        tribe.getMembers().get(CharacterType.INVENTOR).add(this);

        player.addFood(getFoodOnAcquired(tribe));

        for (BuildingCard b : tribe.getBuildings()) {
            b.onCharacterAdded(this, player);
        }
    }
}


package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;

/**
 * Class representing Gatherer Card.
 *
 * @author Andrea Markvukaj
 */
public class GathererCard extends CharacterCard {

    public GathererCard(Era era, String id) {
        super(era, id);
    }

    /**
     * Applies this GATHERER card to the given player.
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

        tribe.getMembers().get(CharacterType.GATHERER).add(this);

        player.addFood(getFoodOnAcquired(tribe));

        for (BuildingCard b : tribe.getBuildings()) {
            b.onCharacterAdded(this, player);
        }
    }
}

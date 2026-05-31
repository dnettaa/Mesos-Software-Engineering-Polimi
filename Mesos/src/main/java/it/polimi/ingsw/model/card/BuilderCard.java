package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;

/**
 * Class representing Builder Card.
 * Builder Card provides a discount acquiring new buildings.
 * Builder Card may grant additional prestige points at the end of the game.
 *
 * @author Andrea Markvukaj
 */
public class BuilderCard extends CharacterCard{

    private final int builderDiscount;
    private final int builderPrestige;

    public BuilderCard(Era era, String id, int builderDiscount, int builderPrestige) {
        super(era, id);
        this.builderDiscount = builderDiscount;
        this.builderPrestige = builderPrestige;
    }

    /**
     * Returns the building cost discount provided by this card.
     *
     * @return the discount value
     */
    @Override
    public int getBuilderDiscount() {
        return builderDiscount;
    }

    /**
     * Returns the prestige points granted by this card.
     *
     * @return the prestige points
     */
    public int getBuilderPrestige() {
        return builderPrestige;
    }

    /**
     * Applies this BUILDER card to the given player.
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

        tribe.getMembers().get(CharacterType.BUILDER).add(this);

        player.addFood(getFoodOnAcquired(tribe));

        for (BuildingCard b : tribe.getBuildings()) {
            b.onCharacterAdded(this, player);
        }
    }
}

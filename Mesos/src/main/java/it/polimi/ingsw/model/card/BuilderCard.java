package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
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

    @Override
    public CharacterType getType() {
        return CharacterType.BUILDER;
    }
}

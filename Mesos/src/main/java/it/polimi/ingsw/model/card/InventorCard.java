package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
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

    @Override
    public CharacterType getType(){
        return CharacterType.INVENTOR;
    }
}


package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
/**
 * Class representing Shaman Card.
 * Shaman Card gives a certain number of shaman symbols to the player's tribe,
 * which are used in some game mechanics such as shamanic ritual event.
 *
 * @author Andrea Markvukaj
 */
public class ShamanCard extends CharacterCard{

    private final int shamanSymbols;

    public ShamanCard(Era era, String id, int shamanSymbols) {
        super(era, id);
        this.shamanSymbols = shamanSymbols;
    }
    /**
     * Returns the number of shaman symbols provided by this card.
     *
     * @return the number of shaman symbols
     */
    public int getShamanSymbols() {
        return shamanSymbols;
    }

    @Override
    public CharacterType getType() {
        return CharacterType.SHAMAN;
    }
}

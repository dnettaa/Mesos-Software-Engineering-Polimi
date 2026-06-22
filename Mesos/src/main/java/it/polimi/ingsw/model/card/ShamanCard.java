package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Tribe;

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

    /**
     * Applies this SHAMAN card to the given player.
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

        tribe.getMembers().get(CharacterType.SHAMAN).add(this);

        player.addFood(getFoodOnAcquired(tribe));

        for (BuildingCard b : tribe.getBuildings()) {
            b.onCharacterAdded(this, player);
        }
    }
}

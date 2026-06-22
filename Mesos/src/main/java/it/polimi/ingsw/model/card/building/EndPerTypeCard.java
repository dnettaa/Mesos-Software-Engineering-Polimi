package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants an end-game bonus based on
 * the number of Character cards of a specific type.
 * The player gains a fixed amount of prestige points
 * for each Character of the specified type.
 *
 * @author Andrea Markvukaj
 */
public class EndPerTypeCard extends BuildingCard {

    private final CharacterType targetType;
    private final int ppPerCard;

    public EndPerTypeCard(Era era, String id, int cost, int prestigePoints, CharacterType targetType, int ppPerCard) {

        super(era, id, cost, prestigePoints);
        this.targetType = targetType;
        this.ppPerCard = ppPerCard;
    }

    /**
     * Calculates the prestige points bonus based on the number
     * of Character cards of the target type.
     *
     * @param player the player whose tribe is getting evaluated
     * @return the bonus prestige points
     */
    @Override
    public int calculateEndGameBonus(Player player) {

        int count = player.getTribe().countByType(targetType);
        return count * ppPerCard;
    }
}
package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;
import java.util.List;

/**
 * Building card that grants an end-game bonus by doubling
 * the prestige points of Builder characters.
 * The bonus returned corresponds to the additional prestige points gained.
 *
 * @author Andrea Markvukaj
 */
public class EndDoubleBuilderCard extends BuildingCard {

    public EndDoubleBuilderCard(Era era, String id, int cost, int prestigePoints) {

        super(era, id, cost, prestigePoints);
    }

    /**
     * Calculates the additional prestige points gained by doubling
     * the value of Builder cards.
     *
     * @param player the player whose tribe is getting evaluated
     * @return the bonus prestige points
     */
    @Override
    public int calculateEndGameBonus(Player player) {

        List<CharacterCard> builders = player.getTribe().getByType(CharacterType.BUILDER);

        int total = 0;

        for (CharacterCard c : builders) {
            total += ((BuilderCard) c).getBuilderPrestige();
        }

        return total;
    }
}
package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that allows the player to perform an additional
 * card selection after the normal offer resolution phase.
 * When this building is owned, the game enters an extra phase
 * in which the player may take one additional card from the upper row, paying its cost.
 *
 * @author Andrea Markvukaj
 */
public class ExtraPickCard extends BuildingCard {

    public ExtraPickCard(Era era, String id, int cost, int prestigePoints) {

        super(era, id, cost, prestigePoints);
    }

    /**
     * Indicates that the player is allowed to perform an extra card phase.
     *
     * @return true, this building enables the extra pick phase
     */
    @Override
    public boolean requiresExtraCardPhase() {

        return true;
    }
}
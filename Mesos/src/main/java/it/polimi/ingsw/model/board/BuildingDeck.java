package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Building deck of the game.
 * It stores the building cards divided by era and allows
 * revealing all cards of a specific era.
 *
 * @author Diana
 */

public class BuildingDeck {
    private final List<BuildingCard> eraOneCards;
    private final List<BuildingCard> eraTwoCards;
    private final List<BuildingCard> eraThreeCards;

    /**
     * Creates a new building deck with the given cards divided by era.
     *
     * @param eraOneCards   the Era 1 building cards
     * @param eraTwoCards   the Era 2 building cards
     * @param eraThreeCards the Era 3 building cards
     */
    public BuildingDeck(List<BuildingCard> eraOneCards, List<BuildingCard> eraTwoCards, List<BuildingCard> eraThreeCards) {
        this.eraOneCards = new ArrayList<>(eraOneCards);
        this.eraTwoCards = new ArrayList<>(eraTwoCards);
        this.eraThreeCards = new ArrayList<>(eraThreeCards);
    }

    /**
     * Reveals all building cards of the specified era.
     * The returned list is a copy, so the internal deck state
     * cannot be modified from outside.
     *
     * @param era the era whose building cards must be revealed
     * @return a copy of the building cards of that era
     */
    //logica di quando rivelare era 1-2-3 gestita in Board
    public List<BuildingCard> revealAll(Era era) { //restituisco copia della lista, così se qualcuno modifica non succ nulla
        return switch (era) {
            case Era1 -> new ArrayList<>(eraOneCards);
            case Era2 -> new ArrayList<>(eraTwoCards);
            case Era3 -> new ArrayList<>(eraThreeCards);
        };
    }
}
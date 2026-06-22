package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;

/**
 * Represents the Building deck of the game.
 * It stores the building cards divided by era and allows
 * revealing all cards of a specific era.
 *
 * @author Diana
 */

public class BuildingDeck {
    private final Map<Era, List<BuildingCard>> cardsByEra;

    /**
     * Creates a new building deck with the given cards divided by era.
     *
     * @param eraOneCards the Era 1 building cards
     * @param eraTwoCards the Era 2 building cards
     * @param eraThreeCards the Era 3 building cards
     */
    public BuildingDeck(List<BuildingCard> eraOneCards, List<BuildingCard> eraTwoCards, List<BuildingCard> eraThreeCards) {
        this.cardsByEra = new EnumMap<>(Era.class);
        this.cardsByEra.put(Era.Era1, new ArrayList<>(eraOneCards));
        this.cardsByEra.put(Era.Era2, new ArrayList<>(eraTwoCards));
        this.cardsByEra.put(Era.Era3, new ArrayList<>(eraThreeCards));
    }

    /**
     * Reveals all building cards belonging to the specified era.
     * <p>
     * The returned list is a defensive copy, so external callers
     * cannot modify the internal state of the deck.
     *
     * @param era the era whose building cards must be revealed
     * @return a copy of all building cards of that era
     * @throws NullPointerException if the era is null
     */
    public List<BuildingCard> revealAll(Era era){
        return new ArrayList<>(cardsByEra.getOrDefault(era, List.of()));
    }
}
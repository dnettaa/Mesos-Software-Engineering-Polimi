package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that provides additional food for each Artist.
 *
 * @author Andrea Markvukaj
 */
public class PaintingsFoodCard extends BuildingCard {

    private final int foodPerArtist;

    public PaintingsFoodCard(Era era, String id, int cost, int prestigePoints, int foodPerArtist) {

        super(era, id, cost, prestigePoints);
        this.foodPerArtist = foodPerArtist;
    }

    /**
     * Returns the food bonus provided by this building for each Artist.
     *
     * @return the food bonus per Artist
     */
    @Override
    public int getPaintingsBonusFood() {

        return foodPerArtist;
    }
}
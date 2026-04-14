package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Building card that grants a food bonus when the player completes
 * a new full set of distinct Character types.
 * A set consists of one Character for each available type.
 * The effect is triggered only when a new set is completed
 * after acquiring this building, ignoring sets already completed.
 *
 * @author Andrea Markvukaj
 */
public class TriggerSetCard extends BuildingCard{

    private final int foodBonus;
    private int lastSetCount = 0;

    public TriggerSetCard(Era era, String id, int cost, int prestigePoints, int foodBonus) {

        super(era, id, cost, prestigePoints);
        this.foodBonus = foodBonus;
    }

    /**
     * Applies this building card to the player and initializes
     * the internal set counter based on the current tribe state.
     * This ensures that only sets completed after acquiring the building
     * will trigger the bonus.
     *
     * @param player the player acquiring the building
     */
    @Override
    public void applyTo(Player player) {

        super.applyTo(player);
        this.lastSetCount = player.getTribe().getFullSetsCount();
    }

    /**
     * Triggered when a Character is added to the player's tribe.
     * If the addition results in an increase in the number of complete sets,
     * the player gains a food bonus.
     *
     * @param card the character that has been added
     * @param player the owner of the building
     */
    @Override
    public void onCharacterAdded(CharacterCard card, Player player) {

        int currentSets = player.getTribe().getFullSetsCount();

        if (currentSets > lastSetCount) {
            player.addFood(foodBonus);
            lastSetCount = currentSets;
        }
    }
}

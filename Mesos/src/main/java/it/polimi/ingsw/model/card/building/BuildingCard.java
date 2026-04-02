package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;

/**
 * Abstract class representing a building card.
 * Building cards provide ongoing effects, triggers, or end-game bonuses.
 * Most methods have default implementations, they will be overridden
 * by subclasses to define specific behaviors.
 *
 * @author Andrea Markvukaj
 */
public abstract class BuildingCard extends Card {

    private final int cost;
    private final int prestigePoints;

    protected BuildingCard(Era era, String id, int cost, int prestigePoints)
    {
        super(era, id);
        this.cost = cost;
        this.prestigePoints = prestigePoints;
    }

    public int getCost() {
        return cost;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    /**
     * Triggered when a character is added to the player's tribe.
     * Default implementation does nothing.
     */
    public void onCharacterAdded(CharacterCard card, Player player) {}

    /**
     * Triggered when a player is placed in the turn order track.
     * Default implementation does nothing.
     */
    public void onTurnOrderPlaced(int slotIndex, Player player) {}

    /**
     * Indicates whether the player requires an extra card phase.
     */
    public boolean requiresExtraCardPhase() {
        return false;
    }

    /**
     * Calculates the eventual end-game bonus
     */
    public int calculateEndGameBonus(Player player) {
        return 0;
    }

    /**
     * Returns the sustenance discount provided by the building card.
     */
    public int getSustenanceDiscount(Player player) {
        return 0;
    }

    /**
     * Returns an additional 3 bonus shaman icons to the building's owner.
     */
    public int getBonusShamanIcons() {
        return 0;
    }

    /**
     * Indicates whether the player avoids minority penalty.
     */
    public boolean avoidsMinorityPenalty() {
        return false;
    }

    /**
     * Indicates whether the player doubles the winner reward.
     */
    public boolean doublesWinnerReward() {
        return false;
    }

    /**
     * Returns bonus food for hunt events.
     */
    public int getHunterBonusFood() {
        return 0;
    }

    /**
     * Returns bonus prestige points for hunt events.
     */
    public int getHuntBonusPP() {
        return 0;
    }

    /**
     * Returns bonus food for painting related events or effects.
     */
    public int getPaintingsBonusFood() {
        return 0;
    }
}

package it.polimi.ingsw.model.card.building;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.InventorCard;
import it.polimi.ingsw.model.card.InventionType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.Era;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Building card that grants a food bonus when the player forms
 * new pairs of Inventor characters with the same invention type.
 * A pair consists of two Inventor cards sharing the same InventionType.
 * The effect is triggered only when a new pair
 * is formed after acquiring this building.
 *
 * @author Andrea Markvukaj
 */
public class TriggerInventorCard extends BuildingCard {

    private final int foodBonus;
    private int lastPairs = 0;

    public TriggerInventorCard(Era era, String id, int cost, int prestigePoints, int foodBonus) {
        super(era, id, cost, prestigePoints);
        this.foodBonus = foodBonus;
    }

    /**
     * Applies this building card to the player and initializes
     * the internal pair counter based on the current tribe state.
     * This ensures that only pairs formed after acquiring the building
     * will trigger the bonus.
     *
     * @param player the player acquiring the building
     */
    @Override
    public void applyTo(Player player) {
        super.applyTo(player);
        this.lastPairs = countPairs(player);
    }


    /**
     * Triggered when a Character is added to the player's tribe.
     * If the addition results in an increased number of Inventor pairs,
     * the player gains a food bonus.
     *
     * @param card the character added to the tribe
     * @param player the owner of the building
     */
    @Override
    public void onCharacterAdded(CharacterCard card, Player player) {

        int currentPairs = countPairs(player);

        if (currentPairs > lastPairs) {
            player.addFood(foodBonus);
            lastPairs = currentPairs;
        }
    }

    /**
     * Counts the number of pairs of Inventor characters that share
     * the same invention type.
     * Inventors are grouped by their InventionType using a map,
     * where each key represents an invention type and the associated value
     * represents the number of Inventors of that type.
     * For each invention type, a pair is formed every two cards.
     * The total number of pairs is the sum of all pairs across different invention types.
     *
     * @param player the player whose tribe is analyzed
     * @return the total number of Inventor pairs
     */
    private int countPairs(Player player) {

        List<CharacterCard> inventors = player.getTribe().getByType(CharacterType.INVENTOR);
        Map<InventionType, Integer> counts = new HashMap<>();

        for (CharacterCard c : inventors) {
            InventionType type = ((InventorCard) c).getInventionType();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        int pairs = 0;

        for (int count : counts.values()) {
            pairs += count / 2;
        }

        return pairs;
    }
}
package it.polimi.ingsw.model.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.ingsw.model.card.building.*;
import it.polimi.ingsw.model.card.*;

/**
 * Represents a player's Tribe, managing all acquired cards
 * (Characters and Buildings) during the various rounds.
 * Contains the logic to group members by type and to calculate
 * the parameters necessary for resolving Events and the final score.
 *
 * @author Vadym Kitsul
 */
public class Tribe {

    /** Map grouping Character cards based on their type. */
    private final Map<CharacterType, List<CharacterCard>> members;

    /** List of Building cards owned by the tribe. */
    private final List<BuildingCard> buildings;

    /**
     * Constructs a new empty tribe.
     * Initializes the data structures necessary to hold the cards.
     */
    public Tribe() {
        this.members = new HashMap<>();
        for (CharacterType type : CharacterType.values()) {
            this.members.put(type, new ArrayList<>());
        }
        this.buildings = new ArrayList<>();
    }

    public Map<CharacterType, List<CharacterCard>> getMembers(){
        return members;
    }

    public List<BuildingCard> getBuildings(){
        return buildings;
    }

    /**
     * Returns the total number of Character cards of a specific type
     * currently present in the tribe.
     * @param type The Character type to count.
     * @return The number of cards of that type; will be 0 if none are present.
     */
    public int countByType(CharacterType type) {
        return this.members.getOrDefault(type, Collections.emptyList()).size();
    }

    /**
     * Returns a copy of the list containing all Characters
     * of a specific type present in the tribe.
     * @param type The desired Character type.
     * @return A list containing the cards of the specified type.
     */
    public List<CharacterCard> getByType(CharacterType type) {
        return new ArrayList<>(this.members.getOrDefault(type, Collections.emptyList()));
    }

    /**
     * Calculates the total number of Shaman icons owned by the tribe.
     * Used during the resolution of the "Shamanic Ritual" Event.
     * @return The sum of the icons present on all Shaman cards of the tribe.
     */
    public int countShamanIcons() {
        return getByType(CharacterType.SHAMAN).stream()
                .mapToInt(c -> ((ShamanCard) c).getShamanSymbols())
                .sum();
    }

    /**
     * Counts the number of distinct Invention icons present in the tribe.
     * Used at the end of the game for calculating the Prestige Points of the Inventors.
     * @return The number of different Invention icons.
     */
    public int countDistinctInventionIcons() {
        return (int) getByType(CharacterType.INVENTOR).stream()
                .map(c -> ((InventorCard) c).getInventionType())
                .distinct()
                .count();
    }

    /**
     * Calculates the total food discount offered by the tribe's Builders
     * for purchasing new Building cards.
     * @return The total discount derived from the sum of the Builders' values.
     */
    public int getBuildingDiscount() {
        return getByType(CharacterType.BUILDER).stream()
                .mapToInt(c -> ((BuilderCard) c).getBuilderDiscount())
                .sum();
    }

    /**
     * Calculates the food discount provided by the tribe's Gatherers.
     * As per the rules, each Gatherer provides a discount of 3 food units
     * during the resolution of the "Sustenance" Event.
     * @return The total food discount provided by the Gatherers.
     */
    public int getCollectorDiscount() {
        return countByType(CharacterType.GATHERER) * 3;
    }

    /**
     * Counts the number of distinct Character types currently present in the tribe.
     * Can range from 0 to 6.
     * @return The number of different Character types of which at least one card is owned.
     */
    public int countDistinctTypes() {
        return (int) this.members.values().stream()
                .filter(list -> !list.isEmpty())
                .count();
    }

    /**
     * Calculates the number of complete Character sets owned by the tribe.
     * A complete set consists of one card for each of the 6 Character types.
     * @return The number of complete sets formed by the tribe's cards.
     */
    public int getFullSetsCount() {
        if (countDistinctTypes() < 6) {
            return 0;
        }
        return this.members.values().stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);
    }
}
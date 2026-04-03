package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.card.CharacterType;
import it.polimi.ingsw.model.card.BuilderCard;
import it.polimi.ingsw.model.card.building.BuildingCard;

import java.util.List;

/**
 * Utility class responsible for calculating the final end-game scoring.
 * It computes the Prestige Points (PP) provided by Builders, Inventors,
 * Artists, and Buildings, and adds them directly to each player's score.
 * * @author Vadym Kitsul
 */
public class FinalScoringCalculator {

    /**
     * Iterates through all players and calculates their final Prestige Points,
     * adding the calculated bonuses directly to their score.
     * @param players The list of players in the game.
     */
    public static void calculate(List<Player> players) {
        for (Player player : players) {
            int totalBonus = 0;

            totalBonus += calculateBuilderPP(player);
            totalBonus += calculateInventorPP(player);
            totalBonus += calculateArtistPP(player);
            totalBonus += calculateBuildingPP(player);

            player.addPP(totalBonus);
        }
    }

    /**
     * Calculates the Prestige Points provided by Builder cards.
     * Each Builder gives the specific PP printed on the card.
     * @param player The player to calculate the score for.
     * @return The total PP from Builders.
     */
    private static int calculateBuilderPP(Player player) {
        return player.getTribe().getByType(CharacterType.BUILDER).stream()
                .mapToInt(c -> ((BuilderCard) c).getBuilderPrestige())
                .sum();
    }

    /**
     * Calculates the Prestige Points provided by Inventor cards.
     * Formula: (Number of Inventors) * (Number of distinct Invention icons).
     * @param player The player to calculate the score for.
     * @return The total PP from Inventors.
     */
    private static int calculateInventorPP(Player player) {
        Tribe tribe = player.getTribe();
        int numInventors = tribe.countByType(CharacterType.INVENTOR);
        int distinctIcons = tribe.countDistinctInventionIcons();

        return numInventors * distinctIcons;
    }

    /**
     * Calculates the Prestige Points provided by Artist cards.
     * Formula: 10 PP for every 2 Artists in the tribe.
     * @param player The player to calculate the score for.
     * @return The total PP from Artists.
     */
    private static int calculateArtistPP(Player player) {
        Tribe tribe = player.getTribe();
        int numArtists = tribe.countByType(CharacterType.ARTIST);

        return (numArtists / 2) * 10;
    }

    /**
     * Calculates the Prestige Points provided by Building cards.
     * This includes the base PP printed on the card plus any end-game effect PP.
     * @param player The player to calculate the score for.
     * @return The total PP from Buildings.
     */
    private static int calculateBuildingPP(Player player) {
        Tribe tribe = player.getTribe();
        int totalBuildingPP = 0;

        for (BuildingCard building : tribe.getBuildings()) {
            // Adds base PP of the building
            totalBuildingPP += building.getPrestigePoints();

            // Adds bonus PP in EndGame
            totalBuildingPP += building.calculateEndGameBonus(player);
        }

        return totalBuildingPP;
    }
}


package it.polimi.ingsw.model.player;

/**
 * Represents a player within a Mesos game.
 * The class manages the player's personal state, including their resources
 * (food and prestige points), and contains a reference to their Tribe ({@link Tribe}),
 * where acquired cards are collected.
 *
 * @author Vadym Kitsul
 */

public class Player {

    private final String nickname;
    private final TotemColor totemColor;
    private final Tribe tribe;
    private int food;
    private int prestigePoints;

    /**
     * Constructs a new player with the chosen name and color.
     * At the beginning of the game, the player starts with 0 food and 0 prestige points,
     * and a new empty tribe is instantiated.
     * @param nickname   The name chosen by the player.
     * @param totemColor The totem color assigned to the player.
     */

    public Player(String nickname, TotemColor totemColor, Tribe tribe, int food, int prestigePoints) {
        this.nickname = nickname;
        this.totemColor = totemColor;
        this.tribe = tribe;
        this.food = 0;
        this.prestigePoints = 0;
    }

    /**
     * Returns the player's nickname.
     * @return The player's nickname.
     */

    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the player's totem color.
     *
     * @return The totem color.
     */

    public TotemColor getTotemColor() {
        return totemColor;
    }

    /**
     * Returns the tribe associated with the player, containing their characters and buildings.
     * @return The {@link Tribe} instance of the player.
     */

    public Tribe getTribe() {
        return tribe;
    }

    /**
     * Returns the amount of food currently owned by the player.
     * @return The number of food tokens of the player.
     */

    public int getFood() {
        return food;
    }

    /**
     * Returns the current Prestige Points (PP) of the player.
     * @return The player's Prestige Points (can be negative).
     */

    public int getPrestigePoints() {
        return prestigePoints;
    }

    /**
     * Adds a specific amount of food to the player's reserve.
     * @param amount The amount of food to add (must be greater than 0).
     */

    public void addFood(int amount) {
        if (amount > 0) {
            this.food += amount;
        }
    }

    /**
     * Spends a specific amount of food from the player's reserve, provided
     * the player has enough food.
     * @param amount The amount of food to spend (must be greater than 0).
     */
    public void spendFood(int amount) {
        if (amount > 0 && this.food >= amount) {
            this.food -= amount;
        }
    }

    /**
     * Adds Prestige Points (PP) to the player's score.
     * @param amount The amount of points to add (must be greater than 0).
     */
    public void addPP(int amount) {
        if (amount > 0) {
            this.prestigePoints += amount;
        }
    }

    /**
     * Subtracts Prestige Points (PP) from the player's score.
     * As per the rules, the total score can take negative values.
     * @param amount The amount of points to subtract (must be greater than 0).
     */
    public void losePP(int amount) {
        if (amount > 0) {
            this.prestigePoints -= amount;
        }
    }
}
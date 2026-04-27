package it.polimi.ingsw.message;

import it.polimi.ingsw.model.player.TotemColor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable DTO containing the public state of a player.
 * <p>
 * This class is used inside GameStateMessage to transfer player information
 * from the server to the clients without exposing model objects.
 */

public class PlayerData implements Serializable{
    private static final long serialVersionUID = 1L;
    private final String nickname;
    private final TotemColor totemColor;
    private final int food;
    private final int prestigePoints;
    private final List<String> tribeCardID;
    private final List<String> buildingID;

    /**
     * Creates a new player data transfer object.
     *
     * @param nickname the player nickname
     * @param totemColor the player totem color
     * @param food the amount of food owned by the player
     * @param prestigePoints the amount of prestige points owned by the player
     * @param tribeCardID the ids of the tribe cards owned by the player
     * @param buildingID the ids of the building cards owned by the player
     */
    public PlayerData(String nickname, TotemColor totemColor, int food, int prestigePoints, List<String> tribeCardID, List<String> buildingID) {
        this.nickname = nickname;
        this.totemColor = totemColor;
        this.food = food;
        this.prestigePoints = prestigePoints;
        this.tribeCardID = new ArrayList<>(tribeCardID);
        this.buildingID = new ArrayList<>(buildingID);
    }

    public String getNickname(){
        return nickname;
    }

    public TotemColor getTotemColor(){
        return totemColor;
    }

    public int getFood(){
        return food;
    }

    public int getPrestigePoints(){
        return prestigePoints;
    }

    public List<String> getTribeCardID(){
        return new ArrayList<>(tribeCardID);
    }

    public List<String> getBuildingID(){
        return new ArrayList<>(buildingID);
    }
}
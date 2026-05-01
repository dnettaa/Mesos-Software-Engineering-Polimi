package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the server to notify clients that a player has taken cards
 * from the board.
 * <p>
 * This is a delta message: it contains only the card identifiers removed from
 * the upper and lower rows and the player who received those cards.
 * @author Diana
 */

public class CardsTakenMessage extends ServerMessage{
    private final String playerNickname;
    private final List<String> upperCardIDs;
    private final List<String> lowerCardIDs;
    private final List<String> characterCardIDs;
    private final List<String> buildingCardIDs;
    private final int foodDelta;
    private final int prestigePointsDelta;
    private final String nextPlayerNickname;

    /**
     * Creates a new cards-taken message.
     *
     * @param playerNickname the nickname of the player who took the cards
     * @param upperCardIDs the ids of the cards removed from the upper row
     * @param lowerCardIDs the ids of the cards removed from the lower row
     * @param characterCardIDs the ids of the character cards added to the player's tribe
     * @param buildingCardIDs the ids of the building cards added to the player
     * @param foodDelta the food variation caused by the action
     * @param prestigePointsDelta the prestige points variation caused by the action
     * @param nextPlayerNickname the nickname of the next player who must act
     */
    public CardsTakenMessage(String playerNickname, List<String> upperCardIDs, List<String> lowerCardIDs, List<String> characterCardIDs, List<String> buildingCardIDs, int foodDelta, int prestigePointsDelta, String nextPlayerNickname) {
        this.playerNickname = playerNickname;
        this.upperCardIDs = new ArrayList<>(upperCardIDs);
        this.lowerCardIDs = new ArrayList<>(lowerCardIDs);
        this.characterCardIDs = new ArrayList<>(characterCardIDs);
        this.buildingCardIDs = new ArrayList<>(buildingCardIDs);
        this.foodDelta = foodDelta;
        this.prestigePointsDelta = prestigePointsDelta;
        this.nextPlayerNickname = nextPlayerNickname;
    }

    public String getPlayerNickname(){
        return playerNickname;
    }

    public List<String> getUpperCardIDs(){
        return new ArrayList<>(upperCardIDs);
    }

    public List<String> getLowerCardIDs(){
        return new ArrayList<>(lowerCardIDs);
    }

    public List<String> getCharacterCardIDs(){
        return new ArrayList<>(characterCardIDs);
    }

    public List<String> getBuildingCardIDs(){
        return new ArrayList<>(buildingCardIDs);
    }

    public int getFoodDelta(){
        return foodDelta;
    }

    public int getPrestigePointsDelta(){
        return prestigePointsDelta;
    }

    public String getNextPlayerNickname(){
        return nextPlayerNickname;
    }

    /**
     * Applies this delta update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display this update
     */
    @Override
    public void apply(View view){
        for(String cardID : upperCardIDs){
            view.getClientModel().removeUpperCard(cardID);
        }

        for(String cardID : lowerCardIDs){
            view.getClientModel().removeLowerCard(cardID);
        }

        for(String cardID : characterCardIDs){
            view.getClientModel().addTribeCardTo(playerNickname, cardID);
        }

        for(String cardID : buildingCardIDs){
            view.getClientModel().addBuildingTo(playerNickname, cardID);
        }

        view.getClientModel().adjustFood(playerNickname, foodDelta);
        view.getClientModel().adjustPP(playerNickname, prestigePointsDelta);
        view.getClientModel().setCurrentPlayer(nextPlayerNickname);

        view.render();
    }
}
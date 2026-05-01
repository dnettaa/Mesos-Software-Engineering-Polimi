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
    private final String nickname;
    private final List<String> takenUpperIDs;
    private final List<String> takenLowerIDs;
    private final List<String> addedTribeCardIDs;
    private final List<String> addedBuildingIDs;
    private final int foodDelta;
    private final int ppDelta;
    private final char freedSlotID;
    private final int turnOrderPosition;
    private final String nextPlayerNickname;

    /**
     * Creates a new cards-taken message.
     *
     * @param nickname the nickname of the player who took the cards
     * @param takenUpperIDs the ids of the cards removed from the upper row
     * @param takenLowerIDs the ids of the cards removed from the lower row
     * @param addedTribeCardIDs the ids of the tribe cards added to the player
     * @param addedBuildingIDs the ids of the building cards added to the player
     * @param foodDelta the food variation caused by the action
     * @param ppDelta the prestige points variation caused by the action
     * @param freedSlotID the id of the offer slot freed after resolving the action
     * @param turnOrderPosition the position where the player returned on the turn-order track
     * @param nextPlayerNickname the nickname of the next player who must act
     */
    public CardsTakenMessage(String nickname, List<String> takenUpperIDs, List<String> takenLowerIDs, List<String> addedTribeCardIDs, List<String> addedBuildingIDs, int foodDelta, int ppDelta, char freedSlotID, int turnOrderPosition, String nextPlayerNickname) {
        this.nickname = nickname;
        this.takenUpperIDs = new ArrayList<>(takenUpperIDs);
        this.takenLowerIDs = new ArrayList<>(takenLowerIDs);
        this.addedTribeCardIDs = new ArrayList<>(addedTribeCardIDs);
        this.addedBuildingIDs = new ArrayList<>(addedBuildingIDs);
        this.foodDelta = foodDelta;
        this.ppDelta = ppDelta;
        this.freedSlotID = freedSlotID;
        this.turnOrderPosition = turnOrderPosition;
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
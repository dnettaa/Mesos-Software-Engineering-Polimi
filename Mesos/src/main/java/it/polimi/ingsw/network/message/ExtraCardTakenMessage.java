package it.polimi.ingsw.network.message;

import it.polimi.ingsw.view.View;

/**
 * Message sent by the server to notify clients that a player has taken
 * an extra card.
 * <p>
 * This is a delta message used when a game effect allows a player to take
 * one additional card outside the standard offer resolution. It contains
 * only the card involved, the row it was removed from, the player who
 * received it, and the possible resource variations caused by the card.
 * @author Diana
 */

public class ExtraCardTakenMessage extends ServerMessage{
    private final String nickname;
    private final String cardID;
    private final boolean fromUpperRow;
    private final boolean isBuilding;
    private final int foodDelta;
    private final String nextPhaseName;

    /**
     * Creates a new extra-card-taken message.
     *
     * @param nickname the nickname of the player who took the extra card
     * @param cardID the id of the extra card taken by the player
     * @param fromUpperRow {@code true} if the card was removed from the upper row,
     *                     {@code false} if it was removed from the lower row
     * @param isBuilding {@code true} if the card is a building card,
     *                   {@code false} if it is a tribe card
     * @param foodDelta the food variation caused by taking the card
     */
    public ExtraCardTakenMessage(String nickname, String cardID, boolean fromUpperRow, boolean isBuilding, int foodDelta, String nextPhaseName) {
        this.nickname = nickname;
        this.cardID = cardID;
        this.fromUpperRow = fromUpperRow;
        this.isBuilding = isBuilding;
        this.foodDelta = foodDelta;
        this.nextPhaseName = nextPhaseName;
    }
    /**
     * Returns the nickname of the player who took the extra card.
     *
     * @return the player nickname
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Returns the id of the extra card taken by the player.
     *
     * @return the card id
     */
    public String getCardID(){
        return cardID;
    }

    /**
     * Checks whether the card was removed from the upper row.
     *
     * @return {@code true} if the card was removed from the upper row,
     *         {@code false} if it was removed from the lower row
     */
    public boolean isFromUpperRow(){
        return fromUpperRow;
    }

    /**
     * Checks whether the extra card is a building card.
     *
     * @return {@code true} if the card is a building card,
     *         {@code false} if it is a tribe card
     */
    public boolean isBuildingCard(){
        return isBuilding;
    }

    /**
     * Returns the food variation caused by taking the card.
     *
     * @return the food delta
     */
    public int getFoodDelta(){
        return foodDelta;
    }

    /**
     * Applies this delta update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display this update
     */
    @Override
    public void apply(View view) {
        if (fromUpperRow) {
            view.getClientModel().removeUpperCard(cardID);
        } else {
            view.getClientModel().removeLowerCard(cardID);
        }

        if (isBuilding) {
            view.getClientModel().addBuildingTo(nickname, cardID);
        } else {
            view.getClientModel().addTribeCardTo(nickname, cardID);
        }

        view.getClientModel().adjustFood(nickname, foodDelta);
        view.getClientModel().setCurrentPhase(nextPhaseName);
        view.render();
    }
}

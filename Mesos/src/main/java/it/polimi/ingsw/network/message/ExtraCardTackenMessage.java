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
 */

public class ExtraCardTackenMessage extends ServerMessage{
    private final String playerNickname;
    private final String cardID;
    private final boolean fromUpperRow;
    private final boolean buildingCard;
    private final int foodDelta;
    private final int prestigePointsDelta;
    private final String nextPlayerNickname;

    /**
     * Creates a new extra-card-taken message.
     *
     * @param playerNickname the nickname of the player who took the extra card
     * @param cardID the id of the extra card taken by the player
     * @param fromUpperRow {@code true} if the card was removed from the upper row,
     *                     {@code false} if it was removed from the lower row
     * @param buildingCard {@code true} if the card is a building card,
     *                     {@code false} if it is a tribe card
     * @param foodDelta the food variation caused by taking the card
     * @param prestigePointsDelta the prestige points variation caused by taking the card
     * @param nextPlayerNickname the nickname of the next player who must act
     */
    public ExtraCardTackenMessage(String playerNickname, String cardID, boolean fromUpperRow, boolean buildingCard, int foodDelta, int prestigePointsDelta, String nextPlayerNickname) {
        this.playerNickname = playerNickname;
        this.cardID = cardID;
        this.fromUpperRow = fromUpperRow;
        this.buildingCard = buildingCard;
        this.foodDelta = foodDelta;
        this.prestigePointsDelta = prestigePointsDelta;
        this.nextPlayerNickname = nextPlayerNickname;
    }
    /**
     * Returns the nickname of the player who took the extra card.
     *
     * @return the player nickname
     */
    public String getPlayerNickname() {
        return playerNickname;
    }

    /**
     * Returns the id of the extra card taken by the player.
     *
     * @return the card id
     */
    public String getCardID() {
        return cardID;
    }

    /**
     * Checks whether the card was removed from the upper row.
     *
     * @return {@code true} if the card was removed from the upper row,
     *         {@code false} if it was removed from the lower row
     */
    public boolean isFromUpperRow() {
        return fromUpperRow;
    }

    /**
     * Checks whether the extra card is a building card.
     *
     * @return {@code true} if the card is a building card,
     *         {@code false} if it is a tribe card
     */
    public boolean isBuildingCard() {
        return buildingCard;
    }

    /**
     * Returns the food variation caused by taking the card.
     *
     * @return the food delta
     */
    public int getFoodDelta() {
        return foodDelta;
    }

    /**
     * Returns the prestige points variation caused by taking the card.
     *
     * @return the prestige points delta
     */
    public int getPrestigePointsDelta() {
        return prestigePointsDelta;
    }

    /**
     * Returns the nickname of the next player who must act.
     *
     * @return the next active player nickname
     */
    public String getNextPlayerNickname() {
        return nextPlayerNickname;
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

        if (buildingCard) {
            view.getClientModel().addBuildingTo(playerNickname, cardID);
        } else {
            view.getClientModel().addTribeCardTo(playerNickname, cardID);
        }

        view.getClientModel().adjustFood(playerNickname, foodDelta);
        view.getClientModel().adjustPP(playerNickname, prestigePointsDelta);
        view.getClientModel().setCurrentPlayer(nextPlayerNickname);

        view.render();
    }
}

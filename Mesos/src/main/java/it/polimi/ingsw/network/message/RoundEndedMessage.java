package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.view.ClientModel;
import it.polimi.ingsw.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the server to notify clients that a round has ended.
 * <p>
 * This is a delta message: it contains only the information needed to update
 * the turn order, round number, and the next active player.
 * @author Diana
 */
public class RoundEndedMessage extends ServerMessage {
    private final List<String> discardedLowerTribeIDs;
    private final List<String> discardedLowerEventIDs;
    private final List<String> movedUpperToLowerTribeIDs;
    private final List<String> discardedLowerBuildingIDs;
    private final List<String> movedUpperToLowerBuildingIDs;
    private final List<String> newUpperRowIDs;
    private final List<String> revealedBuildingIDs;
    private final Era newEra;
    private final int newRound;
    private final List<String> newTurnOrder;
    private final String firstPlayerNickname;
    private final int tribeDeckRemaining;

    /**
     * Creates a new round-ended message.
     */
    public RoundEndedMessage(
            List<String> discardedLowerTribeIDs,
            List<String> discardedLowerEventIDs,
            List<String> movedUpperToLowerTribeIDs,
            List<String> discardedLowerBuildingIDs,
            List<String> movedUpperToLowerBuildingIDs,
            List<String> newUpperRowIDs,
            List<String> revealedBuildingIDs,
            Era newEra,
            int newRound,
            List<String> newTurnOrder,
            String firstPlayerNickname,
            int tribeDeckRemaining)
    {
        this.discardedLowerTribeIDs = new ArrayList<>(discardedLowerTribeIDs);
        this.discardedLowerEventIDs = new ArrayList<>(discardedLowerEventIDs);
        this.movedUpperToLowerTribeIDs = new ArrayList<>(movedUpperToLowerTribeIDs);
        this.discardedLowerBuildingIDs = new ArrayList<>(discardedLowerBuildingIDs);
        this.movedUpperToLowerBuildingIDs = new ArrayList<>(movedUpperToLowerBuildingIDs);
        this.newUpperRowIDs = new ArrayList<>(newUpperRowIDs);
        this.revealedBuildingIDs = new ArrayList<>(revealedBuildingIDs);
        this.newEra = newEra;
        this.newRound = newRound;
        this.newTurnOrder = new ArrayList<>(newTurnOrder);
        this.firstPlayerNickname = firstPlayerNickname;
        this.tribeDeckRemaining = tribeDeckRemaining;
    }

    public List<String> getDiscardedLowerTribeIDs() {
        return new ArrayList<>(discardedLowerTribeIDs);
    }

    public List<String> getDiscardedLowerEventIDs() {
        return new ArrayList<>(discardedLowerEventIDs);
    }

    public List<String> getMovedUpperToLowerTribeIDs() {
        return new ArrayList<>(movedUpperToLowerTribeIDs);
    }

    public List<String> getDiscardedLowerBuildingIDs() {
        return new ArrayList<>(discardedLowerBuildingIDs);
    }

    public List<String> getMovedUpperToLowerBuildingIDs() {
        return new ArrayList<>(movedUpperToLowerBuildingIDs);
    }

    public List<String> getNewUpperRowIDs() {
        return new ArrayList<>(newUpperRowIDs);
    }

    public List<String> getRevealedBuildingIDs() {
        return new ArrayList<>(revealedBuildingIDs);
    }

    public Era getNewEra() {
        return newEra;
    }

    /**
     * Returns the new round number.
     *
     * @return the round number
     */
    public int getNewRound() {
        return newRound;
    }

    /**
     * Returns the new turn order.
     *
     * @return a copy of the turn order list
     */
    public List<String> getNewTurnOrder() {
        return new ArrayList<>(newTurnOrder);
    }

    /**
     * Returns the nickname of the first active player for the new round.
     *
     * @return the first player nickname
     */
    public String getFirstPlayerNickname() {
        return firstPlayerNickname;
    }

    public int getTribeDeckRemaining() {
        return tribeDeckRemaining;
    }

    /**
     * Applies this delta update to the client-side model and renders the view.
     *
     * @param view the view that must apply and display this update
     */
    @Override
    public void apply(View view) {
        ClientModel model = view.getClientModel();
        if (model == null) return;

        List<String> lowerRow = new ArrayList<>(model.getLowerRowCardIDs());

        lowerRow.removeAll(discardedLowerTribeIDs);
        lowerRow.removeAll(discardedLowerEventIDs);
        lowerRow.removeAll(discardedLowerBuildingIDs);
        lowerRow.removeAll(movedUpperToLowerBuildingIDs);
        lowerRow.addAll(movedUpperToLowerTribeIDs);
        lowerRow.addAll(movedUpperToLowerBuildingIDs);

        model.setLowerRow(lowerRow);

        model.setUpperRow(newUpperRowIDs);

        model.setCurrentRound(newRound);
        model.setCurrentEra(newEra);
        model.setTurnOrder(newTurnOrder);
        model.setCurrentPlayer(firstPlayerNickname);
        model.setTribeDeckRemaining(tribeDeckRemaining);
        model.setCurrentPhase("TotemPlacementPhase");

        view.render();
    }
}
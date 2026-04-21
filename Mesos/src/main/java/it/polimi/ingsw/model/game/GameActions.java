package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OfferSlot;
import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * Interface defining the set of actions and accessible state of a game session.
 * This interface is used by the controller to interact with the game logic
 * and by the view layer to retrieve a read-only representation of the game state.
 * It exposes only the operations that are allowed during gameplay and avoids
 * direct access to the full {@link Game} implementation.
 * Implementations of this interface are responsible for enforcing game rules,
 * validating player actions, and updating the internal state.
 *
 * @author Andrea Markvukaj
 */
public interface GameActions {

    void placeTotem(Player player, char slotID);

    void takeCards(Player player, List<Card> chosenUpper, List<Card> chosenLower);

    void takeExtraCard(Player player, Card card);

    List<Player> getPlayers();

    Board getBoard();

    int getCurrentRound();

    int getCurrentPlayerIndex();

    List<Player> getPlacementOrder();

    List<OfferSlot> getResolutionOrder();
}
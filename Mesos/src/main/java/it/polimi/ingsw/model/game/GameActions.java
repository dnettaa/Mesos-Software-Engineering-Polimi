package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
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

    /**
     * Places the player's totem on the selected offer slot.
     *
     * @param nickname the nickname of the player placing the totem
     * @param slotID the identifier of the chosen offer slot
     */
    void placeTotem(String nickname, char slotID);

    /**
     * Lets a player take cards from the upper and lower rows.
     *
     * @param nickname the nickname of the player taking the cards
     * @param chosenUpperIDs identifiers of the cards chosen from the upper row
     * @param chosenLowerIDs identifiers of the cards chosen from the lower row
     * @param orderedIDs identifiers of all chosen cards in the order selected by the player
     */
    void takeCards(String nickname, List<String> chosenUpperIDs, List<String> chosenLowerIDs, List<String> orderedIDs);

    /**
     * Lets a player take an additional card granted by a special effect.
     *
     * @param nickname the nickname of the player taking the extra card
     * @param cardID the identifier of the selected card
     */
    void takeExtraCard(String nickname, String cardID);

    /**
     * Returns the nickname of the player currently expected to act.
     *
     * @return the active player's nickname, or null if no player is expected to act
     */
    String getCurrentPlayerNickname();

    /**
     * Returns the name of the current game phase.
     *
     * @return the simple class name of the current phase
     */
    String getCurrentPhaseName();

    /**
     * Returns whether the game has reached its final state.
     *
     * @return true if the game is ended, false otherwise
     */
    boolean isGameEnded();

    /**
     * Starts the game and notifies listeners with the initial state snapshot.
     */
    void startGame();

    /**
     * Registers a listener for game event notifications.
     *
     * @param listener the listener to add
     */
    void addListener(GameListener listener);

    /**
     * Unregisters a listener from game event notifications.
     *
     * @param listener the listener to remove
     */
    void removeListener(GameListener listener);

    /**
     * Builds a complete serializable snapshot of the current public game state.
     *
     * @return the current game state snapshot
     */
    GameStateSnapshot buildSnapshot();

}
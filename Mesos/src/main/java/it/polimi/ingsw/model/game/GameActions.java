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

    void placeTotem(String nickname, char slotID);

    void takeCards(String nickname, List<String> chosenUpperIDs, List<String> chosenLowerIDs, List<String> orderedIDs);

    void takeExtraCard(String nickname, String cardID);

    String getCurrentPlayerNickname();

    String getCurrentPhaseName();

    boolean isGameEnded();

    void startGame();

    void addListener(GameListener listener);

    void removeListener(GameListener listener);

    GameStateSnapshot buildSnapshot();

}
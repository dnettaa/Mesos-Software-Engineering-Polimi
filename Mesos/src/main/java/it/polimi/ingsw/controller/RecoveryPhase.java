package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.DTO.GameStateSnapshot;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.game.GameActions;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualView;

import java.util.HashSet;
import java.util.Set;

/**
 * Special controller phase used after a server crash.
 * <p>
 * During this phase the server waits for all original
 * players to reconnect before resuming the game.
 * <p>
 * Only players belonging to the saved match are allowed
 * to reconnect, and they must use the same nickname
 * and totem color originally associated with them.
 *
 * @author Andrea Markvukaj
 */
public class RecoveryPhase implements ControllerPhase {

    private final GameController controller;
    private final GameActions game;

    private final Set<String> reconnectedPlayers;

    private final int expectedTotal;

    /**
     * Creates a new recovery phase.
     *
     * @param controller the main game controller
     * @param game the restored game instance
     */
    public RecoveryPhase(
            GameController controller,
            GameActions game
    ) {

        this.controller = controller;
        this.game = game;

        this.reconnectedPlayers = new HashSet<>();

        GameStateSnapshot snapshot = game.buildSnapshot();

        this.expectedTotal = snapshot.players().size();
    }

    /**
     * Handles a reconnect request from a previously
     * connected player.
     * Validates:
     *     nickname belongs to original match
     *     totem color matches original player
     *     is not already reconnected
     * Once validated:
     *     the new view is registered
     *     the full snapshot is resent
     *     the game resumes when all players return
     */
    @Override
    public synchronized void reconnect(GameController controller, String nickname, TotemColor color, VirtualView view) {

        GameStateSnapshot snapshot = game.buildSnapshot();

        PlayerData playerData =
                snapshot.players()
                        .stream()
                        .filter(p -> p.nickname().equals(nickname))
                        .findFirst()
                        .orElse(null);

        /*
         * Reject unknown players
         */
        if(playerData == null){
            view.onError("RECOVERY_MODE", "Unknown player for recovery.");
            return;
        }

        /*
         * Validate original color
         */
        if(playerData.totemColor() != color){
            view.onError("RECOVERY_MODE", "Invalid recovery color.");
            return;
        }

        /*
         * Prevent duplicate reconnect
         */
        if(reconnectedPlayers.contains(nickname)){
            view.onError("RECOVERY_MODE", "Player already reconnected.");

            return;
        }

        /*
         * Register restored view
         */
        controller.registerView(nickname, view);

        reconnectedPlayers.add(nickname);

        System.out.println("[RECOVERY] Player reconnected: " + nickname + " (" + reconnectedPlayers.size() + "/"
                + expectedTotal + ")"
        );

        /*
         * Restore full client state
         */
        view.onGameStarted(snapshot);

        /*
         * Resume game once everybody is back
         */
        if(reconnectedPlayers.size() == expectedTotal){

            System.out.println("[RECOVERY] All players reconnected. Resuming game.");

            controller.transitionTo(new InGamePhase(controller, game));
        }
    }

    /**
     * Rejects any attempt to create a new lobby
     * while the server is restoring a match.
     */
    @Override
    public void createLobby(String nickname, TotemColor color, VirtualView view) {
        view.onError("RECOVERY_MODE", "Cannot create a new lobby while "
                + "a saved match is being restored.");
    }

    /**
     * Rejects normal lobby joins during recovery.
     */
    @Override
    public void joinLobby(String nickname, TotemColor color, VirtualView view) {
        view.onError("RECOVERY_MODE", "Server is currently restoring " + "a saved game.");
    }

    /**
     * Handles disconnects occurring during recovery.
     */
    @Override
    public void onDisconnect(String nickname) {

        reconnectedPlayers.remove(nickname);

        controller.unregisterView(nickname);
    }

    /**
     * Disabled during recovery.
     */
    @Override
    public void placeTotem(String nickname, char slotID) {}

    /**
     * Disabled during recovery.
     */
    @Override
    public void takeCards(String nickname, java.util.List<String> upperIDs, java.util.List<String> lowerIDs) {}

    /**
     * Disabled during recovery.
     */
    @Override
    public void takeExtraCard(String nickname, String cardID) {}
}